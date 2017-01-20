package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.error.ErrorCode;
import com.faforever.server.error.ErrorResponse;
import org.junit.Test;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ErrorResponseTransformerTest {
  @Test
  public void transform() throws Exception {
    ErrorCode errorCode = ErrorCode.UNKNOWN_MESSAGE;
    ErrorResponse errorResponse = new ErrorResponse(errorCode, new Object[]{"foobar"});

    Map<String, Serializable> result = ErrorResponseTransformer.INSTANCE.transform(errorResponse);

    assertThat(result.get("command"), is("notice"));
    assertThat(result.get("style"), is("error"));
    assertThat(result.get("code"), is(errorCode.getCode()));
    assertThat(result.get("text"), is(
      MessageFormat.format(errorCode.getTitle() + ": " + errorCode.getDetail(), "foobar"))
    );
  }
}
