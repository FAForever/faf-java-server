package com.faforever.server.integration;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
import com.faforever.server.game.Faction;
import com.faforever.server.matchmaker.MatchMakerSearchRequest;
import com.faforever.server.matchmaker.MatchMakerService;
import com.faforever.server.security.FafUserDetails;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MatchMakerServiceActivatorTest {
  private MatchMakerServiceActivator instance;

  @Mock
  private MatchMakerService matchmakerService;

  private ClientConnection clientConnection;
  private Player player;

  @Before
  public void setUp() throws Exception {
    player = new Player();
    player.setClientConnection(clientConnection);

    clientConnection = new ClientConnection("1", Protocol.LEGACY_UTF_16)
      .setUserDetails(new FafUserDetails((User) new User().setPlayer(player).setPassword("pw").setLogin("JUnit")));

    instance = new MatchMakerServiceActivator(matchmakerService);
  }

  @Test
  public void startSearch() throws Exception {
    instance.startSearch(new MatchMakerSearchRequest(Faction.CYBRAN, "ladder1v1"), clientConnection);
    verify(matchmakerService).submitSearch(player, Faction.CYBRAN, "ladder1v1");
  }
}
