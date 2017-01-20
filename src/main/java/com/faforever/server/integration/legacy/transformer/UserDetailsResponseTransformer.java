package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.security.UserDetailsResponse;
import com.faforever.server.security.UserDetailsResponse.Player;
import com.faforever.server.security.UserDetailsResponse.Player.Avatar;
import com.faforever.server.security.UserDetailsResponse.Player.Rating;
import com.google.common.collect.ImmutableMap;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.util.Map;

public enum UserDetailsResponseTransformer implements GenericTransformer<UserDetailsResponse, Map<String, Serializable>> {

  INSTANCE;

  @Override
  public Map<String, Serializable> transform(UserDetailsResponse source) {
    Player player = source.getPlayer();
    Rating globalRating = player.getGlobalRating();
    Rating ladder1v1Rating = player.getLadder1v1Rating();
    Avatar avatar = player.getAvatar();

    return ImmutableMap.of(
      "command", "welcome",
      "id", source.getUserId(),
      "login", source.getUsername(),
      "me", ImmutableMap.builder()
        .put("id", source.getUserId())
        .put("login", source.getUsername())
        .put("global_rating", globalRating != null ? new double[]{globalRating.getMean(), globalRating.getDeviation()} : new double[0])
        .put("ladder_rating", ladder1v1Rating != null ? new double[]{ladder1v1Rating.getMean(), ladder1v1Rating.getDeviation()} : new double[0])
        .put("number_of_games", globalRating != null ? player.getNumberOfGames() : 0)
        .put("avatar", ImmutableMap.of(
          "url", avatar.getUrl(),
          "tooltip", avatar.getTooltip()))
        .put("country", source.getCountry())
        // FIXME implement
        .put("clan", "")
        .build()
    );
  }
}
