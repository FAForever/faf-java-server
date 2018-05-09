package com.faforever.server.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for classes that represent server respond messages. Server responses are messages sent by the server to
 * the client in response to a client request.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface V2ServerResponse {
  String title() default "";

  /**
   * The description to be rendered in the documentation. If not specified, the message's JavaDoc will be used.
   */
  String description() default "";
}
