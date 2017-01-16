package com.faforever.server.error;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public final class RequestExceptionWithCode extends BaseMatcher<RequestException> {

  private final ErrorCode errorCode;

  private RequestExceptionWithCode(ErrorCode errorCode) {
    this.errorCode = errorCode;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("an RequestException with error: " + errorCode);
  }

  @Override
  public boolean matches(Object item) {
    return item instanceof RequestException
      && ((RequestException) item).getErrorCode() == errorCode;
  }

  public static RequestExceptionWithCode requestExceptionWithCode(ErrorCode errorCode) {
    return new RequestExceptionWithCode(errorCode);
  }
}
