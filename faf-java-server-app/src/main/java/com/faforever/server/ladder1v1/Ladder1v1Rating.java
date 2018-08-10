package com.faforever.server.ladder1v1;

import com.faforever.server.player.Player;
import com.faforever.server.rating.Rating;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ladder1v1_rating")
@Getter
@Setter
@NoArgsConstructor
public class Ladder1v1Rating extends Rating {

  @Column(name = "winGames")
  private short winGames;

  public Ladder1v1Rating(Player player, double mean, double deviation) {
    super(player.getId(), 0, true, mean, deviation, player);
  }
}
