package com.faforever.server.common;

/**
 * Tag interface to be implemented by classes who internally represent a message sent from the server to the client.
 * Depending on the nature of the message, subclasses should be suffixed with either {@code Response} (the message is
 * triggered by a client request) or {@code Message} (the message is fire-and-forget).
 * <p>
 * Subclasses are for server-internal use only; they are not part of the client-server contract and, therefore, must not
 * be serialized directly and their name and fields may change at any time.
 *
 * @see ClientMessage
 */
public interface ServerMessage {

}
