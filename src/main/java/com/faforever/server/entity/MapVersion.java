package com.faforever.server.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "map_version")
@Immutable
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class MapVersion {
  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

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
