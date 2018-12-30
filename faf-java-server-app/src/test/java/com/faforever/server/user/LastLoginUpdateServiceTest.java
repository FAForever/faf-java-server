package com.faforever.server.user;

import com.faforever.server.security.FafUserDetails;
import com.faforever.server.security.User;
import com.faforever.server.security.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LastLoginUpdateServiceTest {

  @Mock
  private UserRepository userRepository;

  private LastLoginUpdateService instance;

  @Before
  public void setUp() throws Exception {
    instance = new LastLoginUpdateService(userRepository);
  }

  @Test
  public void onApplicationEvent() {
    User user = (User) new User().setLogin("JUnit").setPassword("");
    Object principal = new FafUserDetails(user);

    Instant now = Instant.now();
    instance.onApplicationEvent(new AuthenticationSuccessEvent(new TestingAuthenticationToken(principal, "JUnit")));

    verify(userRepository).save(argThat(
      argument -> argument.getPlayer().getUser().getLastLogin().isAfter(now.minusSeconds(1)))
    );
  }
}
