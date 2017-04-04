package com.faforever.server.client;

import com.faforever.server.FafServerApplication.ApplicationShutdownEvent;
import com.faforever.server.api.dto.UpdatedAchievementResponse;
import com.faforever.server.chat.JoinChatChannelResponse;
import com.faforever.server.common.ServerMessage;
import com.faforever.server.config.ServerProperties;
import com.faforever.server.coop.CoopMissionResponse;
import com.faforever.server.coop.CoopService;
import com.faforever.server.entity.AvatarAssociation;
import com.faforever.server.entity.Clan;
import com.faforever.server.entity.FeaturedMod;
import com.faforever.server.entity.Game;
import com.faforever.server.entity.GlobalRating;
import com.faforever.server.entity.Player;
import com.faforever.server.game.GameResponse;
import com.faforever.server.game.HostGameResponse;
import com.faforever.server.game.StartGameProcessResponse;
import com.faforever.server.ice.ForwardedIceMessage;
import com.faforever.server.ice.IceServerList;
import com.faforever.server.integration.ClientGateway;
import com.faforever.server.matchmaker.MatchCreatedResponse;
import com.faforever.server.matchmaker.MatchMakerResponse;
import com.faforever.server.mod.FeaturedModResponse;
import com.faforever.server.player.LoginDetailsResponse;
import com.faforever.server.player.PlayerResponse;
import com.faforever.server.player.PlayerResponse.Player.Rating;
import com.faforever.server.social.SocialRelationListResponse;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service to send messages to the client.
 */
@Service
@Slf4j
// TODO decide to which package(s) response classes belong and where they are created
public class ClientService {

  private static final Map<Class<?>, DelayedResponseAggregator<?, ?>> RESPONSE_AGGREGATORS = ImmutableMap.of(
    GameResponse.class, GameResponseAggregator.INSTANCE,
    PlayerResponse.class, PlayerResponseAggregator.INSTANCE
  );

  private static final BiFunction<PlayerResponses, PlayerResponses, PlayerResponses> PLAYER_RESPONSES_AGGREGATOR = (oldObject, newObject) -> {
    List<Integer> updatedPlayerIds = newObject.getResponses().stream().map(PlayerResponse::getPlayerId).collect(Collectors.toList());
    oldObject.getResponses().removeIf(playerResponse -> updatedPlayerIds.contains(playerResponse.getPlayerId()));
    oldObject.getResponses().addAll(newObject.getResponses());
    return oldObject;
  };

  private final ClientGateway clientGateway;
  private final CoopService coopService;
  private final ConcurrentMap<Object, DelayedResponse> delayedResponses;
  private final ServerProperties serverProperties;

  @VisibleForTesting
  Duration broadcastMinDelay;
  @VisibleForTesting
  Duration broadcastMaxDelay;

  public ClientService(ClientGateway clientGateway, CoopService coopService, ServerProperties serverProperties) {
    this.clientGateway = clientGateway;
    this.coopService = coopService;
    this.serverProperties = serverProperties;
    delayedResponses = new ConcurrentHashMap<>();

    broadcastMinDelay = Duration.ofSeconds(2);
    broadcastMaxDelay = Duration.ofSeconds(5);
  }

  public void startGameProcess(Game game, Player player) {
    log.debug("Telling '{}' to start game process for game '{}'", game.getHost(), game);
    send(new StartGameProcessResponse(game.getFeaturedMod().getTechnicalName(), game.getId(), getCommandLineArgs(player)), player);
  }

  /**
   * Tells the client to connect to a host. The game process must have been started before.
   *
   * @param player the player to send the message to
   * @param game the game to whose host to connect to
   */
  public void connectToHost(Player player, Game game) {
    Player host = game.getHost();

    log.debug("Telling '{}' to connect to host '{}'", player, host);
    send(new ConnectToHostResponse(host.getLogin(), host.getId()), player);
  }

  /**
   * Tells the client to connect to another player. The game process must have been started before.
   *
   * @param player the player to send the message to
   * @param otherPlayer the player to connect to
   * @param isOffer TODO document this
   */
  public void connectToPeer(Player player, Player otherPlayer, boolean isOffer) {
    log.debug("Telling '{}' to connect to '{}'", player, otherPlayer);
    send(new ConnectToPeerResponse(otherPlayer.getLogin(), otherPlayer.getId(), isOffer), player);
  }

  public void hostGame(Game game, @NotNull ConnectionAware recipient) {
    send(new HostGameResponse(game.getMapName()), recipient);
  }

  public void reportUpdatedAchievements(List<UpdatedAchievementResponse> playerAchievements, @NotNull ConnectionAware recipient) {
    send(new UpdatedAchievementsResponse(playerAchievements.stream()
        .map(item -> new UpdatedAchievementsResponse.UpdatedAchievement(
          item.getAchievementId(),
          item.getCurrentSteps(),
          item.getState(),
          item.isNewlyUnlocked()
        ))
        .collect(Collectors.toList())),
      recipient);
  }

