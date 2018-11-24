package com.faforever.server.common;

/**
 * Tag interface to be implemented by classes who internally represent a message sent from the client to the server.
 * Depending on the nature of the message, subclasses should be suffixed with either {@code Request} (the message
 * expects a response from the server) or {@code Report} (the message is fire-and-forget).
 * <p>
 * Subclasses are for server-internal use only; they are not part of the client-server contract and, therefore, must not
 * be deserialized directly and their name and fields may change at any time.
 *
 * @see ServerMessage
 */
public interface ClientMessage {

}
