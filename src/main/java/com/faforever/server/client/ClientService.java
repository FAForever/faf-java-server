package com.faforever.server.client;

import com.faforever.server.api.dto.UpdatedAchievement;
import com.faforever.server.coop.CoopMissionResponse;
import com.faforever.server.coop.CoopService;
import com.faforever.server.entity.AvatarAssociation;
import com.faforever.server.entity.FeaturedMod;
import com.faforever.server.entity.Game;
import com.faforever.server.entity.GlobalRating;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
import com.faforever.server.game.DelayedResponse;
import com.faforever.server.game.GameResponse;
import com.faforever.server.game.HostGameResponse;
import com.faforever.server.integration.ClientGateway;
import com.faforever.server.integration.response.StartGameProcessResponse;
import com.faforever.server.matchmaker.MatchMakerResponse;
import com.faforever.server.mod.FeaturedModResponse;
import com.faforever.server.player.PlayerService;
import com.faforever.server.response.ServerResponse;
import com.faforever.server.security.FafUserDetails;
import com.faforever.server.security.UserDetailsResponse;
import com.faforever.server.security.UserDetailsResponse.Player.Avatar;
import com.faforever.server.security.UserDetailsResponse.Player.Rating;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service to send messages to the client.
 */
@Service
@Slf4j
public class ClientService {

  private final ClientGateway clientGateway;
  private final CoopService coopService;
  private final PlayerService playerService;
  private final ApplicationEventPublisher eventPublisher;
  private final Map<Object, DelayedResponse<?>> dirtyObjects;

  public ClientService(ClientGateway clientGateway, CoopService coopService, PlayerService playerService, ApplicationEventPublisher eventPublisher) {
    this.clientGateway = clientGateway;
    this.coopService = coopService;
    this.playerService = playerService;
    this.eventPublisher = eventPublisher;
    dirtyObjects = new HashMap<>();
  }

  public void startGameProcess(Game game, Player player) {
    log.debug("Telling '{}' to start game progress for game '{}'", game.getHost(), game);
    send(new StartGameProcessResponse(game.getFeaturedMod().getTechnicalName(), game.getId(), getCommandLineArgs(player)), player);
  }

  /**
   * Tells the client to connect to a host. The game process must have been started before.
   */
  public void connectToHost(Game game, @NotNull Player player) {
    log.debug("Telling '{}' to connect to '{}'", player, game.getHost());
    send(new ConnectToHostResponse(game.getHost().getId()), player);
  }

  /**
   * Tells the client to connect to another player. The game process must have been started before.
   *
   * @param player the player to send the message to
   * @param otherPlayer the player to connect to
   */
  public void connectToPlayer(Player player, Player otherPlayer) {
    log.debug("Telling '{}' to connect to '{}'", player, otherPlayer);
    send(new ConnectToPlayerResponse(otherPlayer.getLogin(), otherPlayer.getId()), player);
  }

  public void hostGame(Game game, @NotNull ConnectionAware connectionAware) {
    send(new HostGameResponse(game.getMapName()), connectionAware);
  }

  public void reportUpdatedAchievements(List<UpdatedAchievement> playerAchievements, @NotNull ConnectionAware connectionAware) {
    send(new UpdatedAchievementsResponse(playerAchievements.stream()
        .map(item -> new UpdatedAchievementsResponse.UpdatedAchievement(item.getCurrentSteps(), item.getState(), item.isNewlyUnlocked()))
        .collect(Collectors.toList())),
      connectionAware);
  }

  public void sendUserDetails(FafUserDetails userDetails, @NotNull ConnectionAware connectionAware) {
    Player player = userDetails.getPlayer();

    Optional<Avatar> avatar = player.getAvailableAvatars().stream()
      .filter(AvatarAssociation::isSelected)
      .findFirst()
      .map(association -> {
        com.faforever.server.entity.Avatar avatarEntity = association.getAvatar();
        return new Avatar(avatarEntity.getUrl(), avatarEntity.getTooltip());
      });

    Optional<Rating> globalRating = Optional.ofNullable(player.getGlobalRating())
      .map(rating -> new Rating(rating.getMean(), rating.getDeviation()));
    Optional<Rating> ladder1v1Rating = Optional.ofNullable(player.getLadder1v1Rating())
      .map(rating -> new Rating(rating.getMean(), rating.getDeviation()));

    send(new UserDetailsResponse(
        player.getId(),
        userDetails.getUsername(),
        player.getCountry(),
        new UserDetailsResponse.Player(
          globalRating.orElse(null),
          ladder1v1Rating.orElse(null),
          Optional.ofNullable(player.getGlobalRating()).map(GlobalRating::getNumGames).orElse(0),
          avatar.orElse(null)
        )
      ),
      connectionAware);
  }

