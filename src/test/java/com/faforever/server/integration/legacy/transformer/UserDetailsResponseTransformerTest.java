package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.player.UserDetailsResponse;
import com.faforever.server.player.UserDetailsResponse.Player.Avatar;
import com.faforever.server.player.UserDetailsResponse.Player.Rating;
import org.junit.Test;

import java.io.Serializable;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class UserDetailsResponseTransformerTest {

  private static final String TEST_USERNAME = "JUnit";

  @Test
  @SuppressWarnings("unchecked")
  public void transformFull() throws Exception {
    Map<String, Serializable> result = UserDetailsResponseTransformer.INSTANCE.transform(new UserDetailsResponse(
      1,
      TEST_USERNAME,
      "CH",
      new UserDetailsResponse.Player(
        new Rating(1200d, 200d),
        new Rating(900d, 100d),
        12,
        new Avatar("http://example.com", "Tooltip"),
        "FOO"
      )
    ));

    assertThat(result.get("command"), is("welcome"));
    assertThat(result.get("id"), is(1));
    assertThat(result.get("login"), is(TEST_USERNAME));

    Map<String, Object> me = (Map<String, Object>) result.get("me");
    assertThat(me.get("id"), is(1));
    assertThat(me.get("login"), is(TEST_USERNAME));
    assertThat(me.get("login"), is(TEST_USERNAME));

    assertThat(me.get("global_rating"), is(new double[]{1200, 200}));
    assertThat(me.get("ladder_rating"), is(new double[]{900, 100}));
    assertThat(me.get("number_of_games"), is(12));

    Map<String, Serializable> avatarMap = (Map<String, Serializable>) me.get("avatar");
    assertThat(avatarMap.get("url"), is("http://example.com"));
    assertThat(avatarMap.get("tooltip"), is("Tooltip"));

    assertThat(me.get("country"), is("CH"));
    assertThat(me.get("clan"), is("FOO"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void transformHandleAvatarNull() throws Exception {
    Map<String, Serializable> result = UserDetailsResponseTransformer.INSTANCE.transform(new UserDetailsResponse(
      1,
      TEST_USERNAME,
      "CH",
      new UserDetailsResponse.Player(
        new Rating(1200d, 200d),
        new Rating(900d, 100d),
        12,
        null,
        "FOO"
      )
    ));

    Map<String, Object> me = (Map<String, Object>) result.get("me");
    assertThat(me.get("avatar"), is(nullValue()));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void transformHandleGlobalRatingNull() throws Exception {
    Map<String, Serializable> result = UserDetailsResponseTransformer.INSTANCE.transform(new UserDetailsResponse(
      1,
      TEST_USERNAME,
      "CH",
      new UserDetailsResponse.Player(
        null,
        new Rating(900d, 100d),
        12,
        new Avatar("http://example.com", "Tooltip"),
        "FOO"
      )
    ));

    Map<String, Object> me = (Map<String, Object>) result.get("me");
    assertThat(me.get("global_rating"), is(new double[2]));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void transformHandleLadder1v1RatingNull() throws Exception {
    Map<String, Serializable> result = UserDetailsResponseTransformer.INSTANCE.transform(new UserDetailsResponse(
      1,
      TEST_USERNAME,
      "CH",
      new UserDetailsResponse.Player(
        new Rating(900d, 100d),
        null,
        12,
        new Avatar("http://example.com", "Tooltip"),
        "FOO"
      )
    ));

    Map<String, Object> me = (Map<String, Object>) result.get("me");
    assertThat(me.get("ladder_rating"), is(new double[2]));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void transformHandleCountryNull() throws Exception {
    Map<String, Serializable> result = UserDetailsResponseTransformer.INSTANCE.transform(new UserDetailsResponse(
      1,
      TEST_USERNAME,
      null,
      new UserDetailsResponse.Player(
        new Rating(1200d, 200d),
        new Rating(900d, 100d),
        12,
        null,
        "FOO"
      )
    ));

    Map<String, Object> me = (Map<String, Object>) result.get("me");
    assertThat(me.get("country"), is(""));
  }
}
