package com.faforever.server.integration.v2.server;

import lombok.Getter;
import lombok.Setter;

/**
 * Message sent from the server to the client containing details about the logged in player.
 */
@Getter
@Setter
class LoginDetailsServerMessage extends PlayerServerMessage {
}
