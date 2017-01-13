package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.entity.Game;
import com.faforever.server.entity.GameState;
import com.faforever.server.error.ProgrammingError;
import com.faforever.server.game.GameResponse;
import com.google.common.collect.ImmutableMap;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum GameResponseTransformer implements GenericTransformer<GameResponse, Map<String, Serializable>> {
  INSTANCE;

  @Override
  public Map<String, Serializable> transform(GameResponse source) {
    Game game = source.getGame();
    return ImmutableMap.<String, Serializable>builder()
      .put("command", "game_info")
      .put("visibility", game.getGameVisibility().getString())
      .put("password_protected", game.getPassword() != null)
      .put("uid", game.getId())
      .put("title", game.getTitle())
      .put("state", clientGameState(game.getState()))
      .put("featured_mod", game.getFeaturedMod().getTechnicalName())
      // FIXME implement this nightmare
      .put("featured_mod_versions", ImmutableMap.of())
      .put("sim_mods", game.getSimMods().toArray())
      .put("mapname", game.getMapName())
      .put("map_file_path", String.format("maps/%s.zip", game.getMapName()))
      .put("host", game.getHost().getLogin())
      .put("num_players", game.getPlayerStats().size())
      .put("max_players", game.getMaxPlayers())
      .put("launched_at", game.getLaunchedAt() != null ? game.getLaunchedAt().toEpochMilli() / 1000f : 0f)
      .put("teams", teams(game))
      .build();
  }

  private String clientGameState(GameState gameState) {
    switch (gameState) {
      case INITIALIZING:
        return "unknown";
      case OPEN:
        return "open";
      case PLAYING:
        return "playing";
      case CLOSED:
        return "closed";
      default:
        throw new ProgrammingError("Uncovered game state: " + gameState);
    }
  }

  private HashMap<String, List<String>> teams(Game game) {
    HashMap<String, List<String>> playerNamesByTeamId = new HashMap<>();
    game.getPlayerStats().forEach(gamePlayerStats ->
      playerNamesByTeamId.computeIfAbsent(
        String.valueOf(gamePlayerStats.getTeam()),
        s -> new ArrayList<>()
      ).add(gamePlayerStats.getPlayer().getLogin())
    );
    return playerNamesByTeamId;
  }
}
