package com.faforever.server.integration;

import com.faforever.server.coop.CoopMissionCompletedReport;
import com.faforever.server.coop.CoopService;
import com.faforever.server.security.FafUserDetails;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.core.Authentication;

import static com.faforever.server.integration.MessageHeaders.USER_HEADER;

/**
 * Message endpoint that takes Co-Op messages and calls the respective methods on the {@link CoopService}.
 */
@MessageEndpoint
public class CoopServiceActivators {

  private final CoopService coopService;

  public CoopServiceActivators(CoopService coopService) {
    this.coopService = coopService;
  }

  @ServiceActivator(inputChannel = ChannelNames.OPERATION_COMPLETE_REPORT)
  public void reportOperationComplete(CoopMissionCompletedReport report, @Header(USER_HEADER) Authentication authentication) {
    coopService.reportOperationComplete(((FafUserDetails) authentication.getPrincipal()).getPlayer(), report.isSecondaryTargets(), report.getDuration());
  }
}
