package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.chat.JoinChatChannelResponse;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class JoinChatChannelsResponseTransformerTest {
  @Test
  @SuppressWarnings("unchecked")
  public void transform() throws Exception {
    JoinChatChannelResponse response = new JoinChatChannelResponse(Sets.newHashSet(
      "#foo", "#bar"
    ));

    Map<String, Serializable> result = JoinChatChannelsResponseTransformer.INSTANCE.transform(response);

    assertThat(result.get("command"), is("social"));
    assertThat((ArrayList<String>) result.get("channels"), containsInAnyOrder("#foo", "#bar"));
    assertThat((ArrayList<String>) result.get("autojoin"), containsInAnyOrder("#foo", "#bar"));
  }
}
