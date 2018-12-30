package com.faforever.server.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class FafUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

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
