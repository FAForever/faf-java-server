package com.faforever.server.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "lobby_admin")
@Data
@NoArgsConstructor
public class UserGroup {

  @Id
  @Column(name = "user_id")
  private int id;

  @OneToOne
  @JoinColumn(name = "user_id", updatable = false, insertable = false)
  private User user;

  @Basic
  @Column(name = "group")
  private int group;
}
