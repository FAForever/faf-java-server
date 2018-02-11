package com.faforever.server.integration.v2.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
class CreateMatchMessage extends V2ClientMessage {
  private UUID requestId;
  private String title;
  /** ID of the map version to be played. */
  private int map;
  /** Name of the featured mod that will be played. */
  private String featuredMod;
  private List<Participant> participants;

  @Getter
  @AllArgsConstructor
  public static class Participant {
    /** The player's ID. */
    int id;
    /** The faction that will be played by this player. */
    Faction faction;
    /** The game slot this player will be assigned to. */
    int slot;
    /** The team this player will be assigned to. */
    int team;
    /**
     * The name to be displayed within the game. This might either be the player's real username or, in case of Galactic
     * War, the character name.
     */
    String name;
  }
}
