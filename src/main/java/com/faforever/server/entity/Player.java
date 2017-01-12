package com.faforever.server.entity;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ConnectionAware;
import com.faforever.server.game.GameState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "login")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, of = {"currentGame", "gameState"})
public class Player extends Login implements ConnectionAware {

  @OneToOne(mappedBy = "player")
  @Nullable
  private Ladder1v1Rating ladder1v1Rating;

  @OneToOne(mappedBy = "player")
  @Nullable
  private GlobalRating globalRating;

  @Transient
  private Game currentGame;

  @Transient
  private GameState gameState;

  @Transient
  private ClientConnection clientConnection;

}