  /**
   * Send a player his own information, usually called after successful login.
   */
  public void sendLoginDetails(Player player, @NotNull ConnectionAware recipient) {
    send(new LoginDetailsResponse(toPlayerInformationResponse(player)), recipient);
  }

  /**
   * @deprecated the client should fetch featured mods from the API.
   */
  @Deprecated
  public void sendModList(List<FeaturedMod> modList, @NotNull ConnectionAware recipient) {
    modList.forEach(mod -> send(new FeaturedModResponse(
      mod.getTechnicalName(), mod.getDisplayName(), mod.getDescription(), mod.getDisplayOrder()
    ), recipient));
  }

  public void sendGameList(GameResponses games, ConnectionAware recipient) {
    send(games, recipient);
  }

  /**
   * Enqueues a message that needs to be broadcast to all clients. Such messages can be hold back for a while in order
   * to avoid message flooding if the object is updated frequently in a short amount of time.
   *
   * @param object the object to be sent.
   * @param minDelay the minimum time to wait since the object has been updated.
   * @param maxDelay the maximum time to wait before the object is forcibly sent, even if the object has been updated
   * less than {@code minDelay} ago. This helps to avoid objects being delayed for too long if they receive frequent
   * updates.
   * @param idFunction the function to use to calculate the object's ID, so that subsequent calls can be associated with
   * previous submissions of the same object. Special care needs to be taken that the generated ID does not clash with
   * IDs generated by other callers, so it's advised to add a prefix like 'game-1' instead of '1'.
   * @param aggregateFunction the aggregate function to use if an object with the same ID is already queued.
   * @param <T> the type of the submitted object
   */
  @SuppressWarnings("unchecked")
  public <T extends ServerMessage> void broadcastDelayed(T object, Duration minDelay, Duration maxDelay, Function<T, Object> idFunction, BiFunction<T, T, T> aggregateFunction) {
    log.trace("Received object to send delayed: {}", object);
    delayedResponses.computeIfAbsent(idFunction.apply(object), o -> new DelayedResponse<>(object, minDelay, maxDelay, aggregateFunction))
      .onUpdated(object);
  }

  /**
   * @deprecated the client should ask the API instead
   */
  @Deprecated
  public void sendCoopList(ClientConnection clientConnection) {
    coopService.getMaps().stream()
      .map(map -> new CoopMissionResponse(map.getName(), map.getDescription(), map.getFilename()))
      .forEach(coopMissionResponse -> clientGateway.send(coopMissionResponse, clientConnection));
  }

  /**
   * Tells the client to drop game connection to the player with the specified ID.
   */
  public void disconnectPlayerFromGame(int playerId, Collection<? extends ConnectionAware> receivers) {
    receivers.forEach(recipient ->
      clientGateway.send(new DisconnectPlayerFromGameResponse(playerId), recipient.getClientConnection()));
  }

  @Scheduled(fixedDelay = 200)
  public void broadcastDelayedResponses() {
    List<Object> objectIds = delayedResponses.entrySet().stream()
      .filter(entry -> {
        DelayedResponse<?> delayedResponse = entry.getValue();
        Instant now = Instant.now();

        return now.isAfter(delayedResponse.getUpdateTime().plus(delayedResponse.getMinDelay()))
          || now.isAfter(delayedResponse.getCreateTime().plus(delayedResponse.getMaxDelay()));
      })
      .map(Map.Entry::getKey)
      .collect(Collectors.toList());

    if (objectIds.isEmpty()) {
      return;
    }

    log.trace("Aggregating and broadcasting '{}' messages", objectIds.size());
    objectIds.stream()
      .map(delayedResponses::remove)
      .collect(Collectors.groupingBy(DelayedResponse::getType)).entrySet().stream()
      .map(entry -> aggregateToServerMessages(entry.getKey(), entry.getValue()))
      .flatMap(Collection::stream)
      .forEach(clientGateway::broadcast);
  }

  public void sendMatchCreatedNotification(UUID requestId, int gameId, ConnectionAware recipient) {
    send(new MatchCreatedResponse(requestId, gameId), recipient);
  }

  /**
   * Aggregates a list of delayed responses if matching aggregator is available. For instance, this will convert a list
   * of {@link PlayerResponse} into a list with a single {@link PlayerResponses} object. If no aggregator is available,
   * the original list will be returned.
   *
   * @param type the type of the list to aggregate
   * @param delayedResponses a list of responses, containing objects of the specified type
   * @return a list of server messages
   */
  @SuppressWarnings("unchecked")
  private List<ServerMessage> aggregateToServerMessages(Class<?> type, List delayedResponses) {
    return Optional.ofNullable(RESPONSE_AGGREGATORS.get(type))
      .map(aggregator -> Collections.singletonList(aggregator.apply(delayedResponses)))
      .orElse((List<ServerMessage>) delayedResponses.stream()
        .map(response -> ((DelayedResponse) response).getResponse())
        .collect(Collectors.toList())
      );
  }

