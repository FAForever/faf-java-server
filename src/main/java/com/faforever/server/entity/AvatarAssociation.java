package com.faforever.server.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "avatars")
@Getter
@Setter
public class AvatarAssociation {

  @Id
  @Column(name = "id")
  private int id;

  @Column(name = "selected")
  private boolean selected;

  @ManyToOne
  @JoinColumn(name = "idUser", referencedColumnName = "id", nullable = false)
  private Player player;

  @ManyToOne
  @JoinColumn(name = "idAvatar", referencedColumnName = "id", nullable = false)
  private Avatar avatar;
}
