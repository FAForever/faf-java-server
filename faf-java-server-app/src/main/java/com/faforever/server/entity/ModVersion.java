package com.faforever.server.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "mod_version")
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class ModVersion {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mod_id")
  private Mod mod;
}
