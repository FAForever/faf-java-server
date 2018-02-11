package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.social.SocialRelationListResponse;
import com.faforever.server.social.SocialRelationListResponse.SocialRelationResponse;
import com.faforever.server.social.SocialRelationListResponse.SocialRelationResponse.RelationType;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SocialRelationResponseListResponseTransformerTest {
  @Test
  public void transform() throws Exception {
    Map<String, Serializable> result = SocialRelationListResponseTransformer.INSTANCE.transform(
      new SocialRelationListResponse(Arrays.asList(
        new SocialRelationResponse(1, RelationType.FRIEND),
        new SocialRelationResponse(2, RelationType.FOE),
        new SocialRelationResponse(3, RelationType.FOE),
        new SocialRelationResponse(4, RelationType.FRIEND)
      ))
    );

    assertThat(result, is(ImmutableMap.of(
      "command", "social",
      "friends", Arrays.asList(1, 4),
      "foes", Arrays.asList(2, 3)
    )));
  }
}
