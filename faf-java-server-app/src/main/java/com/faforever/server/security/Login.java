package com.faforever.server.security;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.keyvalue.annotation.KeySpace;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

@MappedSuperclass
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
@KeySpace("login")
public abstract class Login {

  @Id
  @org.springframework.data.annotation.Id
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

  @Column(name = "last_login")
  private Instant lastLogin;

  @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
  private Set<BanDetails> banDetails = new HashSet<>();

  @OneToOne(mappedBy = "player")
  private UniqueIdExempt uniqueIdExempt;

  @Transient
  private String country;

  @Transient
  @Nullable
  private TimeZone timeZone;
}
