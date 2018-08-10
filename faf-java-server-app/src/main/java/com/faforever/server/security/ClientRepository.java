package com.faforever.server.security;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<OAuthClient, String> {
  Optional<OAuthClient> findOneById(String id);
}
