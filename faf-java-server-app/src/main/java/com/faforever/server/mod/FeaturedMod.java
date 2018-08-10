package com.faforever.server.mod;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "game_featuredMods")
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class FeaturedMod {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "gamemod")
  private String technicalName;

  @Column(name = "description")
  private String description;

  @Column(name = "name")
  private String displayName;

  @Column(name = "publish")
  private boolean publish;

  @Column(name = "\"order\"")
  private int displayOrder;

  @Column(name = "git_url")
  private String gitUrl;

  @Column(name = "git_branch")
  private String gitBranch;

}
