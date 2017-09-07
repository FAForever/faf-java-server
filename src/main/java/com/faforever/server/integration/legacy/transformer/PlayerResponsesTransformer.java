package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.client.PlayerResponses;
import com.faforever.server.player.PlayerResponse;
import com.faforever.server.player.PlayerResponse.Player;
import com.faforever.server.player.PlayerResponse.Player.Avatar;
import com.faforever.server.player.PlayerResponse.Player.Rating;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public enum PlayerResponsesTransformer implements GenericTransformer<PlayerResponses, Map<String, Serializable>> {

  INSTANCE;

  @Override
  public Map<String, Serializable> transform(PlayerResponses source) {
    return ImmutableMap.of(
      "command", "player_info",
      "players", players(source.getResponses())
    );
  }

  private ArrayList<ImmutableMap<Object, Serializable>> players(Collection<PlayerResponse> source) {
    return source.stream()
      .map(PlayerResponsesTransformer::player)
      .collect(Collectors.toCollection(ArrayList::new));
  }

  private static double[] ratingToDoubleArray(Rating globalRating) {
    return globalRating != null ? new double[]{globalRating.getMean(), globalRating.getDeviation()} : new double[2];
  }

  static ImmutableMap<Object, Serializable> player(PlayerResponse source) {
    Player player = source.getPlayer();
    Rating globalRating = player.getGlobalRating();
    Rating ladder1v1Rating = player.getLadder1v1Rating();
    Avatar avatar = player.getAvatar();

    Builder<Object, Serializable> builder = ImmutableMap.<Object, Serializable>builder()
      .put("id", source.getUserId())
      .put("login", source.getUsername())
      .put("global_rating", ratingToDoubleArray(globalRating))
      .put("ladder_rating", ratingToDoubleArray(ladder1v1Rating))
      .put("number_of_games", Optional.of(player).map(Player::getNumberOfGames).orElse(0))
      .put("country", Optional.ofNullable(source.getCountry()).orElse(""));

    Optional.ofNullable(source.getPlayer().getClanTag()).ifPresent(clan -> builder.put("clan", clan));

    if (avatar != null) {
      builder
        .put("avatar", ImmutableMap.of(
          "url", avatar.getUrl(),
          "tooltip", avatar.getTooltip()));
    }

    return builder.build();
  }
}
