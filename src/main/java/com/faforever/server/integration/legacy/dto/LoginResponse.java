package com.faforever.server.integration.legacy.dto;

import com.faforever.server.response.ServerResponse;
import com.faforever.server.security.FafUserDetails;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LoginResponse implements ServerResponse {

  private final FafUserDetails userDetails;
}
