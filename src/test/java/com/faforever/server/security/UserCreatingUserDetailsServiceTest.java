package com.faforever.server.security;

import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
import com.faforever.server.player.PlayerRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserCreatingUserDetailsServiceTest {

  private UserCreatingUserDetailsService instance;
  @Mock
  private UserRepository userRepository;
  @Mock
  private PlayerRepository playerRepository;

  @Before
  public void setUp() throws Exception {
    instance = new UserCreatingUserDetailsService(userRepository, playerRepository);

    when(userRepository.save(any())).thenAnswer(invocation -> ((User) invocation.getArgument(0)).setId(1));
    when(playerRepository.findById(any())).thenAnswer(invocation -> Optional.of(new Player().setId(invocation.getArgument(0))));
  }

  @Test
  public void loadUserByUsername() {
    instance.loadUserByUsername("junit");

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());

    User user = captor.getValue();
    assertThat(user.getLogin(), is("junit"));
    assertThat(user.getPassword(), is(""));
    assertThat(user.getEMail(), is("junit@example.com"));
  }
}
