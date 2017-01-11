package com.faforever.server.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "global_rating")
@Data
@NoArgsConstructor
public class GlobalRating {

  @Id
  @Column(name = "id")
  private int id;

  @Column(name = "mean")
  private Double mean;

  @Column(name = "deviation")
  private Double deviation;

  @Column(name = "numGames")
  private short numGames;

  @Column(name = "is_active")
  private boolean isActive;

  @OneToOne
  @JoinColumn(name = "id", updatable = false, insertable = false)
  private Player player;

}
