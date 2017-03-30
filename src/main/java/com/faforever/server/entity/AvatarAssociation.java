package com.faforever.server.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "avatars")
@Getter
@Setter
@Immutable
@EqualsAndHashCode(of = "id")
public class AvatarAssociation {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "selected")
  private boolean selected;

  @ManyToOne
  @JoinColumn(name = "idUser", referencedColumnName = "id", nullable = false)
  private Player player;

  @ManyToOne
  @JoinColumn(name = "idAvatar", referencedColumnName = "id", nullable = false)
  private Avatar avatar;
}
