package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.entity.Game;
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
      .put("state", game.getGameState().getString())
      .put("featured_mod", game.getFeaturedMod().getTechnicalName())
      // FIXME implement this nightmare
      .put("featured_mod_versions", "{}")
      .put("sim_mods", game.getSimMods().toArray())
      .put("mapname", game.getMapName())
      .put("map_file_path", "maps/" + game.getMap().getFilename())
      .put("host", game.getHost().getLogin())
      .put("num_players", game.getPlayerStats().size())
      .put("max_players", game.getMaxPlayers())
      .put("launched_at", game.getLaunchedAt().toEpochMilli() / 1000f)
      .put("teams", teams(game))
      .build();
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
