package com.faforever.server.config.security;

import com.faforever.server.security.FafUserDetails;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.security.authentication.LockedException;

@EqualsAndHashCode(callSuper = true)
@Value
public class FafLockedException extends LockedException {
  FafUserDetails fafUserDetails;

  public FafLockedException(FafUserDetails fafUserDetails) {
    super("Account " + fafUserDetails.getUsername() + " is locked");
    this.fafUserDetails = fafUserDetails;
  }
}
