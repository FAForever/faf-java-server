package com.faforever.server.security;

import com.faforever.server.config.ServerProperties;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.error.ProgrammingError;
import com.faforever.server.error.Requests;
import com.faforever.server.player.Player;
import com.faforever.server.security.BanDetails.BanScope;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Transactional
public class PolicyService {
  private final RestTemplate restTemplate;
  private final boolean enabled;
  private final String linkToSteamUrl;
  private final BanDetailsService banDetailsService;

  public PolicyService(ServerProperties properties, BanDetailsService banDetailsService, RestTemplate restTemplate) {
    String url = properties.getPolicyService().getUrl();
    this.enabled = !Strings.isNullOrEmpty(url) && !"false".equals(url);
    this.linkToSteamUrl = properties.getPolicyService().getLinkToSteamUrl();
    this.banDetailsService = banDetailsService;
    this.restTemplate = restTemplate;

    if (!enabled) {
      log.warn("The policy service is disabled as no policy service URL is specified");
    }
  }

  public void verify(Player player, String uid, String session) {
    if (!enabled) {
      log.debug("Skipping verification for player '{}' because it is disabled", player);
      return;
    }

    if (player.getUniqueIdExempt() != null) {
      log.debug("Skipping verification for player '{}' because: {}", player, player.getUniqueIdExempt().getReason());
      return;
    }

    if (player.getSteamId() != null) {
      log.debug("Skipping verification for player '{}' because of steam ID: {}", player, player.getSteamId());
      return;
    }

    String stringResponse = restTemplate.postForObject("/verify", ImmutableMap.of(
      "player_id", player.getId(),
      "uid_hash", uid,
      "session", session
    ), String.class);

    log.debug("Policy server result for player '{}' UID '{}' was: {}", player, uid, stringResponse);
    Result result = Result.fromString(stringResponse);
    switch (result) {
      case VM:
        throw Requests.exception(ErrorCode.UID_VM, linkToSteamUrl);
      case ALREADY_ASSOCIATED:
        throw Requests.exception(ErrorCode.UID_ALREADY_ASSOCIATED, linkToSteamUrl);
      case FRAUDULENT:
        banPlayer(player);
        throw Requests.exception(ErrorCode.UID_FRAUD, linkToSteamUrl);
      case UNKNOWN:
        log.warn("Policy server returned unknown result '{}' for player '{}' and UID: {}", stringResponse, player, uid);
        break;
      case HONEST:
        log.debug("Player '{}' passed unique ID check", player);
        break;
      default:
        throw new ProgrammingError("Uncovered result: " + result);
    }
  }

  private void banPlayer(Player player) {
    banDetailsService.banUser(player.getId(), player.getId(), BanScope.GLOBAL, "Auto-banned because of fraudulent login attempt");
  }

  private enum Result {
    VM("vm"),
    ALREADY_ASSOCIATED("already_associated"),
    FRAUDULENT("fraudulent"),
    HONEST("honest"),
    UNKNOWN(null);

    private static final Map<String, Result> fromString;

    static {
      fromString = new HashMap<>();
      for (Result result : values()) {
        fromString.put(result.string, result);
      }
    }

    private final String string;

    Result(String string) {
      this.string = string;
    }

    private static Result fromString(String string) {
      Result result = fromString.get(string);
      if (result == null) {
        return UNKNOWN;
      }
      return result;
    }
  }
}
