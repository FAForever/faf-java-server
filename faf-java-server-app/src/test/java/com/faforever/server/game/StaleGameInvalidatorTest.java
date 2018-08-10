package com.faforever.server.game;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class StaleGameInvalidatorTest {

  private StaleGameInvalidator instance;

  @Mock
  private GameService gameService;
  @Mock
  private ApplicationContext applicationContext;

  @Before
  public void setUp() throws Exception {
    instance = new StaleGameInvalidator(gameService);
  }

  @Test
  public void onServerStart() {
    instance.onApplicationEvent(new ContextRefreshedEvent(applicationContext));

    verify(gameService).updateUnfinishedGamesValidity(Validity.STALE);
  }

  @Test
  public void onServerShutdown() {
    instance.onApplicationEvent(new ContextClosedEvent(applicationContext));

    verify(gameService).updateUnfinishedGamesValidity(Validity.SERVER_SHUTDOWN);
  }
}
