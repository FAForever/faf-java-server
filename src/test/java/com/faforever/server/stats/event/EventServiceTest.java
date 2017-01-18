package com.faforever.server.stats.event;

import com.faforever.server.api.ApiAccessor;
import com.faforever.server.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class EventServiceTest {

  private EventService instance;

  @Mock
  private ApiAccessor apiAccessor;

  @Before
  public void setUp() throws Exception {
    instance = new EventService(apiAccessor);
  }

  @Test
  public void executeBatchUpdate() throws Exception {
    Player player = new Player();
    List<EventUpdate> eventUpdates = Arrays.asList(
      new EventUpdate(EventId.EVENT_AEON_PLAYS, 1),
      new EventUpdate(EventId.EVENT_AEON_WINS, 1)
    );
    instance.executeBatchUpdate(player, eventUpdates);

    verify(apiAccessor).updateEvents(eventUpdates);
  }
}
