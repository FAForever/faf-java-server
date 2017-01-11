package com.faforever.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "ladder1v1_rating")
@Data
@NoArgsConstructor
public class Ladder1v1Rating {

  @Id
  @Column(name = "id")
  private int id;

  @Column(name = "mean")
  private Double mean;

  @Column(name = "deviation")
  private Double deviation;

  @Column(name = "numGames")
  private short numGames;

  @Column(name = "winGames")
  private short winGames;

  @Column(name = "is_active")
  private boolean isActive;

  @OneToOne
  @JoinColumn(name = "id", updatable = false, insertable = false)
  @JsonIgnore
  private Player player;

}
