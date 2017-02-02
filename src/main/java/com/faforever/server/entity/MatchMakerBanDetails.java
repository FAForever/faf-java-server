package com.faforever.server.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "matchmaker_ban")
@Data
@NoArgsConstructor
public class MatchMakerBanDetails {

  @Id
  @Column(name = "userid")
  private int id;

  @OneToOne
  @JoinColumn(name = "userid", updatable = false)
  private Player player;

}
