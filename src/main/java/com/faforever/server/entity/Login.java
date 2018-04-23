package com.faforever.server.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import java.util.TimeZone;

@MappedSuperclass
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public abstract class Login {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "login")
  private String login;

  @Column(name = "password")
  private String password;

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

  @Transient
  private String country;

  @Transient
  @Nullable
  private TimeZone timeZone;
}
