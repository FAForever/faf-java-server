package com.faforever.server.matchmaker;

import com.faforever.server.player.Player;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode(of = "id")
public class MatchMakerBanDetails {

  @Id
  @Column(name = "userid")
  private Integer id;

  @OneToOne
  @JoinColumn(name = "userid", updatable = false, insertable = false)
  private Player player;

}
