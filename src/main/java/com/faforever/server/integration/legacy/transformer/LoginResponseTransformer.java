package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.entity.GlobalRating;
import com.faforever.server.entity.Ladder1v1Rating;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
import com.faforever.server.integration.legacy.dto.LoginResponse;
import com.faforever.server.security.FafUserDetails;
import com.google.common.collect.ImmutableMap;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.Serializable;
import java.util.Map;

public enum  LoginResponseTransformer implements GenericTransformer<LoginResponse, Map<String, Serializable>> {

  INSTANCE;

  @Override
  public Map<String, Serializable> transform(LoginResponse source) {
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
        .put("global_rating", new double[]{globalRating.getMean(), globalRating.getDeviation()})
        .put("ladder_rating", new double[]{ladder1v1Rating.getMean(), ladder1v1Rating.getDeviation()})
        .put("number_of_games", globalRating.getNumGames())
        // FIXME implement
        .put("avatar", "")
        .put("country", "")
        .put("clan", "")
        .build()
    );
  }
}
