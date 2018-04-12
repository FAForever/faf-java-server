package com.faforever.server.client;

import org.junit.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.function.BiFunction;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class DelayedResponseTest {

  @Test
  @SuppressWarnings("unchecked")
  public void sameObjectDoesntCallAggregator() {
    InfoResponse infoResponse = new InfoResponse("Test");

    BiFunction<InfoResponse, InfoResponse, InfoResponse> mockAggregator = Mockito.mock(BiFunction.class);
    DelayedResponse<InfoResponse> response = new DelayedResponse<>(infoResponse, Duration.ZERO, Duration.ZERO, mockAggregator);
    response.onUpdated(infoResponse);

    verifyZeroInteractions(mockAggregator);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void differentObjectDoesntCallsAggregator() {
    InfoResponse oldObject = new InfoResponse("Test");
    InfoResponse newObject = new InfoResponse("Test");

    BiFunction<InfoResponse, InfoResponse, InfoResponse> mockAggregator = Mockito.mock(BiFunction.class);
    DelayedResponse<InfoResponse> response = new DelayedResponse<>(oldObject, Duration.ZERO, Duration.ZERO, mockAggregator);
    response.onUpdated(newObject);

    verify(mockAggregator).apply(oldObject, newObject);
  }
}
