package com.faforever.server.entity;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ConnectionAware;
import com.faforever.server.game.PlayerGameState;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "login")
@Getter
@Setter
public class Player extends Login implements ConnectionAware {

  @OneToOne(mappedBy = "player")
  @Nullable
  private Ladder1v1Rating ladder1v1Rating;

  @OneToOne(mappedBy = "player")
  @Nullable
  private GlobalRating globalRating;

  @ManyToMany
  @JoinTable(name = "unique_id_users",
    joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
    inverseJoinColumns = @JoinColumn(name = "uniqueid_hash", referencedColumnName = "id"))
  private Set<HardwareInformation> hardwareInformations;

  @OneToOne(mappedBy = "player")
  private MatchMakerBanDetails matchMakerBanDetails;

  @ManyToMany
  @JoinTable(name = "avatars",
    joinColumns = @JoinColumn(name = "idUser", referencedColumnName = "id"),
    inverseJoinColumns = @JoinColumn(name = "idAvatar", referencedColumnName = "id"))
  private List<AvatarAssociation> availableAvatars;

  @Transient
  private Game currentGame;

  @Transient
  private PlayerGameState gameState = PlayerGameState.NONE;

  @Transient
  private ClientConnection clientConnection;

  public void setGameState(PlayerGameState gameState) {
    PlayerGameState.verifyTransition(this.gameState, gameState);
    this.gameState = gameState;
  }

  @Override
  public String toString() {
    return "Player(" + getId() + ", " + getLogin() + ")";
  }
}
