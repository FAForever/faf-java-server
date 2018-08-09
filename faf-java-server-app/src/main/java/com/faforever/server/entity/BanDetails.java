package com.faforever.server.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "lobby_ban")
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class BanDetails {

  @Id
  @Column(name = "idUser")
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "player_id")
  private User user;

  @ManyToOne
  @JoinColumn(name = "author_id")
  private User author;

  @Column(name = "reason")
  private String reason;

  @Column(name = "expires_at")
  private Timestamp expiresAt;

  @Column(name = "level")
  private BanScope scope;

  public enum BanScope {
    CHAT, GLOBAL
  }
}
