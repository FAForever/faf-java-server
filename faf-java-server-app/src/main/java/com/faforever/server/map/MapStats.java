package com.faforever.server.map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "table_map_features")
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class MapStats {
  @Id
  @Column(name = "map_id")
  private Integer id;

  @Column(name = "times_played")
  private int timesPlayed;

  @Column(name = "num_draws")
  private int draws;

  @Column(name = "downloads")
  private int downloads;

  @Column(name = "rating")
  private double rating;

  public void incrementTimesPlayed() {
    timesPlayed++;
  }
}

