package com.faforever.server.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * A login service that directly accesses the database in order to verify the login name and sha256-hashed password.
 * It's called legacy because in future login should be provided via API and requests should send the JWT token.
 */
@Service
public class LegacyUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Inject
  public LegacyUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return new FafUserDetails(userRepository.findOneByLogin(username));
  }
}
