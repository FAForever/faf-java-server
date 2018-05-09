package com.faforever.server.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Requires subclasses to be annotated with either of {@link V2ClientNotification}, {@link V2ClientRequest}, {@link
 * V2ServerResponse} and contain a constant {@code TYPE_NAME} that will be used as type identifier in the message's JSON
 * representation.
 */
@Inherited
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ValidV2Message {

}
