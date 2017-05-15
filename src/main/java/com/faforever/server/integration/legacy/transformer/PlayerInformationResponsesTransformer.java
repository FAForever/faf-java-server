package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.client.PlayerInformationResponses;
import com.faforever.server.player.PlayerInformationResponse;
import com.faforever.server.player.PlayerInformationResponse.Player;
import com.faforever.server.player.PlayerInformationResponse.Player.Avatar;
import com.faforever.server.player.PlayerInformationResponse.Player.Rating;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public enum PlayerInformationResponsesTransformer implements GenericTransformer<PlayerInformationResponses, Map<String, Serializable>> {

  INSTANCE;

  @Override
  public Map<String, Serializable> transform(PlayerInformationResponses source) {
    return ImmutableMap.of(
      "command", "player_info",
      "players", players(source.getResponses())
    );
  }

  private ArrayList<ImmutableMap<Object, Object>> players(Collection<PlayerInformationResponse> source) {
    return source.stream()
      .map(PlayerInformationResponsesTransformer::player)
      .collect(Collectors.toCollection(ArrayList::new));
  }

  static ImmutableMap<Object, Object> player(PlayerInformationResponse source) {
    Player player = source.getPlayer();
    Rating globalRating = player.getGlobalRating();
    Rating ladder1v1Rating = player.getLadder1v1Rating();
    Avatar avatar = player.getAvatar();

    Builder<Object, Object> builder = ImmutableMap.builder()
      .put("id", source.getUserId())
      .put("login", source.getUsername())
      .put("global_rating", globalRating != null ? new double[]{globalRating.getMean(), globalRating.getDeviation()} : new double[2])
      .put("ladder_rating", ladder1v1Rating != null ? new double[]{ladder1v1Rating.getMean(), ladder1v1Rating.getDeviation()} : new double[2])
      .put("number_of_games", globalRating != null ? player.getNumberOfGames() : 0)
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
