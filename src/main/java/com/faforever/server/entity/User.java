package com.faforever.server.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "login")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class User extends Login {

  @Column(name = "password")
  private String password;

  @OneToOne
  @JoinColumn(name = "id", insertable = false, updatable = false)
  private Player player;

  @OneToOne(mappedBy = "user")
  private UserGroup userGroup;

  @Override
  public String toString() {
    return "User(" + getId() + ", " + getLogin() + ")";
  }
}
