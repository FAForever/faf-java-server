package com.faforever.server.security;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;

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
  @NotNull
  private User user;

  @ManyToOne
  @JoinColumn(name = "author_id")
  private User author;

  @Column(name = "reason")
  @NotNull
  private String reason;

  @Column(name = "expires_at")
  private Timestamp expiresAt;

  @Column(name = "level")
  @Enumerated(EnumType.STRING)
  private BanScope scope;

  @Column(name = "revoke_reason")
  private String revokeReason;

  @ManyToOne
  @JoinColumn(name = "revoke_author_id")
  private User revokeAuthor;

  @Column(name = "revoke_time")
  private OffsetDateTime revokeTime;

  public boolean isActive() {
    return (revokeTime == null || revokeTime.isAfter(OffsetDateTime.now()))
      && (expiresAt == null || expiresAt.after(Timestamp.from(Instant.now())));
  }

  public enum BanScope {
    CHAT, GLOBAL
  }
}
