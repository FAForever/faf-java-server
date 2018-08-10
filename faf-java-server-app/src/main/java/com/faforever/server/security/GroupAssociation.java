package com.faforever.server.security;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "lobby_admin")
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class GroupAssociation {

  @Id
  @Column(name = "user_id")
  private Integer id;

  @OneToOne
  @JoinColumn(name = "user_id", updatable = false, insertable = false)
  private User user;

  @Basic
  @Column(name = "\"group\"")
  @Enumerated(EnumType.ORDINAL)
  private Group group;

  public enum Group {
    // Order is crucial
    INVALID,
    ADMIN,
    MODERATOR
  }
}
