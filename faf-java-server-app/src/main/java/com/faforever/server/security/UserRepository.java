package com.faforever.server.security;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

  Optional<User> findOneByLoginIgnoreCase(String login);
}
