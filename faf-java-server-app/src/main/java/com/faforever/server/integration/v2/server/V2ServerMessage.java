package com.faforever.server.integration.v2.server;

import com.faforever.server.annotations.ValidV2Message;

/**
 * Superclass of all message DTO classes sent from the client to the server. A client does not need to be a player's
 * client but can also be another service like the Galactic War Server or the API.
 */
@ValidV2Message
abstract class V2ServerMessage {
}
