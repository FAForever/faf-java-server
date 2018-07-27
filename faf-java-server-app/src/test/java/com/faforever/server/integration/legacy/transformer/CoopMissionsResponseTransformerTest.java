package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.coop.CoopMissionResponse;
import com.faforever.server.coop.CoopMissionType;
import org.junit.Test;

import java.io.Serializable;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CoopMissionsResponseTransformerTest {
  @Test
  public void transform() throws Exception {
    CoopMissionResponse response = new CoopMissionResponse(
      123,
      "Coop Mission",
      "This is a description",
      "COOP_001",
      CoopMissionType.CYBRAN_CAMPAIGN
    );

    Map<String, Serializable> result = CoopMissionsResponseTransformer.INSTANCE.transform(response);

    assertThat(result.get("command"), is("coop_info"));
    assertThat(result.get("uid"), is(123));
    assertThat(result.get("featured_mod"), is("coop"));
    assertThat(result.get("name"), is("Coop Mission"));
    assertThat(result.get("description"), is("This is a description"));
    assertThat(result.get("filename"), is("COOP_001"));
    assertThat(result.get("type"), is("Cybran Vanilla Campaign"));
  }
}
