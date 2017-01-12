package com.faforever.server.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "game_featuredMods")
@Data
@NoArgsConstructor
public class FeaturedMod {

  @Id
  @Column(name = "id")
  @GeneratedValue
  private byte id;

  @Column(name = "gamemod")
  private String technicalName;

  @Column(name = "description")
  private String description;

  @Column(name = "name")
  private String displayName;

  @Column(name = "publish")
  private boolean publish;

  @Column(name = "order")
  private short displayOrder;

  @Column(name = "git_url")
  private String gitUrl;

  @Column(name = "git_branch")
  private String gitBranch;

}
