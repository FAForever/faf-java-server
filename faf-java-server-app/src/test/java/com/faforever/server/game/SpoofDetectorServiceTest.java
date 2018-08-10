package com.faforever.server.game;

import com.faforever.server.avatar.Avatar;
import com.faforever.server.avatar.AvatarAssociation;
import com.faforever.server.client.ClientService;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.player.Player;
import com.faforever.server.player.PlayerService;
import com.faforever.server.rating.GlobalRating;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Optional;

import static com.faforever.server.error.RequestExceptionWithCode.requestExceptionWithCode;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SpoofDetectorServiceTest {

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  private SpoofDetectorService instance;

  @Mock
  private PlayerService playerService;
  @Mock
  private ClientService clientService;

  @Before
  public void setUp() throws Exception {
    instance = new SpoofDetectorService(playerService, clientService);
  }

  @Test
  public void verifyPlayerReporterNotInGame() {
    expectedException.expect(requestExceptionWithCode(ErrorCode.THIS_PLAYER_NOT_IN_GAME));

    instance.verifyPlayer(new Player(), 1, "", 1, 1, null, null, null);
    verifyZeroInteractions(clientService);
  }

  @Test
  public void verifyPlayerPlayerNotOnline() {
    when(playerService.getOnlinePlayer(1)).thenReturn(Optional.empty());
    expectedException.expect(requestExceptionWithCode(ErrorCode.PLAYER_NOT_ONLINE));

    instance.verifyPlayer(new Player().setCurrentGame(new Game()), 1, "", 1, 1, null, null, null);
    verifyZeroInteractions(clientService);
  }

  @Test
  public void verifyPlayerReporteeNotInGame() {
    when(playerService.getOnlinePlayer(1)).thenReturn(Optional.of(new Player()));
    expectedException.expect(requestExceptionWithCode(ErrorCode.OTHER_PLAYER_NOT_IN_GAME));

    instance.verifyPlayer(new Player().setCurrentGame(new Game()), 1, "", 1, 1, null, null, null);
    verifyZeroInteractions(clientService);
  }

  @Test
  public void verifyPlayerReporterAndReporteeNotInSameGame() {
    when(playerService.getOnlinePlayer(1)).thenReturn(Optional.of(new Player().setCurrentGame(new Game(1))));
    expectedException.expect(requestExceptionWithCode(ErrorCode.NOT_SAME_GAME));

    instance.verifyPlayer(new Player().setCurrentGame(new Game(2)), 1, "", 1, 1, null, null, null);
    verifyZeroInteractions(clientService);
  }

  @Test
  public void verifyPlayerReporterNameMismatch() {
    Player reportee = (Player) new Player()
      .setCurrentGame(new Game(1))
      .setId(1)
      .setLogin("JUnit");
    when(playerService.getOnlinePlayer(1)).thenReturn(Optional.of(reportee));

    Player reporter = (Player) new Player().setCurrentGame(new Game(1)).setId(42);
    instance.verifyPlayer(reporter, reportee.getId(), "Fraud", 1, 1, null, null, null);

    assertThat(reportee.getFraudReporterIds(), hasSize(1));
    assertThat(reportee.getFraudReporterIds(), Matchers.contains(42));

    verifyZeroInteractions(clientService);
  }

  @Test
  public void verifyPlayerRatingMeanMismatch() {
    Player reportee = (Player) new Player()
      .setCurrentGame(new Game(1))
      .setId(1)
      .setLogin("JUnit");
    reportee.setRatingWithinCurrentGame(new GlobalRating(reportee, 51, 5));

    when(playerService.getOnlinePlayer(1)).thenReturn(Optional.of(reportee));

    Player reporter = (Player) new Player().setCurrentGame(new Game(1)).setId(42);
    instance.verifyPlayer(reporter, reportee.getId(), "JUnit", 50, 5, null, null, null);

    assertThat(reportee.getFraudReporterIds(), hasSize(1));
    assertThat(reportee.getFraudReporterIds(), Matchers.contains(42));

    verifyZeroInteractions(clientService);
  }

  @Test
  public void verifyPlayerGlobalRatingDeviationMismatch() {
    Player reportee = (Player) new Player()
      .setCurrentGame(new Game(1))
      .setId(1)
      .setLogin("JUnit");
    reportee.setRatingWithinCurrentGame(new GlobalRating(reportee, 50, 6));

    when(playerService.getOnlinePlayer(1)).thenReturn(Optional.of(reportee));

    Player reporter = (Player) new Player().setCurrentGame(new Game(1)).setId(42);
    instance.verifyPlayer(reporter, reportee.getId(), "JUnit", 50, 5, null, null, null);

    assertThat(reportee.getFraudReporterIds(), hasSize(1));
    assertThat(reportee.getFraudReporterIds(), Matchers.contains(42));

    verifyZeroInteractions(clientService);
  }

  @Test
  public void verifyPlayerCountry() {
    Player reportee = (Player) new Player()
      .setCurrentGame(new Game(1))
      .setCountry("CH")
      .setId(1)
      .setLogin("JUnit");
    reportee.setRatingWithinCurrentGame(new GlobalRating(reportee, 50, 5));

    when(playerService.getOnlinePlayer(1)).thenReturn(Optional.of(reportee));

    Player reporter = (Player) new Player().setCurrentGame(new Game(1)).setId(42);
    instance.verifyPlayer(reporter, reportee.getId(), "JUnit", 50, 5, "XX", null, null);

    assertThat(reportee.getFraudReporterIds(), hasSize(1));
    assertThat(reportee.getFraudReporterIds(), Matchers.contains(42));

    verifyZeroInteractions(clientService);
  }

  @Test
  public void verifyPlayerAvatarDescription() {
    Player reportee = (Player) new Player()
      .setCurrentGame(new Game(1))
      .setCountry("CH")
      .setId(1)
      .setLogin("JUnit");
    reportee.setRatingWithinCurrentGame(new GlobalRating(reportee, 50, 5));
    reportee.getAvailableAvatars().add(
      new AvatarAssociation()
        .setAvatar(new Avatar().setDescription("Description"))
        .setPlayer(reportee)
        .setSelected(true)
    );

    when(playerService.getOnlinePlayer(1)).thenReturn(Optional.of(reportee));

    Player reporter = (Player) new Player().setCurrentGame(new Game(1)).setId(42);
    instance.verifyPlayer(reporter, reportee.getId(), "JUnit", 50, 5, "CH", null, "Fake");

    assertThat(reportee.getFraudReporterIds(), hasSize(1));
    assertThat(reportee.getFraudReporterIds(), Matchers.contains(42));

    verifyZeroInteractions(clientService);
  }

  @Test
  public void verifyPlayerAvatarUrl() {
    Player reportee = (Player) new Player()
      .setCurrentGame(new Game(1))
      .setCountry("CH")
      .setId(1)
      .setLogin("JUnit");
    reportee.setRatingWithinCurrentGame(new GlobalRating(reportee, 50, 5));
    reportee.getAvailableAvatars().add(
      new AvatarAssociation()
        .setAvatar(new Avatar().setUrl("http://example.com/avatar.png"))
        .setPlayer(reportee)
        .setSelected(true)
    );

    when(playerService.getOnlinePlayer(1)).thenReturn(Optional.of(reportee));

    Player reporter = (Player) new Player().setCurrentGame(new Game(1)).setId(42);
    instance.verifyPlayer(reporter, reportee.getId(), "JUnit", 50, 5, "CH", "http://example.com/fake.png", null);

    assertThat(reportee.getFraudReporterIds(), hasSize(1));
    assertThat(reportee.getFraudReporterIds(), Matchers.contains(42));

    verifyZeroInteractions(clientService);
  }

  @Test
  public void verifyPlayerAllGood() {
    Player reportee = (Player) new Player()
      .setCurrentGame(new Game(1))
      .setCountry("CH")
      .setId(1)
      .setLogin("JUnit");
    reportee.setRatingWithinCurrentGame(new GlobalRating(reportee, 50, 5));
    reportee.getAvailableAvatars().add(
      new AvatarAssociation()
        .setAvatar(new Avatar().setUrl("http://example.com/avatar.png").setDescription("Description"))
        .setPlayer(reportee)
        .setSelected(true)
    );

    when(playerService.getOnlinePlayer(1)).thenReturn(Optional.of(reportee));

    Player reporter = (Player) new Player().setCurrentGame(new Game(1)).setId(42);
    instance.verifyPlayer(reporter, reportee.getId(), "JUnit", 50, 5, "CH", "http://example.com/avatar.png", "Description");

    assertThat(reportee.getFraudReporterIds(), empty());

    verifyZeroInteractions(clientService);
  }

  @Test
  public void verifyPlayerFakeNameAndMultipleReports() {
    Game reporteeGame = new Game(1);

    Player reportee = (Player) new Player()
      .setCurrentGame(reporteeGame)
      .setCountry("CH")
      .setId(1)
      .setLogin("JUnit");
    reportee.setRatingWithinCurrentGame(new GlobalRating(reportee, 50, 5));
    reportee.getAvailableAvatars().add(
      new AvatarAssociation()
        .setAvatar(new Avatar().setUrl("http://example.com/avatar.png").setDescription("Description"))
        .setPlayer(reportee)
        .setSelected(true)
    );

    when(playerService.getOnlinePlayer(1)).thenReturn(Optional.of(reportee));

    Player reporter1 = (Player) new Player().setCurrentGame(reporteeGame).setId(42);
    Player reporter2 = (Player) new Player().setCurrentGame(reporteeGame).setId(43);

    reporteeGame.getConnectedPlayers().put(reportee.getId(), reportee);
    reporteeGame.getConnectedPlayers().put(reporter1.getId(), reporter1);
    reporteeGame.getConnectedPlayers().put(reporter2.getId(), reporter2);

    instance.verifyPlayer(reporter1, reportee.getId(), "Fake", 50, 5, "CH", "http://example.com/avatar.png", "Description");
    instance.verifyPlayer(reporter2, reportee.getId(), "Fake", 50, 5, "CH", "http://example.com/avatar.png", "Description");

    assertThat(reportee.getFraudReporterIds(), hasSize(2));
    assertThat(reportee.getFraudReporterIds(), containsInAnyOrder(42, 43));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Collection<Player>> argumentCaptor = ArgumentCaptor.forClass(Collection.class);
    verify(clientService).disconnectPlayerFromGame(eq(reportee.getId()), argumentCaptor.capture());

    assertThat(argumentCaptor.getValue(), containsInAnyOrder(reporter1, reporter2));
  }
}
