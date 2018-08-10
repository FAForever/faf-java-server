package com.faforever.server.avatar;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Collection;

@Entity
@Table(name = "avatars_list")
@Getter
@Setter
@Immutable
@EqualsAndHashCode(of = "id")
public class Avatar {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "url")
  private String url;

  @Column(name = "tooltip")
  private String description;

  @OneToMany(mappedBy = "avatar")
  private Collection<AvatarAssociation> owners;

}
