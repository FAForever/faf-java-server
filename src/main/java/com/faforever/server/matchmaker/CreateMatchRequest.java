package com.faforever.server.matchmaker;

import com.faforever.server.common.ClientMessage;
import com.faforever.server.game.Faction;
import com.faforever.server.game.LobbyMode;
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

  @Getter
  @Setter
  public static class Participant {
    private int id;
    private Faction faction;
    private int team;
    private String name;
    private int startSpot;
  }
}
