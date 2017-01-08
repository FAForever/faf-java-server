package com.faforever.server.entity;

import lombok.Setter;
import lombok.ToString;

import javax.persistence.Basic;
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
@Setter
public abstract class Login {

  private int id;
  private String login;
  private String eMail;
  private String steamId;
  private String userAgent;
  private String ip;
  private BanDetails banDetails;

  @Id
  @GeneratedValue
  public int getId() {
    return id;
  }

  @Basic
  @Column(name = "login")
  public String getLogin() {
    return login;
  }

  @Basic
  @Column(name = "email")
  public String getEMail() {
    return eMail;
  }

  @Basic
  @Column(name = "steamid")
  public String getSteamId() {
    return steamId;
  }

  @Basic
  @Column(name = "user_agent")
  public String getUserAgent() {
    return userAgent;
  }

  @OneToOne(mappedBy = "player")
  public BanDetails getBanDetails() {
    return banDetails;
  }

  @Basic
  @Column(name = "ip")
  public String getIp() {
    return ip;
  }
}
