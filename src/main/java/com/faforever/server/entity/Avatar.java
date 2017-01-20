package com.faforever.server.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Collection;

@Entity
@Table(name = "avatars_list")
@Getter
@Setter
@Immutable
public class Avatar {

  @Id
  @Column(name = "id")
  @GeneratedValue
  private int id;

  @Column(name = "url")
  private String url;

  @Column(name = "tooltip")
  private String tooltip;

  @OneToMany(mappedBy = "player")
  private Collection<AvatarAssociation> owners;

}
