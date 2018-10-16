package com.faforever.server.config.security;

import com.faforever.server.player.Player;
import com.faforever.server.player.PlayerRepository;
import com.faforever.server.security.User;
import com.faforever.server.security.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserCreatingAuthenticationProviderTest {

  private UserCreatingAuthenticationProvider instance;

  @Mock
  private UserRepository userRepository;
  @Mock
  private PlayerRepository playerRepository;

  @Before
  public void setUp() throws Exception {
    instance = new UserCreatingAuthenticationProvider(userRepository, playerRepository);

    when(userRepository.save(any())).thenAnswer(invocation -> ((User) invocation.getArgument(0)).setId(1));
    when(playerRepository.findById(any())).thenAnswer(invocation -> Optional.of(new Player().setId(invocation.getArgument(0))));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void test() {
    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("junit", "password");
    instance.retrieveUser("junit", authentication);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());

    User user = userCaptor.getValue();
    assertThat(user.getLogin(), is(authentication.getPrincipal()));
    assertThat(user.getPassword(), is(authentication.getCredentials()));
    assertThat(user.getEMail(), is("junit@example.com"));
  }
}