  /**
   * @deprecated the client should fetch featured mods from the API.
   */
  @Deprecated
  public void sendModList(List<FeaturedMod> modList, @NotNull ConnectionAware connectionAware) {
    modList.forEach(mod -> send(new FeaturedModResponse(
      mod.getTechnicalName(), mod.getDisplayName(), mod.getDescription(), mod.getDisplayOrder()
    ), connectionAware));
  }

  public void sendGameList(Collection<GameResponse> games, ConnectionAware connectionAware) {
    games.forEach(game -> send(game, connectionAware));
  }

  /**
   * Sends a response that needs to be send to the client at some point in the future. Responses can be hold back for
   * a while in order to avoid message flooding if the object is updated frequently in a short amount of time.
   *
   * @param object the object to be sent.
   * @param minDelay the minimum time to wait since the object has been updated.
   * @param maxDelay the maximum time to wait before the object is forcibly sent, even if the object has been updated
   * less than {@code minDelay} ago. This helps to avoid objects being delayed for too long if they receive frequent
   * updates.
   * @param idFunction the function to use to calculate the object's ID, so that subsequent calls can be associated
   * with previous submissions of the same object.
   * @param <T> the type of the submitted object
   */
  public <T extends ServerResponse> void sendDelayed(T object, Duration minDelay, Duration maxDelay, Function<T, Object> idFunction) {
    synchronized (dirtyObjects) {
      dirtyObjects.computeIfAbsent(idFunction.apply(object),
        o -> new DelayedResponse<>(object, minDelay, maxDelay)).onUpdated();
    }
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
   * Tells the client to drop connection to the player with the specified ID.
   */
  public void disconnectPlayer(int playerId, Collection<? extends ConnectionAware> receivers) {
    receivers.forEach(connectionAware ->
      clientGateway.send(new DisconnectPlayerResponse(playerId), connectionAware.getClientConnection()));
  }

  @Scheduled(fixedDelay = 1000)
  public void sendDirtyObjects() {
    synchronized (dirtyObjects) {
      List<Object> objectIds = dirtyObjects.entrySet().stream()
        .filter(entry -> {
          DelayedResponse<?> delayedResponse = entry.getValue();
          Instant now = Instant.now();

          return now.isAfter(delayedResponse.getUpdateTime().plus(delayedResponse.getMinDelay()))
            || now.isAfter(delayedResponse.getCreateTime().plus(delayedResponse.getMaxDelay()));
        })
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());

      int size = objectIds.size();
      if (size < 1) {
        return;
      }

      log.trace("Sending '{}' delayed responses", size);
      objectIds.forEach(id -> {
        DelayedResponse<?> delayedResponse = dirtyObjects.get(id);
        clientGateway.broadcast(delayedResponse.getResponse());
        dirtyObjects.remove(id);
      });
    }
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
   * @deprecated passing command line args to the client is a bad (legacy) idea.
   */
  @Deprecated
  private List<String> getCommandLineArgs(Player player) {
    int numGames = Optional.ofNullable(player.getGlobalRating()).map(GlobalRating::getNumGames).orElse(0);
    return Arrays.asList("/numgames", String.valueOf(numGames));
  }

  private void send(ServerResponse serverResponse, @NotNull ConnectionAware connectionAware) {
    ClientConnection clientConnection = connectionAware.getClientConnection();
    if (clientConnection == null) {
      throw new IllegalStateException("No connection available: " + connectionAware);
    }
    clientGateway.send(serverResponse, clientConnection);
  }

  /**
   * Fires a {@link CloseConnectionEvent} in order to disconnect the client of the user with the specified ID.
   */
  void disconnectClient(User requester, int userId) {
    // TODO actually there should be a user service, returning a User
    Optional<Player> optional = playerService.getPlayer(userId);
    if (!optional.isPresent()) {
      log.warn("User '{}' requested disconnection of unknown user '{}'", requester, userId);
      return;
    }
    Player player = optional.get();
    eventPublisher.publishEvent(new CloseConnectionEvent(this, player.getClientConnection()));
    log.info("User '{}' closed connection of user '{}'", requester, player);
  }
}
