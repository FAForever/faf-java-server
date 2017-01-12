package com.faforever.server.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "mod_version")
@Data
@NoArgsConstructor
public class ModVersion {
  @Id
  @Column(name = "id")
  @GeneratedValue
  private int id;

  @Column(name = "uid")
  private String uid;

  @Column(name = "type")
  private ModType type;

  @Column(name = "description")
  private String description;

  @Column(name = "version")
  private short version;

  @Column(name = "filename")
  private String filename;

  @Column(name = "icon")
  private String icon;

  @Column(name = "ranked")
  private boolean ranked;

  @Column(name = "hidden")
  private boolean hidden;

  @Column(name = "create_time")
  private Timestamp createTime;

  @Column(name = "update_time")
  private Timestamp updateTime;
}
