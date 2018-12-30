package com.faforever.server.user;

import com.faforever.server.security.FafUserDetails;
import com.faforever.server.security.User;
import com.faforever.server.security.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

//@Component
@Slf4j
public class LastLoginUpdateService implements ApplicationListener<AuthenticationSuccessEvent> {

  private final UserRepository userRepository;

  public LastLoginUpdateService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void onApplicationEvent(@NotNull AuthenticationSuccessEvent event) {
    Object principal = event.getAuthentication().getPrincipal();
    if (!(principal instanceof FafUserDetails)) {
      log.debug("Not an instance of {}: {}", FafUserDetails.class, principal);
      return;
    }

    FafUserDetails fafUserDetails = (FafUserDetails) event.getAuthentication().getPrincipal();
    User user = fafUserDetails.getUser();
    user.setLastLogin(Instant.now());

    userRepository.updateLastLogin(user, Instant.now());
  }
}
