package com.faforever.server.chat;

import com.google.common.collect.ImmutableMap;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Repository to access Anope's @{NickCore} table (the one that contains usernames and passwords).
 */
@Repository
public class NickCoreRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public NickCoreRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  void updatePassword(String username, String password) {
    jdbcTemplate.update("UPDATE `faf-anope`.anope_db_NickCore SET pass = :password WHERE display = :username",
      ImmutableMap.of(
        "password", password,
        "username", username
      ));
  }
}
