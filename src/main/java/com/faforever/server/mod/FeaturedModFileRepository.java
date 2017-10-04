package com.faforever.server.mod;

import org.springframework.data.repository.Repository;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;
import java.util.regex.Pattern;

@org.springframework.stereotype.Repository
public class FeaturedModFileRepository implements Repository<FeaturedModFile, Integer> {
  private static final Pattern MOD_NAME_PATTERN = Pattern.compile("[a-z]+");
  private final EntityManager entityManager;

  public FeaturedModFileRepository(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @SuppressWarnings("unchecked")
  public List<FeaturedModFile> getLatestFileVersions(String modName) {
    verifyModName(modName);

    // The following joke is sponsored by FAF's patcher mechanism which shouldn't even require a DB.
    Query query = entityManager.createNativeQuery(String.format(
      "SELECT\n" +
        "  id,\n" +
        "  MAX(file.version) AS `version`,\n" +
        "  file.fileId       AS `fileId`\n" +
        "FROM updates_%1$s_files file\n" +
        "GROUP BY file.fileId;", modName), FeaturedModFile.class);

    return (List<FeaturedModFile>) query.getResultList();
  }

  private void verifyModName(String modName) {
    Assert.isTrue(MOD_NAME_PATTERN.matcher(modName).matches(), "Invalid mod name: " + modName);
  }
}
