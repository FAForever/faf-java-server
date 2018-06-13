package com.faforever.server.integration.v2.client;

import com.faforever.server.annotations.V2ClientNotification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * Message sent from the client to the server, requesting it to create a new match.
 */
@Getter
@AllArgsConstructor
@V2ClientNotification
@NoArgsConstructor
class CreateMatchClientMessage extends V2ClientMessage {

  public static final String TYPE_NAME = "createMatch";

  /** The request ID as specified in the client's original request. */
  @NotNull
  private UUID requestId;

  /** The title of the game that will be created. */
  @NotNull
  private String title;

  /** ID of the map version to be played. */
  private int map;

  /** Name of the featured mod that will be played, e.g. {@code faf}. */
  @NotNull
  private String featuredMod;

  /** Specifies in which lobby mode the game has to be started in. */
  @NotNull
  private LobbyMode lobbyMode;

  /** The players who will participate in this match. */
  @NotNull
  private List<Participant> participants;

  /** A player that participates in a match. */
  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Participant {
    /** The player's ID. */
    int id;

    /** The faction that will be played by this player. */
    @NotNull
    Faction faction;

    /** The game slot this player will be assigned to. */
    int slot;

    /** The team this player will be assigned to. */
    int team;

    /**
     * The name to be displayed within the game. This might either be the player's real username or, in case of Galactic
     * War, the character name.
     */
    @NotNull
    String name;

    /** ID of the slot on the map the player will start in. */
    int startSpot;
  }

  /** See values for description. */
  public enum LobbyMode {

    /** Default lobby where players can select their faction, teams and so on. */
    DEFAULT,

    /** The lobby is skipped; the game starts straight away, */
    NONE
  }
}
