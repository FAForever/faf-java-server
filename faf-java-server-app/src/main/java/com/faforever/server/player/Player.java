package com.faforever.server.player;

import com.faforever.server.avatar.Avatar;
import com.faforever.server.clan.Clan;
import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ConnectionAware;
import com.faforever.server.game.Game;
import com.faforever.server.game.GameState;
import com.faforever.server.game.PlayerGameState;
import com.faforever.server.ladder1v1.Ladder1v1Rating;
import com.faforever.server.matchmaker.MatchMakerBanDetails;
import com.faforever.server.rating.GlobalRating;
import com.faforever.server.rating.Rating;
import com.faforever.server.security.Login;
import com.faforever.server.security.User;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.keyvalue.annotation.KeySpace;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Entity
@Table(name = "login")
@Getter
@Setter
@KeySpace("player")
public class Player extends Login implements ConnectionAware {

  @OneToOne(mappedBy = "player", fetch = FetchType.EAGER)
  @Nullable
  private Ladder1v1Rating ladder1v1Rating;

  @OneToOne(mappedBy = "player", fetch = FetchType.EAGER)
  @Nullable
  private GlobalRating globalRating;

  @OneToOne(mappedBy = "player", fetch = FetchType.EAGER)
  private MatchMakerBanDetails matchMakerBanDetails;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "id", insertable = false, updatable = false)
  private User user;

  @ManyToOne
  @JoinColumnsOrFormulas({
    @JoinColumnOrFormula(formula = @JoinFormula(value = "(select a.idAvatar from avatars a where a.idUser = id and a.selected = 1)", referencedColumnName = "id")),
  })
  private Avatar avatar;

  @Transient
  @Nullable
  private Game currentGame;

  @Transient
  private PlayerGameState gameState = PlayerGameState.NONE;

  @Transient
  private ClientConnection clientConnection;

  /** ID of players who reported that this player spoofed their data. */
  @Transient
  private Set<Integer> fraudReporterIds = new HashSet<>();

  /**
   * The future that will be completed as soon as the player's game entered {@link GameState#OPEN}. A player's game may
   * never start if it crashes or the player disconnects.
   */
  @Transient
  private CompletableFuture<Game> gameFuture;

  /** The player's rating for the game he joined, at the time he joined. */
  @Transient
  private Rating ratingWithinCurrentGame;

  @ManyToOne
  @JoinColumnsOrFormulas({
    @JoinColumnOrFormula(formula = @JoinFormula(value = "(select cm.clan_id from clan_membership cm where cm.player_id = id)", referencedColumnName = "id")),
  })
  private Clan clan;

  public void setGameState(PlayerGameState gameState) {
    PlayerGameState.verifyTransition(this.gameState, gameState);
    this.gameState = gameState;
  }

  @Override
  public String toString() {
    return "Player(" + getId() + ", " + getLogin() + ")";
  }
}
