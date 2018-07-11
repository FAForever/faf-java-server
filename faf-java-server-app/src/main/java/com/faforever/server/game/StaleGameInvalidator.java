package com.faforever.server.game;

import com.faforever.server.entity.Validity;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Invalidates unfinished games on server shutdown and startup. Games are unfinished (in the database) when they're
 * running while the server shuts down or crashes. In such cases, the "game ended" message is missing so the server
 * doesn't know when the game ended.
 * <p>
 * Even if it's received after the server is back online, as the server lost all information about players and games,
 * such messages will be rejected since the server can not associate them with a game. Trying to restore a game's
 * information based on what the client sends is not possible (e. g. the game's configuration would be missing) unless
 * the clients would be extended to store all relevant information and send it to the server. However, even then it
 * would be a hassle to implement since a client may not implement this correctly resulting in different reports,
 * resulting in trust issues.
 */
@Component
public class StaleGameInvalidator {

  private final GameService gameService;

  public StaleGameInvalidator(GameService gameService) {
    this.gameService = gameService;
  }

  @EventListener
  public void onApplicationEvent(ContextRefreshedEvent event) {
    gameService.updateUnfinishedGamesValidity(Validity.STALE);
  }

  @EventListener
  public void onApplicationEvent(ContextClosedEvent event) {
    gameService.updateUnfinishedGamesValidity(Validity.SERVER_SHUTDOWN);
  }
}
