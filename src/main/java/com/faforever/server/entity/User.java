package com.faforever.server.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "login")
@Getter
@Setter
@NoArgsConstructor
public class User extends Login {

  @Column(name = "password")
  private String password;

  @OneToOne
  @JoinColumn(name = "id", insertable = false, updatable = false)
  private Player player;

  @OneToOne(mappedBy = "user")
  private GroupAssociation groupAssociation;

  @Override
  public String toString() {
    return "User(" + (player != null ? player.getId() : null) + ", " + getLogin() + ")";
  }
}
