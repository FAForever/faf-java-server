package com.faforever.server.integration.v2.client;

/**
 * Sent by the client to let the server know that it still connected. This is needed since without it, TCP timeouts are
 * way too long (about 15min) and can't be reduced by the application.
 */
class PingClientMessage extends V2ClientMessage {
}
