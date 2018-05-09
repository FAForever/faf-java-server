package com.faforever.server.stats.event;

import com.faforever.server.api.ApiAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
    List<EventUpdate> eventUpdates = Arrays.asList(
      new EventUpdate(1, EventId.EVENT_AEON_PLAYS, 1),
      new EventUpdate(2, EventId.EVENT_AEON_WINS, 1)
    );
    instance.executeBatchUpdate(eventUpdates);

    verify(apiAccessor).updateEvents(eventUpdates);
  }
}
