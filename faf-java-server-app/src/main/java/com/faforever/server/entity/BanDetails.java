package com.faforever.server.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "ban")
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class BanDetails {

  @Id
  @Column(name = "id")
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
  @Enumerated(EnumType.STRING)
  private BanScope scope;

  @Formula(value = "(SELECT count(1) FROM ban_revoke WHERE ban_revoke.ban_id = id)")
  private boolean revoked;

  public enum BanScope {
    CHAT, GLOBAL
  }
}
