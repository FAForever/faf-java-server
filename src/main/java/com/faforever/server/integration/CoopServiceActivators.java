package com.faforever.server.integration;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.coop.CoopService;
import com.faforever.server.coop.CoopMissionCompletedReport;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;

import static com.faforever.server.client.ClientConnection.CLIENT_CONNECTION;

@MessageEndpoint
public class CoopServiceActivators {

  private final CoopService coopService;

  public CoopServiceActivators(CoopService coopService) {
    this.coopService = coopService;
  }

  @ServiceActivator(inputChannel = ChannelNames.OPERATION_COMPLETE_REPORT)
  public void reportOperationComplete(CoopMissionCompletedReport report, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    coopService.reportOperationComplete(clientConnection.getUserDetails().getPlayer(), report.isSecondaryTargets(), report.getDuration());
  }
}
