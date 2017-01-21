package com.faforever.server.mod;

import com.faforever.server.cache.CacheNames;
import com.faforever.server.client.ClientService;
import com.faforever.server.client.ConnectionAware;
import com.faforever.server.entity.FeaturedMod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Service
// Required for inner calls to cached methods
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
@Slf4j
public class ModService {
  private static final String COOP_MOD_NAME = "coop";
  private static final String LADDER_1V1_MOD_NAME = "ladder1v1";
  private final ModRepository modRepository;
  private final FeaturedModRepository featuredModRepository;
  private final ClientService clientService;
  private FeaturedMod coopFeaturedMod;
  private FeaturedMod ladder1v1FeaturedMod;

  public ModService(ModRepository modRepository, FeaturedModRepository featuredModRepository, ClientService clientService) {
    this.modRepository = modRepository;
    this.featuredModRepository = featuredModRepository;
    this.clientService = clientService;
  }

  @PostConstruct
  public void postConstruct() {
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

  public List<Object> getMods(List<String> modUids) {
    // FIXME implement
    return emptyList();
  }

  @Cacheable(CacheNames.RANKED_MODS)
  public boolean isModRanked(String simModId) {
    return modRepository.findOneByUidAndRankedTrue(simModId).isPresent();
  }

  @EventListener
  public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
    List<FeaturedMod> mods = getFeaturedMods().stream()
      .filter(FeaturedMod::isPublish)
      .collect(Collectors.toList());

    clientService.sendModList(mods, ((ConnectionAware) event.getAuthentication().getDetails()));
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

  public FeaturedMod getLadder1v1() {
    return ladder1v1FeaturedMod;
  }
}
