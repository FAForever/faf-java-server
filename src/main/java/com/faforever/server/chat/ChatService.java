package com.faforever.server.chat;

import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class ChatService {

  private final NickCoreRepository nickCoreRepository;

  public ChatService(NickCoreRepository nickCoreRepository) {
    this.nickCoreRepository = nickCoreRepository;
  }

  public void updateIrcPassword(String username, String password) {
    log.debug("Updating IRC password for user: {}", username);
    try {
      nickCoreRepository.updatePassword(username, Hashing.md5().hashString(password, StandardCharsets.UTF_8).toString());
    } catch (BadSqlGrammarException e) {
      // TODO remove this as soon as faf-stacks' anope sets up all tables correctly
      log.warn("IRC password for user '{}' could not be updated ({})", username, e.getMessage());
    }
  }
}
