package com.faforever.server.security;

import com.faforever.server.response.ServerResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public class UserDetailsResponse implements ServerResponse {

  private final FafUserDetails userDetails;
}
