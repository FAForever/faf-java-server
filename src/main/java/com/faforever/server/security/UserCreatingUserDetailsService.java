package com.faforever.server.security;

import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
import com.faforever.server.player.PlayerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Creates users if they don't already exist. Not for production use, only for testing environments.
 */
@Service
@ConditionalOnProperty(value = "faf-server.disable-authentication")
@Slf4j
public class UserCreatingUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;
  private final PlayerRepository playerRepository;

  @Inject
  public UserCreatingUserDetailsService(UserRepository userRepository, PlayerRepository playerRepository) {
    this.userRepository = userRepository;
    this.playerRepository = playerRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findOneByLoginIgnoreCase(username)
      .orElseGet(() -> createUser(username));

    return new FafUserDetails(user);
  }

  private User createUser(String username) {
    log.debug("User '{}' does not yet exist, creating", username);

    User user = new User();
    user.setLogin(username);
    user.setEMail(username + "@example.com");
    user.setPassword("");
    user = userRepository.save(user);

    Integer userId = user.getId();
    Player player = playerRepository.findById(userId)
      .orElseThrow(() -> new IllegalStateException("User with id " + userId + " could not be found"));

    user.setPlayer(player);
    return user;
  }
}
