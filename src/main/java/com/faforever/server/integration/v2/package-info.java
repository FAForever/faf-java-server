/**
 * Contains classes required to serialize/deserialize messages to/from the {@link com.faforever.server.integration.Protocol#V2_JSON_UTF_8}
 * protocol. Mapping is done via MapStruct and all mappers need to implement {@link
 * org.springframework.integration.transformer.GenericTransformer}. Even though implementing this interface eliminates
 * the need to override mapping methods in the mapping interface, it is recommended as otherwise MapStruct produces
 * error messages without class information in case of errors, making it much more difficult to find the cause.
 */
package com.faforever.server.integration.v2;
