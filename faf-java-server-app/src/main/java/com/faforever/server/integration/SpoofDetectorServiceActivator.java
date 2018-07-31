package com.faforever.server.integration;

import com.faforever.server.entity.Player;
import com.faforever.server.game.SpoofDetectorService;
import com.faforever.server.game.VerifyPlayerReport;
import com.faforever.server.security.FafUserDetails;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.core.Authentication;

import static com.faforever.server.integration.MessageHeaders.USER_HEADER;

@MessageEndpoint
public class SpoofDetectorServiceActivator {
  private final SpoofDetectorService spoofDetectorService;

  public SpoofDetectorServiceActivator(SpoofDetectorService spoofDetectorService) {
    this.spoofDetectorService = spoofDetectorService;
  }

  @ServiceActivator(inputChannel = ChannelNames.VERIFY_PLAYER_REQUEST)
  public void verifyPlayerReport(VerifyPlayerReport report, @Header(USER_HEADER) Authentication authentication) {
    spoofDetectorService.verifyPlayer(
      getPlayer(authentication),
      report.getId(),
      report.getName(),
      report.getMean(),
      report.getDeviation(),
      report.getCountry(),
      report.getAvatarUrl(),
      report.getAvatarDescription()
    );
  }

  private Player getPlayer(Authentication authentication) {
    return ((FafUserDetails) authentication.getPrincipal()).getPlayer();
  }
}
