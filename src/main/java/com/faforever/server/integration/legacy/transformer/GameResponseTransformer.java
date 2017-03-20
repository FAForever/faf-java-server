package com.faforever.server.integration.legacy.transformer;

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
    final ImmutableMap.Builder<String, Serializable> response = ImmutableMap.<String, Serializable>builder()
      .put("command", "game_info")
      .put("visibility", source.getGameVisibility().getString())
      .put("password_protected", source.getPassword() != null)
      .put("uid", source.getId())
      .put("title", source.getTitle())
      .put("state", clientGameState(source.getState()))
      .put("featured_mod", source.getFeaturedModTechnicalName())
      .put("sim_mods", source.getSimMods().toArray())
      .put("mapname", source.getTechnicalMapName())
      .put("map_file_path", String.format("maps/%s.zip", source.getTechnicalMapName()))
      .put("host", source.getHostUsername())
      .put("num_players", source.getPlayers().size())
      .put("max_players", source.getMaxPlayers())
      .put("launched_at", source.getStartTime() != null ? source.getStartTime().toEpochMilli() / 1000d : 0d)
      .put("teams", teams(source))
      // FIXME implement this nightmare
      .put("featured_mod_versions", ImmutableMap.of());

    if (source.getMinRating() != null) {
      response.put("min_rating", source.getMinRating());
    }
    if (source.getMaxRating() != null) {
      response.put("max_rating", source.getMaxRating());
    }
    return response.build();
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

  private HashMap<String, List<String>> teams(GameResponse game) {
    HashMap<String, List<String>> playerNamesByTeamId = new HashMap<>();
    game.getPlayers().forEach(player ->
      playerNamesByTeamId.computeIfAbsent(String.valueOf(player.getTeam()),
        s -> new ArrayList<>()).add(player.getName())
    );
    return playerNamesByTeamId;
  }
}
