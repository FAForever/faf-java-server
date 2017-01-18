package com.faforever.server.game;

import com.faforever.server.response.ServerResponse;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;

/**
 * Hold a dirty object as well as information about when the {@code DirtyObject} was created or updated. This
 * information can be used to determine whether or not a dirty object should be processed.
 */
@Getter
public class DirtyObject<T> {

  /**
   * The dirty object.
   */
  private final T object;

  /**
   * The instant when this instance of {@link DirtyObject} was created.
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
   * Function that creates a {@link ServerResponse} to use to send the object.
   */
  private final Function<T, ServerResponse> responseCreator;

  /**
   * The instant when this {@link DirtyObject} was updated.
   */
  private Instant updateTime;

  public DirtyObject(T object, Duration minDelay, Duration maxDelay, Function<T, ServerResponse> responseCreator) {
    this.object = object;
    this.minDelay = minDelay;
    this.maxDelay = maxDelay;
    this.responseCreator = responseCreator;
    createTime = Instant.now();
    updateTime = createTime;
  }

  public void onUpdated() {
    updateTime = Instant.now();
  }

  public ServerResponse createResponse() {
    return responseCreator.apply(object);
  }
}
