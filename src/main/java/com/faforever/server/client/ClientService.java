package com.faforever.server.client;

import com.faforever.server.api.dto.UpdatedAchievement;
import com.faforever.server.entity.FeaturedMod;
import com.faforever.server.entity.Game;
import com.faforever.server.entity.GlobalRating;
import com.faforever.server.entity.Player;
import com.faforever.server.game.GameResponse;
import com.faforever.server.game.HostGameResponse;
import com.faforever.server.integration.ClientGateway;
import com.faforever.server.integration.response.StartGameProcessResponse;
import com.faforever.server.mod.FeaturedModResponse;
import com.faforever.server.response.ServerResponse;
import com.faforever.server.security.FafUserDetails;
import com.faforever.server.security.UserDetailsResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Service to send messages to the client.
 */
@Service
public class ClientService {

  private final ClientGateway clientGateway;

  public ClientService(ClientGateway clientGateway) {
    this.clientGateway = clientGateway;
  }

  public void startGameProcess(Game game, Player player) {
    send(new StartGameProcessResponse(game.getFeaturedMod().getTechnicalName(), game.getId(), getCommandLineArgs(player)), player);
  }

  /**
   * Tells the client to connect to a host. The game process must have been started before.
   */
  public void connectToHost(Game game, @NotNull ConnectionAware connectionAware) {
    send(new JoinGameResponse(game.getHost().getId()), connectionAware);
  }

  /**
   * @deprecated passing command line args to the client is a bad (legacy) idea.
   */
  @Deprecated
  private List<String> getCommandLineArgs(Player player) {
    short numGames = Optional.ofNullable(player.getGlobalRating()).map(GlobalRating::getNumGames).orElse((short) 0);
    return Arrays.asList("/numgames", String.valueOf(numGames));
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

  private void send(ServerResponse serverResponse, @NotNull ConnectionAware connectionAware) {
    ClientConnection clientConnection = connectionAware.getClientConnection();
    if (clientConnection == null) {
      throw new IllegalStateException("No connection available: " + connectionAware);
    }
    clientGateway.send(serverResponse, clientConnection);
  }
}
