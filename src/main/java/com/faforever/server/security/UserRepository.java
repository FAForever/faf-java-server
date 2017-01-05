package com.faforever.server.security;


import com.faforever.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

  User findOneByLogin(String login);
}
