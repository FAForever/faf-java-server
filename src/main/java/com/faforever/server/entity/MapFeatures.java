package com.faforever.server.entity;

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
public class MapFeatures { //TODO rename to MapStats?
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

  // TODO find out what the 'voters' column is, and model it accordingly
  @Column(name = "voters")
  private String voters = "";

  public void incrementTimesPlayed(){
    timesPlayed++;
  }
}

