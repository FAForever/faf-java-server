package com.faforever.server.security;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ConnectionAware;
import com.faforever.server.player.Player;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "login")
@Getter
@Setter
@NoArgsConstructor
public class User extends Login implements ConnectionAware {

  @OneToOne
  @JoinColumn(name = "id", insertable = false, updatable = false, nullable = false)
  private Player player;

  @OneToOne(mappedBy = "user")
  private GroupAssociation groupAssociation;

  @Override
  public String toString() {
    return "User(" + (player != null ? player.getId() : null) + ", " + getLogin() + ")";
  }

  @Override
  public ClientConnection getClientConnection() {
    return player.getClientConnection();
  }

  public void setClientConnection(ClientConnection clientConnection) {
    player.setClientConnection(clientConnection);
  }
}
