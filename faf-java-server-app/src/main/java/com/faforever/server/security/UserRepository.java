package com.faforever.server.security;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

  Optional<User> findOneByLoginIgnoreCase(String login);

  @Query("update User u set u.lastLogin = :now where u = :user")
  @Modifying
  void updateLastLogin(@Param("user") User user, @Param("now") Instant now);
}
