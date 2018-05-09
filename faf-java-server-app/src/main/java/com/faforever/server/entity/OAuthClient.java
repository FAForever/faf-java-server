package com.faforever.server.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "oauth_clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OAuthClient {
  @Id
  @Column(name = "id")
  private String id;

  @Column(name = "name")
  private String name;

  @Column(name = "client_secret")
  private String secret;

  @NotNull
  @Column(name = "redirect_uris")
  private String redirectUris;

  @NotNull
  @Column(name = "default_redirect_uri")
  private String defaultRedirectUri;

  @NotNull
  @Column(name = "default_scope")
  private String defaultScope;
}
