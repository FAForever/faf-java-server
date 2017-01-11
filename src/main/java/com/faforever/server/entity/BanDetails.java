package com.faforever.server.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "lobby_ban")
@Data
@NoArgsConstructor
public class BanDetails {

  @Id
  @Column(name = "idUser")
  private int id;

  @OneToOne
  @JoinColumn(name = "idUser", updatable = false)
  private Player player;

  @Column(name = "reason")
  private String reason;

  @Column(name = "expires_at")
  private Timestamp expiresAt;

}
