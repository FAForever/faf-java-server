package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.client.PlayerInformationResponses;
import com.faforever.server.player.PlayerInformationResponse;
import com.faforever.server.player.PlayerInformationResponse.Player.Avatar;
import com.faforever.server.player.PlayerInformationResponse.Player.Rating;
import org.junit.Test;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class PlayerInformationResponsesTransformerTest {

  private static final String TEST_USERNAME = "JUnit";

  @Test
  @SuppressWarnings("unchecked")
  public void transformFull() throws Exception {
    Map<String, Serializable> result = PlayerInformationResponsesTransformer.INSTANCE.transform(new PlayerInformationResponses(
      Collections.singletonList(new PlayerInformationResponse(
        1,
        TEST_USERNAME,
        "CH",
        new PlayerInformationResponse.Player(
          new Rating(1200d, 200d),
          new Rating(900d, 100d),
          12,
          new Avatar("http://example.com", "Tooltip"),
          "FOO"
        )
      ))));

    assertThat(result.get("command"), is("player_info"));

    List<Map<Object, Object>> players = (List<Map<Object, Object>>) result.get("players");
    assertThat(players.size(), is(1));
    Map<Object, Object> player = players.get(0);
    assertThat(player.get("id"), is(1));
    assertThat(player.get("login"), is(TEST_USERNAME));
    assertThat(player.get("login"), is(TEST_USERNAME));

    assertThat(player.get("global_rating"), is(new double[]{1200, 200}));
    assertThat(player.get("ladder_rating"), is(new double[]{900, 100}));
    assertThat(player.get("number_of_games"), is(12));

    Map<String, Serializable> avatarMap = (Map<String, Serializable>) player.get("avatar");
    assertThat(avatarMap.get("url"), is("http://example.com"));
    assertThat(avatarMap.get("tooltip"), is("Tooltip"));

    assertThat(player.get("country"), is("CH"));
    assertThat(player.get("clan"), is("FOO"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void transformHandleAvatarNull() throws Exception {
    Map<String, Serializable> result = PlayerInformationResponsesTransformer.INSTANCE.transform(new PlayerInformationResponses(
      Collections.singletonList(new PlayerInformationResponse(
        1,
        TEST_USERNAME,
        "CH",
        new PlayerInformationResponse.Player(
          new Rating(1200d, 200d),
          new Rating(900d, 100d),
          12,
          null,
          "FOO"
        )
      ))));

    List<Map<String, Object>> players = (List<Map<String, Object>>) result.get("players");
    assertThat(players.get(0).get("avatar"), is(nullValue()));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void transformHandleGlobalRatingNull() throws Exception {
    Map<String, Serializable> result = PlayerInformationResponsesTransformer.INSTANCE.transform(new PlayerInformationResponses(
      Collections.singletonList(new PlayerInformationResponse(
        1,
        TEST_USERNAME,
        "CH",
        new PlayerInformationResponse.Player(
          null,
          new Rating(900d, 100d),
          12,
          new Avatar("http://example.com", "Tooltip"),
          "FOO"
        )
      ))));

    List<Map<String, Object>> players = (List<Map<String, Object>>) result.get("players");
    assertThat(players.get(0).get("global_rating"), is(new double[2]));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void transformHandleLadder1v1RatingNull() throws Exception {
    Map<String, Serializable> result = PlayerInformationResponsesTransformer.INSTANCE.transform(new PlayerInformationResponses(
      Collections.singletonList(new PlayerInformationResponse(
        1,
        TEST_USERNAME,
        "CH",
        new PlayerInformationResponse.Player(
          new Rating(900d, 100d),
          null,
          12,
          new Avatar("http://example.com", "Tooltip"),
          "FOO"
        )
      ))));

    List<Map<String, Object>> players = (List<Map<String, Object>>) result.get("players");
    assertThat(players.get(0).get("ladder_rating"), is(new double[2]));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void transformHandleCountryNull() throws Exception {
    Map<String, Serializable> result = PlayerInformationResponsesTransformer.INSTANCE.transform(new PlayerInformationResponses(
      Collections.singletonList(new PlayerInformationResponse(
        1,
        TEST_USERNAME,
        null,
        new PlayerInformationResponse.Player(
          new Rating(1200d, 200d),
          new Rating(900d, 100d),
          12,
          null,
          "FOO"
        )
      ))));

    List<Map<String, Object>> players = (List<Map<String, Object>>) result.get("players");
    assertThat(players.get(0).get("country"), is(""));
  }
}
