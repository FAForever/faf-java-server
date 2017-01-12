package com.faforever.server.security;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ConnectionAware;
import com.faforever.server.entity.BanDetails;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
import lombok.ToString;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.sql.Timestamp;
import java.time.Instant;

import static java.util.Collections.singletonList;

@ToString
public class FafUserDetails extends org.springframework.security.core.userdetails.User implements ConnectionAware {

  private final User user;

  public FafUserDetails(User user) {
    // TODO implement lobby_admin
    super(user.getLogin(), user.getPassword(), true, true, true, isNonLocked(user.getBanDetails()), singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    this.user = user;
  }

  private static boolean isNonLocked(BanDetails banDetails) {
    return banDetails == null
      || banDetails.getExpiresAt().before(Timestamp.from(Instant.now()));
  }

  public User getUser() {
    return user;
  }

  public Player getPlayer() {
    return user.getPlayer();
  }

  @Override
  public ClientConnection getClientConnection() {
    return user.getPlayer().getClientConnection();
  }
}
