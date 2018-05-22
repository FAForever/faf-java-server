package com.faforever.server.integration.v2.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
class CreateMatchClientMessage extends V2ClientMessage {
  private UUID requestId;
  private String title;
  /** ID of the map version to be played. */
  private int map;
  /** Name of the featured mod that will be played. */
  private String featuredMod;
  /** In which lobby mode to start the game in. */
  private LobbyMode lobbyMode;

  private List<Participant> participants;

  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Participant {
    /** The player's ID. */
    int id;
    /** The faction that will be played by this player. */
    Faction faction;
    /** The team this player will be assigned to. */
    int team;
    /**
     * The name to be displayed within the game. This might either be the player's real username or, in case of Galactic
     * War, the character name.
     */
    String name;
    /** ID of the slot on the map the player will start in. */
    int startSpot;
  }

  /**
   * See values for description.
   */
  public enum LobbyMode {

    /**
     * Default lobby where players can select their faction, teams and so on.
     */
    DEFAULT,

    /**
     * The lobby is skipped; the game starts straight away,
     */
    NONE
  }
}
