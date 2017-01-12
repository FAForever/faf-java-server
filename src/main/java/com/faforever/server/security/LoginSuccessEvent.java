package com.faforever.server.security;

import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@Getter
@ToString
public class LoginSuccessEvent extends ApplicationEvent {
  private final FafUserDetails fafUserDetails;

  public LoginSuccessEvent(Object source, FafUserDetails fafUserDetails) {
    super(source);
    this.fafUserDetails = fafUserDetails;
  }
}
