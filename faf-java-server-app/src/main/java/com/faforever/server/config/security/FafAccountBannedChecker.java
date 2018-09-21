package com.faforever.server.config.security;

import com.faforever.server.security.FafUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.util.Assert;

@Slf4j
public class FafAccountBannedChecker implements UserDetailsChecker {

  @Override
  public void check(UserDetails user) {
    Assert.isInstanceOf(FafUserDetails.class, user);

    FafUserDetails fafUserDetails = (FafUserDetails) user;

    if (!fafUserDetails.isAccountNonLocked()) {
      log.debug("User '{}' is locked", fafUserDetails.getUsername());
      throw new FafLockedException(fafUserDetails);
    }
  }
}
