package com.faforever.server.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "map_version")
@Immutable
@Data
@NoArgsConstructor
public class MapVersion {

  @Id
  @Column(name = "id")
  @GeneratedValue
  private int id;

  @Column(name = "description")
  private String description;

  @Column(name = "max_players")
  private Integer maxPlayers;

  @Column(name = "width")
  private int width;

  @Column(name = "height")
  private int height;

  @Column(name = "version")
  private int version;

  @Column(name = "filename")
  private String filename;

  @Column(name = "ranked")
  private boolean ranked;

  @Column(name = "hidden")
  private boolean hidden;

  @Column(name = "create_time")
  private Timestamp createTime;

  @Column(name = "update_time")
  private Timestamp updateTime;

}
