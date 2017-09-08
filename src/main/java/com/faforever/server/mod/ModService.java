package com.faforever.server.mod;

import com.faforever.server.cache.CacheNames;
import com.faforever.server.client.ClientService;
import com.faforever.server.entity.FeaturedMod;
import com.faforever.server.entity.ModVersion;
import com.faforever.server.player.PlayerOnlineEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ModService {
  private static final String COOP_MOD_NAME = "coop";
  private static final String LADDER_1V1_MOD_NAME = "ladder1v1";
  private final ModVersionRepository modVersionRepository;
  private final FeaturedModRepository featuredModRepository;
  private final FeaturedModFileRepository featuredModFileRepository;
  private final ClientService clientService;
  private FeaturedMod coopFeaturedMod;
  private FeaturedMod ladder1v1FeaturedMod;

  @Autowired
  @SuppressWarnings("SpringAutowiredFieldsWarningInspection")
  // Required for access to @Cacheable methods since inner calls do not go through proxy object.
  private ModService modService;

  public ModService(ModVersionRepository modVersionRepository, FeaturedModRepository featuredModRepository,
                    FeaturedModFileRepository featuredModFileRepository, ClientService clientService) {
    this.modVersionRepository = modVersionRepository;
    this.featuredModRepository = featuredModRepository;
    this.featuredModFileRepository = featuredModFileRepository;
    this.clientService = clientService;
  }

  @EventListener
  @Transactional(readOnly = true)
  public void onApplicationEvent(ContextRefreshedEvent event) {
    coopFeaturedMod = featuredModRepository.findOneByTechnicalName(COOP_MOD_NAME).orElse(null);
    ladder1v1FeaturedMod = featuredModRepository.findOneByTechnicalName(LADDER_1V1_MOD_NAME).orElse(null);

    if (coopFeaturedMod == null) {
      throw new IllegalStateException("No mod named '" + COOP_MOD_NAME + "' was found in the database. Note that '"
        + LADDER_1V1_MOD_NAME + "' needs to exist as well.");
    }
    if (ladder1v1FeaturedMod == null) {
      throw new IllegalStateException("No mod named '" + LADDER_1V1_MOD_NAME + "' was found in the database.");
    }
  }

  @Cacheable(CacheNames.FEATURED_MODS)
  public List<FeaturedMod> getFeaturedMods() {
    return Collections.unmodifiableList(featuredModRepository.findAll());
  }

  // TODO cache
  public List<ModVersion> findModVersionsByUids(List<String> uids) {
    return modVersionRepository.findByUidIn(uids);
  }

  @Cacheable(CacheNames.RANKED_MODS)
  public boolean isModRanked(String simModId) {
    return modVersionRepository.findOneByUidAndRankedTrue(simModId).isPresent();
  }

  @EventListener
  public void onPlayerOnlineEvent(PlayerOnlineEvent event) {
    List<FeaturedMod> mods = modService.getFeaturedMods().stream()
      .filter(FeaturedMod::isPublish)
      .collect(Collectors.toList());

    clientService.sendModList(mods, event.getPlayer());
  }

  public boolean isCoop(FeaturedMod featuredMod) {
    return coopFeaturedMod != null && coopFeaturedMod.equals(featuredMod);
  }

  public boolean isLadder1v1(FeaturedMod featuredMod) {
    return ladder1v1FeaturedMod != null && ladder1v1FeaturedMod.equals(featuredMod);
  }

  public Optional<FeaturedMod> getFeaturedMod(int modId) {
    return Optional.ofNullable(featuredModRepository.findOne(modId));
  }

  public Optional<FeaturedMod> getLadder1v1() {
    return Optional.ofNullable(ladder1v1FeaturedMod);
  }

  public Optional<FeaturedMod> getFeaturedMod(String featuredModName) {
    return featuredModRepository.findOneByTechnicalName(featuredModName);
  }

  public List<FeaturedModFile> getLatestFileVersions(FeaturedMod featuredMod) {
    return featuredModFileRepository.getLatestFileVersions(featuredMod.getTechnicalName());
  }
}
