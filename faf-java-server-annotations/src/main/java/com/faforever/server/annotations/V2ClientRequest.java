package com.faforever.server.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotation for classes that represent client request messages. A client request always expects a server response. */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface V2ClientRequest {
  Class<?> successResponse();

  String title() default "";

  String description() default "";
}
