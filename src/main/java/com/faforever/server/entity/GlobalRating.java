package com.faforever.server.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "global_rating")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class GlobalRating extends Rating {
  public GlobalRating(Player player, double mean, double deviation) {
    super(player.getId(), 0, true, mean, deviation, player);
  }
}
