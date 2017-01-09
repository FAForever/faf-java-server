package com.faforever.server.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "map_version")
@Immutable
@EqualsAndHashCode
@Getter
public class MapVersion {

  @Id
  @Column(name = "id")
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
  private byte ranked;

  @Column(name = "hidden")
  private byte hidden;

  @Column(name = "create_time")
  private Timestamp createTime;

  @Column(name = "update_time")
  private Timestamp updateTime;

}
