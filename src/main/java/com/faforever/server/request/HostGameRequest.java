package com.faforever.server.request;

import lombok.Getter;

@Getter
public class HostGameRequest extends ClientRequest {

  private String mapId;
  private String title;
  private String mod;
  private GameAccess access;
  private Integer version;
  private String password;
  private GameVisibility visibility;

  public HostGameRequest(String mapId, String title, String mod, GameAccess access, Integer version, String password, GameVisibility visibility) {
    this.mapId = mapId;
    this.title = title;
    this.mod = mod;
    this.access = access;
    this.version = version;
    this.password = password;
    this.visibility = visibility;
  }
}
