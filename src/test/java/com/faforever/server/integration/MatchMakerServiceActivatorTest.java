package com.faforever.server.integration;

import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
import com.faforever.server.game.Faction;
import com.faforever.server.matchmaker.MatchMakerCancelRequest;
import com.faforever.server.matchmaker.MatchMakerSearchRequest;
import com.faforever.server.matchmaker.MatchMakerService;
import com.faforever.server.security.FafUserDetails;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MatchMakerServiceActivatorTest {
  private static final String LADDER_1V1 = "ladder1v1";
  private MatchMakerServiceActivator instance;

  @Mock
  private MatchMakerService matchmakerService;

  private Player player;
  private Authentication authentication;

  @Before
  public void setUp() throws Exception {
    player = new Player();

    authentication = new TestingAuthenticationToken(new FafUserDetails((User) new User()
      .setPlayer(player).setPassword("pw").setLogin("JUnit")), null);

    instance = new MatchMakerServiceActivator(matchmakerService);
  }

  @Test
  public void startSearch() throws Exception {
    instance.startSearch(new MatchMakerSearchRequest(Faction.CYBRAN, LADDER_1V1), authentication);
    verify(matchmakerService).submitSearch(player, Faction.CYBRAN, LADDER_1V1);
  }

  @Test
  public void cancelSearch() throws Exception {
    instance.cancelSearch(new MatchMakerCancelRequest(LADDER_1V1), authentication);
    verify(matchmakerService).cancelSearch(LADDER_1V1, player);
  }
}
