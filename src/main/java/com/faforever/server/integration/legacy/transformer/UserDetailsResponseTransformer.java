package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.player.UserDetailsResponse;
import com.faforever.server.player.UserDetailsResponse.Player;
import com.faforever.server.player.UserDetailsResponse.Player.Avatar;
import com.faforever.server.player.UserDetailsResponse.Player.Rating;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

public enum UserDetailsResponseTransformer implements GenericTransformer<UserDetailsResponse, Map<String, Serializable>> {

  INSTANCE;

  @Override
  public Map<String, Serializable> transform(UserDetailsResponse source) {
    return ImmutableMap.of(
      "command", "welcome",
      "id", source.getUserId(),
      "login", source.getUsername(),
      "me", me(source)
    );
  }

  private ImmutableMap<Object, Object> me(UserDetailsResponse source) {
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
      .put("country", Optional.ofNullable(source.getCountry()).orElse(""))
      // FIXME implement
      .put("clan", "");

    if (avatar != null) {
      builder
        .put("avatar", ImmutableMap.of(
          "url", avatar.getUrl(),
          "tooltip", avatar.getTooltip()));
    }

    return builder.build();
  }
}
