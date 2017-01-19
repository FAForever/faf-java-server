package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.entity.Avatar;
import com.faforever.server.entity.AvatarAssociation;
import com.faforever.server.entity.GlobalRating;
import com.faforever.server.entity.Ladder1v1Rating;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
import com.faforever.server.security.FafUserDetails;
import com.faforever.server.security.UserDetailsResponse;
import org.junit.Test;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class UserDetailsResponseTransformerTest {

  @Test
  @SuppressWarnings("unchecked")
  public void transformFull() throws Exception {
    Avatar avatar = new Avatar();
    avatar.setId(1);
    avatar.setId(141);
    avatar.setTooltip("Tooltip");
    avatar.setUrl("http://example.com");

    Player player = new Player()
      .setGlobalRating((GlobalRating) new GlobalRating().setMean(1200d).setDeviation(200d).setNumGames(12))
      .setLadder1v1Rating((Ladder1v1Rating) new Ladder1v1Rating().setMean(900d).setDeviation(100d));

    player.setAvailableAvatars(Collections.singletonList(new AvatarAssociation()
      .setId(1)
      .setSelected(true)
      .setAvatarByIdAvatar(avatar)
      .setPlayer(player)));

    User user = (User) new User()
      .setPassword("pw")
      .setPlayer(player)
      .setLogin("JUnit")
      .setCountry("CH")
      .setId(1);

    FafUserDetails userDetails = new FafUserDetails(user);

    Map<String, Serializable> result = UserDetailsResponseTransformer.INSTANCE.transform(new UserDetailsResponse(userDetails));

    assertThat(result.get("command"), is("welcome"));
    assertThat(result.get("id"), is(1));
    assertThat(result.get("login"), is("JUnit"));

    Map<String, Object> me = (Map<String, Object>) result.get("me");
    assertThat(me.get("id"), is(1));
    assertThat(me.get("login"), is("JUnit"));
    assertThat(me.get("login"), is("JUnit"));

    assertThat(me.get("global_rating"), is(new double[]{1200, 200}));
    assertThat(me.get("ladder_rating"), is(new double[]{900, 100}));
    assertThat(me.get("number_of_games"), is(12));

    Map<String, Serializable> avatarMap = (Map<String, Serializable>) me.get("avatar");
    assertThat(avatarMap.get("url"), is("http://example.com"));
    assertThat(avatarMap.get("tooltip"), is("Tooltip"));

    assertThat(me.get("country"), is("CH"));
    assertThat(me.get("clan"), is(""));
  }
}
