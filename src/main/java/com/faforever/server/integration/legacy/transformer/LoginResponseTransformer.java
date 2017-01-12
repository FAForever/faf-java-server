package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.entity.GlobalRating;
import com.faforever.server.entity.Ladder1v1Rating;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
import com.faforever.server.security.FafUserDetails;
import com.faforever.server.security.UserDetailsResponse;
import com.google.common.collect.ImmutableMap;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.util.Map;

public enum LoginResponseTransformer implements GenericTransformer<UserDetailsResponse, Map<String, Serializable>> {

  INSTANCE;

  @Override
  public Map<String, Serializable> transform(UserDetailsResponse source) {
    FafUserDetails userDetails = source.getUserDetails();
    User user = userDetails.getUser();
    Player player = user.getPlayer();
    GlobalRating globalRating = player.getGlobalRating();
    Ladder1v1Rating ladder1v1Rating = player.getLadder1v1Rating();

    return ImmutableMap.of(
      "command", "welcome",
      "id", user.getId(),
      "login", userDetails.getUsername(),
      "me", ImmutableMap.builder()
        .put("id", user.getId())
        .put("login", userDetails.getUsername())
        .put("global_rating", globalRating != null ? new double[]{globalRating.getMean(), globalRating.getDeviation()} : new double[0])
        .put("ladder_rating", ladder1v1Rating != null ? new double[]{ladder1v1Rating.getMean(), ladder1v1Rating.getDeviation()} : new double[0])
        .put("number_of_games", globalRating != null ? globalRating.getNumGames() : 0)
        // FIXME implement
        .put("avatar", "")
        .put("country", "")
        .put("clan", "")
        .build()
    );
  }
}
