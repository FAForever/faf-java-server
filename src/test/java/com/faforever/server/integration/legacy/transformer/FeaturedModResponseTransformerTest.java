package com.faforever.server.integration.legacy.transformer;

import com.faforever.server.entity.FeaturedMod;
import com.faforever.server.mod.FeaturedModResponse;
import org.junit.Test;

import java.io.Serializable;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FeaturedModResponseTransformerTest {
  @Test
  public void transform() throws Exception {
    FeaturedMod featuredMod = new FeaturedMod();
    featuredMod.setTechnicalName("test");
    featuredMod.setDisplayName("Test mod");
    featuredMod.setDescription("This is a test mod");
    featuredMod.setDisplayOrder(3);

    Map<String, Serializable> result = FeaturedModResponseTransformer.INSTANCE.transform(new FeaturedModResponse(featuredMod));

    assertThat(result.get("command"), is("mod_info"));
    assertThat(result.get("publish"), is(1));
    assertThat(result.get("name"), is("test"));
    assertThat(result.get("fullname"), is("Test mod"));
    assertThat(result.get("desc"), is("This is a test mod"));
    assertThat(result.get("order"), is(3));
  }
}
