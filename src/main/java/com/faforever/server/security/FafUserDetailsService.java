package com.faforever.server.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
@ConditionalOnProperty(value = "faf-server.disable-authentication", matchIfMissing = true, havingValue = "false")
public class FafUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Inject
  public FafUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository.findOneByLoginIgnoreCase(username)
      .map(FafUserDetails::new)
      .orElseThrow(() -> new UsernameNotFoundException("User could not be found: " + username));
  }
}
