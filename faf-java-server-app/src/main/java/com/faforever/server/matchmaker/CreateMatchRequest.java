package com.faforever.server.matchmaker;

import com.faforever.server.common.ClientMessage;
import com.faforever.server.game.Faction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateMatchRequest implements ClientMessage {

  private UUID requestId;
  private String title;
  private int mapVersionId;
  private String featuredMod;
  private List<Participant> participants;
  private LobbyMode lobbyMode;

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

  @Getter
  @Setter
  public static class Participant {
    private int id;
    private Faction faction;
    private int slot;
    private int team;
    private String name;
    private int startSpot;
  }
}
