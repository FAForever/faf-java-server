package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.mod.FeaturedModResponse;
import org.junit.Test;

import java.io.Serializable;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FeaturedModResponseTransformerTest {
  @Test
  public void transform() throws Exception {
    Map<String, Serializable> result = FeaturedModResponseTransformer.INSTANCE.transform(new FeaturedModResponse(
      "test", "Test mod", "This is a test mod", 3
    ));

    assertThat(result.get("command"), is("mod_info"));
    assertThat(result.get("publish"), is(1));
    assertThat(result.get("name"), is("test"));
    assertThat(result.get("fullname"), is("Test mod"));
    assertThat(result.get("desc"), is("This is a test mod"));
    assertThat(result.get("order"), is(3));
  }
}
