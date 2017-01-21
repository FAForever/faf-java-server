package com.faforever.server.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

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
