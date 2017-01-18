package com.faforever.server.client;

import com.faforever.server.api.dto.UpdatedAchievement;
import com.faforever.server.coop.CoopMissionResponse;
import com.faforever.server.coop.CoopService;
import com.faforever.server.entity.FeaturedMod;
import com.faforever.server.entity.Game;
import com.faforever.server.entity.GlobalRating;
import com.faforever.server.entity.Player;
import com.faforever.server.game.DirtyObject;
import com.faforever.server.game.GameResponse;
import com.faforever.server.game.HostGameResponse;
import com.faforever.server.integration.ClientGateway;
import com.faforever.server.integration.response.StartGameProcessResponse;
import com.faforever.server.mod.FeaturedModResponse;
import com.faforever.server.response.ServerResponse;
import com.faforever.server.security.FafUserDetails;
import com.faforever.server.security.UserDetailsResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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
  private final Map<Object, DirtyObject<?>> dirtyObjects;

  public ClientService(ClientGateway clientGateway, CoopService coopService) {
    this.clientGateway = clientGateway;
    this.coopService = coopService;
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

  public void reportUpdatedAchievements(List<UpdatedAchievement> playerAchievements,
                                        @NotNull ConnectionAware connectionAware) {
    send(new UpdatedAchievementsResponse(playerAchievements), connectionAware);
  }

  public void sendUserDetails(FafUserDetails userDetails, @NotNull ConnectionAware connectionAware) {
    clientGateway.send(new UserDetailsResponse(userDetails), connectionAware.getClientConnection());
  }

  /**
   * @deprecated the client should fetch featured mods from the API.
   */
  @Deprecated
  public void sendModList(List<FeaturedMod> modList, @NotNull ConnectionAware connectionAware) {
    modList.forEach(mod -> clientGateway.send(new FeaturedModResponse(mod), connectionAware.getClientConnection()));
  }

  public void sendGameList(Collection<Game> games, ConnectionAware connectionAware) {
    games.forEach(game -> clientGateway.send(new GameResponse(game), connectionAware.getClientConnection()));
  }

  /**
   * Submits a dirty object that needs to be send to the client. Dirty objects can be hold back for a while in order to
   * avoid message flooding if the object is updated frequently in a short amount of time.
   *
   * @param object the object to be sent.
   * @param minDelay the minimum time to wait since the object has been updated.
   * @param maxDelay the maximum time to wait before the object is forcibly sent, even if the object has been updated
   * less than {@code minDelay} ago. This helps to avoid objects being delayed for too long if they receive frequent
   * updates.
   * @param idFunction the function to use to calculate the object's ID, so that subsequent calls can be associated
   * with previous submissions of the same object.
   * @param responseCreator the function to use to create the {@link ServerResponse} which is used to send the object
   * to the client.
   * @param <T> the type of the submitted object
   */
  public <T> void submitDirty(T object, Duration minDelay, Duration maxDelay, Function<T, Object> idFunction,
                              Function<T, ServerResponse> responseCreator) {
    synchronized (dirtyObjects) {
      dirtyObjects.computeIfAbsent(idFunction.apply(object),
        o -> new DirtyObject<>(object, minDelay, maxDelay, responseCreator)).onUpdated();
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

  @Scheduled(fixedDelay = 1000)
  private void sendDirtyObjects() {
    synchronized (dirtyObjects) {
      List<Object> objectIds = dirtyObjects.entrySet().stream()
        .filter(entry -> {
          DirtyObject<?> dirtyObject = entry.getValue();
          Instant now = Instant.now();

          return now.isAfter(dirtyObject.getUpdateTime().plus(dirtyObject.getMinDelay()))
            || now.isAfter(dirtyObject.getCreateTime().plus(dirtyObject.getMaxDelay()));
        })
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());

      int size = objectIds.size();
      if (size > 0) {
        log.trace("Sending '{}' dirty objects", size);
        objectIds.forEach(id -> {
          DirtyObject<?> dirtyObject = dirtyObjects.get(id);
          clientGateway.broadcast(dirtyObject.createResponse());
          dirtyObjects.remove(id);
        });
      }
    }
  }
}
