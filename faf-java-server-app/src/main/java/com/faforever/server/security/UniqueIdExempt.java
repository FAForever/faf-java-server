package com.faforever.server.security;

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
@Table(name = "uniqueid_exempt")
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class UniqueIdExempt {

  @Id
  @Column(name = "user_id")
  private Integer id;

  @OneToOne
  @JoinColumn(name = "user_id", updatable = false, insertable = false)
  private Player player;

  @Column(name = "reason")
  private String reason;

}
