package com.faforever.server.security;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ConnectionAware;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;

public class FafClientDetails extends BaseClientDetails implements ConnectionAware {
  private ClientConnection clientConnection;

  FafClientDetails(OAuthClient oAuthClient, String scopes, String redirectUris) {
    super(oAuthClient.getId(),
      null,
      scopes,
      "client_credentials",
      "",
      redirectUris);
  }

  @Override
  public ClientConnection getClientConnection() {
    return clientConnection;
  }

  public void setClientConnection(ClientConnection clientConnection) {
    this.clientConnection = clientConnection;
  }
}
