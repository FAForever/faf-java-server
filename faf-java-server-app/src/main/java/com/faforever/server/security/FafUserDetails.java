package com.faforever.server.security;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ConnectionAware;
import com.faforever.server.entity.BanDetails;
import com.faforever.server.entity.GroupAssociation.Group;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.error.Requests;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ToString
public class FafUserDetails extends org.springframework.security.core.userdetails.User implements ConnectionAware {

  private final User user;

  public FafUserDetails(@NotNull User user) {
    super(user.getLogin(), user.getPassword(), true, true, true, isNonLocked(user.getBanDetails()), getRoles(user));
    this.user = user;
  }

  /**
   * Returns the underlying {@link Player} or throws a {@link com.faforever.server.error.RequestException} if this user
   * does not have a player.
   */
  public Player getPlayer() {
    Optional<Player> playerOptional = Optional.ofNullable(user).map(User::getPlayer);
    Requests.verify(playerOptional.isPresent(), ErrorCode.NOT_A_PLAYER);

    return playerOptional.get();
  }

  @Override
  public ClientConnection getClientConnection() {
    return user.getClientConnection();
  }

  public void setClientConnection(ClientConnection clientConnection) {
    user.setClientConnection(clientConnection);
  }

  @NotNull
  private static List<GrantedAuthority> getRoles(User user) {
    ArrayList<GrantedAuthority> roles = new ArrayList<>();
    roles.add(new SimpleGrantedAuthority("ROLE_USER"));

    if (user.getGroupAssociation() != null && user.getGroupAssociation().getGroup() == Group.ADMIN) {
      roles.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    return roles;
  }

  private static boolean isNonLocked(Set<BanDetails> banDetails) {
    return banDetails.stream()
      .noneMatch(details ->
        !details.isRevoked()
          && (details.getExpiresAt() == null || details.getExpiresAt().after(Timestamp.from(Instant.now())))
      );
  }
}
