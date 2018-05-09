package com.faforever.server.integration.v2.server;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AvatarsServerMessage {

  List<Avatar> avatars;

  @Data
  public static class Avatar {
    String url;
    String description;
  }
}
