package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.client.GameResponses;
import com.faforever.server.entity.GameState;
import com.faforever.server.error.ProgrammingError;
import com.faforever.server.game.GameResponse;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public enum GameResponsesTransformer implements GenericTransformer<GameResponses, Map<String, Serializable>> {
  INSTANCE;

  @Override
  public Map<String, Serializable> transform(GameResponses source) {
    return ImmutableMap.of(
      "command", "game_info",
      "games", games(source.getResponses()));
  }

  private ArrayList<ImmutableMap<Object, Serializable>> games(Collection<GameResponse> source) {
    return source.stream()
      .map(GameResponsesTransformer::game)
      .collect(Collectors.toCollection(ArrayList::new));
  }

  static ImmutableMap<Object, Serializable> game(GameResponse source) {
    Builder<Object, Serializable> builder = ImmutableMap.<Object, Serializable>builder()
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

    Optional.ofNullable(source.getMinRating()).ifPresent(minRating -> builder.put("min_rating", minRating));
    Optional.ofNullable(source.getMaxRating()).ifPresent(maxRating -> builder.put("max_rating", maxRating));

    return builder.build();
  }

  static String clientGameState(GameState gameState) {
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

  static HashMap<String, List<String>> teams(GameResponse game) {
    HashMap<String, List<String>> playerNamesByTeamId = new HashMap<>();
    game.getPlayers().forEach(player ->
      playerNamesByTeamId.computeIfAbsent(String.valueOf(player.getTeam()),
        s -> new ArrayList<>()).add(player.getName())
    );
    return playerNamesByTeamId;
  }
}
