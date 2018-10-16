package com.faforever.server.config.security;

import com.faforever.server.player.Player;
import com.faforever.server.player.PlayerRepository;
import com.faforever.server.security.FafUserDetails;
import com.faforever.server.security.User;
import com.faforever.server.security.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Slf4j
@ConditionalOnProperty(value = "faf-server.disable-authentication")
@Service
class UserCreatingAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

  private final UserRepository userRepository;
  private final PlayerRepository playerRepository;

  UserCreatingAuthenticationProvider(UserRepository userRepository, PlayerRepository playerRepository) {
    this.userRepository = userRepository;
    this.playerRepository = playerRepository;
    setPreAuthenticationChecks(new FafAccountBannedChecker());
  }

  private User updatePassword(User user, String password) {
    user.setPassword(password);
    return userRepository.save(user);
  }

  private User createUser(String username, String credentials) {
    log.debug("User '{}' does not yet exist, creating", username);

    User user = new User();
    user.setLogin(username);
    user.setEMail(username + "@example.com");
    user.setPassword(credentials);
    user = userRepository.save(user);

    Integer userId = user.getId();
    Player player = playerRepository.findById(userId)
      .orElseThrow(() -> new IllegalStateException("User with id " + userId + " could not be found"));

    user.setPlayer(player);
    return user;
  }

  @Override
  protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
    // Does nothing
  }

  @Override
  protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
    User user = userRepository.findOneByLoginIgnoreCase(username)
      .map(u -> updatePassword(u, String.valueOf(authentication.getCredentials())))
      .orElseGet(() -> createUser(username, String.valueOf(authentication.getCredentials())));

    return new FafUserDetails(user);
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
