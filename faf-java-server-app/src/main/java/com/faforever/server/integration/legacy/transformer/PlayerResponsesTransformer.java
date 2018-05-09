package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.client.PlayerResponses;
import com.faforever.server.player.PlayerResponse;
import com.faforever.server.player.PlayerResponse.Avatar;
import com.faforever.server.player.PlayerResponse.Rating;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
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
    Rating globalRating = source.getGlobalRating();
    Rating ladder1v1Rating = source.getLadder1v1Rating();
    Avatar avatar = source.getAvatar();

    Builder<Object, Serializable> builder = ImmutableMap.<Object, Serializable>builder()
      .put("id", source.getPlayerId())
      .put("login", source.getUsername())
      .put("global_rating", ratingToDoubleArray(globalRating))
      .put("ladder_rating", ratingToDoubleArray(ladder1v1Rating))
      .put("number_of_games", source.getNumberOfGames());

    if (source.getCountry() != null) {
      builder.put("country", source.getCountry());
    }

    Optional.ofNullable(source.getTimeZone()).map(TimeZone::getID).ifPresent(timeZone -> builder.put("time_zone", timeZone));
    Optional.ofNullable(source.getClanTag()).ifPresent(clan -> builder.put("clan", clan));

    if (avatar != null) {
      builder
        .put("avatar", ImmutableMap.of(
          "url", avatar.getUrl(),
          "tooltip", avatar.getDescription()));
    }

    return builder.build();
  }
}
