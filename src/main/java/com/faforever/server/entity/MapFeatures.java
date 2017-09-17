package com.faforever.server.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "table_map_features")
@Immutable
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class MapFeatures {
  @Id
  @Column(name = "map_id")
  private int id;

  @Column(name = "times_played")
  private int timesPlayed;

  @Column(name = "num_draws")
  private int draws;

  @Column(name = "downloads")
  private int downloads;

  @Column(name = "rating")
  private double rating;

  // TODO find out what the 'voters' column is, and model it accordingly

  public void incrementTimesPlayed(){
    timesPlayed++;
  }
}

