package com.faforever.server.integration;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.game.SpoofDetectorService;
import com.faforever.server.game.VerifyPlayerReport;
import com.faforever.server.player.Player;
import com.faforever.server.security.FafUserDetails;
import com.faforever.server.security.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.net.InetAddress;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SpoofDetectorServiceActivatorTest {

  private SpoofDetectorServiceActivator instance;
  private ClientConnection clientConnection;
  private Player reporter;

  @Mock
  private SpoofDetectorService spoofDetectorService;

  @Before
  public void setUp() throws Exception {
    clientConnection = new ClientConnection("1", Protocol.V1_LEGACY_UTF_16, mock(InetAddress.class));
    reporter = new Player();
    User user = (User) new User().setPlayer(reporter).setPassword("password").setLogin("JUnit");
    clientConnection.setAuthentication(new TestingAuthenticationToken(new FafUserDetails(user), null));

    instance = new SpoofDetectorServiceActivator(spoofDetectorService);
  }

  @Test
  public void verifyPlayerReport() {
    instance.verifyPlayerReport(new VerifyPlayerReport(
      123, "JUnit", 123.45f, 543.21f, "CH", "http://example.com/avatar.png", "Avatar Description"
    ), clientConnection.getAuthentication());

    verify(spoofDetectorService).verifyPlayer(reporter, 123, "JUnit", 123.45f, 543.21f, "CH", "http://example.com/avatar.png", "Avatar Description");
  }
}
