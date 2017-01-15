package com.faforever.server.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

@MappedSuperclass
@Inheritance(strategy = InheritanceType.JOINED)
@ToString(includeFieldNames = false, of = {"id", "login"})
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public abstract class Login {

  @Id
  @GeneratedValue
  private int id;

  @Column(name = "login")
  private String login;

  @Column(name = "email")
  private String eMail;

  @Column(name = "steamid")
  private String steamId;

  @Column(name = "user_agent")
  private String userAgent;

  @Column(name = "ip")
  private String ip;

  @OneToOne(mappedBy = "player")
  private BanDetails banDetails;

  @OneToOne(mappedBy = "player")
  private UniqueIdExempt uniqueIdExempt;

}