  /**
   * Notifies the player about available opponents in the matchmaker.
   *
   * @param queueName name of the queue that has opponents available
   */
  public void sendMatchmakerNotification(String queueName, ConnectionAware recipient) {
    send(new MatchMakerResponse(queueName), recipient);
  }

  /**
   * Sends a list of player information to the specified recipient.
   */
  public void sendPlayerInformation(Collection<Player> players, ConnectionAware recipient) {
    send(toPlayerResponses(players), recipient);
  }

  /**
   * Sends a list of player information to all authenticated clients.
   */
  public void broadcastPlayerInformation(Collection<Player> players) {
    broadcastDelayed(toPlayerResponses(players), broadcastMinDelay, broadcastMaxDelay, o -> "players", PLAYER_RESPONSES_AGGREGATOR);
  }

  /**
   * Sends a list of chat channels to join to the client.
   */
  public void sendChatChannels(Set<String> channelNames, ConnectionAware recipient) {
    // TODO write test
    send(new JoinChatChannelResponse(channelNames), recipient);
  }

  /**
   * Sends a list of ICE servers to the client.
   */
  public void sendIceServers(List<IceServerList> iceServers, ConnectionAware recipient) {
    send(new IceServersResponse(iceServers), recipient);
  }

  public void sendIceMessage(int senderId, Object content, ConnectionAware recipient) {
    send(new ForwardedIceMessage(senderId, content), recipient);
  }

  public void sendSocialRelations(SocialRelationListResponse response, ConnectionAware recipient) {
    send(response, recipient);
  }

  public void sendAvatarList(List<com.faforever.server.entity.Avatar> avatars, ConnectionAware recipient) {
    AvatarsResponse avatarListResponse = new AvatarsResponse(avatars.stream()
      .map(avatar -> new AvatarsResponse.Avatar(avatar.getUrl(), avatar.getDescription())).collect(Collectors.toList()));

    send(avatarListResponse, recipient);
  }

  private PlayerResponses toPlayerResponses(Collection<Player> players) {
    return new PlayerResponses(players.stream()
      .map(this::toPlayerInformationResponse)
      .collect(Collectors.toList()));
  }

  private PlayerResponse toPlayerInformationResponse(Player player) {
    Optional<PlayerResponse.Player.Avatar> avatar = player.getAvailableAvatars().stream()
      .filter(AvatarAssociation::isSelected)
      .findFirst()
      .map(association -> {
        com.faforever.server.entity.Avatar avatarEntity = association.getAvatar();
        return new PlayerResponse.Player.Avatar(avatarEntity.getUrl(), avatarEntity.getDescription());
      });

    Optional<Rating> globalRating = Optional.ofNullable(player.getGlobalRating())
      .map(rating -> new Rating(rating.getMean(), rating.getDeviation()));
    Optional<Rating> ladder1v1Rating = Optional.ofNullable(player.getLadder1v1Rating())
      .map(rating -> new Rating(rating.getMean(), rating.getDeviation()));

    return new PlayerResponse(
      player.getId(),
      player.getLogin(),
      player.getCountry(),
      player.getTimeZone(),
      new PlayerResponse.Player(
        globalRating.orElse(null),
        ladder1v1Rating.orElse(null),
        Optional.ofNullable(player.getGlobalRating()).map(GlobalRating::getNumGames).orElse(0),
        avatar.orElse(null),
        Optional.ofNullable(player.getClan()).map(Clan::getTag).orElse(null)
      )
    );
  }


  @EventListener
  @VisibleForTesting
  void onServerShutdown(ApplicationShutdownEvent event) {
    try {
      clientGateway.broadcast(new InfoResponse(serverProperties.getShutdown().getMessage()));
    } catch (Exception e) {
      log.warn("Could not broadcast shutdown to clients.", e);
    }
  }

  /**
   * @deprecated passing command line args to the client is a bad (legacy) idea.
   */
  @Deprecated
  private List<String> getCommandLineArgs(Player player) {
    int numGames = Optional.ofNullable(player.getGlobalRating()).map(GlobalRating::getNumGames).orElse(0);
    return Arrays.asList("/numgames", String.valueOf(numGames));
  }

  private void send(ServerMessage serverMessage, @NotNull ConnectionAware recipient) {
    ClientConnection clientConnection = recipient.getClientConnection();
    try {
      if (clientConnection == null) {
        throw new IllegalStateException("No connection available: " + recipient);
      }
      clientGateway.send(serverMessage, clientConnection);
    } catch (Exception e) {
      log.warn("Could not send message to connection '" + clientConnection + "': " + serverMessage, e);
    }
  }
}
