package com.faforever.server.client;

import com.faforever.server.entity.Game;
import com.faforever.server.entity.GlobalRating;
import com.faforever.server.entity.Player;
import com.faforever.server.game.HostGameResponse;
import com.faforever.server.integration.ClientGateway;
import com.faforever.server.integration.response.StartGameProcessResponse;
import com.faforever.server.response.ServerResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Service to send commands to the client.
 */
@Service
public class ClientService {

  private final ClientGateway clientGateway;

  public ClientService(ClientGateway clientGateway) {
    this.clientGateway = clientGateway;
  }

  public void startGameProcess(Game game, Player player) {
    send(new StartGameProcessResponse(resolveMod(game), game.getId(), getCommandLineArgs(player)), player);
  }

  /**
   * Tells the client to connect to a host. The game process must have been started before.
   */
  public void connectToHost(Game game, Player player) {
    send(new JoinGameResponse(game.getHost().getId()), player);
  }

  /**
   * @deprecated passing command line args to the client is a bad (legacy) idea.
   */
  @Deprecated
  private List<String> getCommandLineArgs(Player player) {
    short numGames = Optional.ofNullable(player.getGlobalRating()).map(GlobalRating::getNumGames).orElse((short) 0);
    return Arrays.asList("/numgames", String.valueOf(numGames));
  }

  public void hostGame(Game game, Player player) {
    send(new HostGameResponse(game.getMapName()), player);
  }

  private void send(ServerResponse serverResponse, Player player) {
    ClientConnection clientConnection = player.getClientConnection();
    if (clientConnection == null) {
      throw new IllegalStateException("Player has no connection: " + player);
    }
    clientGateway.send(serverResponse, clientConnection);
  }

  private String resolveMod(Game game) {
    // FIXME implement
    return "faf";
  }
}
