package com.faforever.server.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name = "login")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@ToString(exclude = "password", callSuper = true)
public class User extends Login {

  @Column(name = "password")
  private String password;

  @OneToOne
  @JoinColumn(name = "id", insertable = false, updatable = false)
  private Player player;

}
