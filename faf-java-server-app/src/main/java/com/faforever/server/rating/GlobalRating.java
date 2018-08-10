package com.faforever.server.rating;

import com.faforever.server.player.Player;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "global_rating")
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GlobalRating extends Rating {
  public GlobalRating(Player player, double mean, double deviation) {
    super(player.getId(), 0, true, mean, deviation, player);
  }
}
