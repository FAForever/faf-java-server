package com.faforever.server.game;

import com.faforever.server.common.ServerMessage;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;

/**
 * Hold a response as well as information about when the it was created or updated. This information can be used to
 * determine whether or not a response should be processed.
 */
@Getter
public class DelayedResponse<T extends ServerMessage> {

  /**
   * The instant when this instance of {@link DelayedResponse} was created.
   */
  private final Instant createTime;
  /**
   * Minimum time to wait since the last {@code updateTime} before the object is allowed to be sent.
   */
  private final Duration minDelay;
  /**
   * Maximum time to wait since {@code createTime} until the object has to be sent.
   */
  private final Duration maxDelay;
  /**
   * The response object.
   */
  private T response;
  /**
   * The instant when this {@link DelayedResponse} was updated.
   */
  private Instant updateTime;

  public DelayedResponse(T response, Duration minDelay, Duration maxDelay) {
    this.response = response;
    this.minDelay = minDelay;
    this.maxDelay = maxDelay;
    createTime = Instant.now();
    updateTime = createTime;
  }

  public void onUpdated(T object) {
    updateTime = Instant.now();
    this.response = (T) object;
  }
}
