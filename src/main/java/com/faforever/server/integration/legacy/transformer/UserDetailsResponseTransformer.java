package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.entity.Avatar;
import com.faforever.server.entity.AvatarAssociation;
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
import java.util.Optional;

public enum UserDetailsResponseTransformer implements GenericTransformer<UserDetailsResponse, Map<String, Serializable>> {

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
        .put("avatar", avatar(player))
        .put("country", user.getCountry())
        // FIXME implement
        .put("clan", "")
        .build()
    );
  }

  private Map<String, Serializable> avatar(Player player) {
    Optional<AvatarAssociation> association = player.getAvailableAvatars().stream()
      .filter(AvatarAssociation::isSelected)
      .findFirst();

    if (!association.isPresent()) {
      return ImmutableMap.of();
    }

    Avatar avatar = association.get().getAvatarByIdAvatar();
    return ImmutableMap.of(
      "url", avatar.getUrl(),
      "tooltip", avatar.getTooltip()
    );
  }
}
