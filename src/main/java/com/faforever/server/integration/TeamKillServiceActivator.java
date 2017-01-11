package com.faforever.server.integration;

import com.faforever.server.client.ClientConnection;
import com.faforever.server.game.TeamKillReport;
import com.faforever.server.teamkill.TeamKillService;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;

import static com.faforever.server.client.ClientConnection.CLIENT_CONNECTION;

@MessageEndpoint
public class TeamKillServiceActivator {
  private final TeamKillService teamKillService;

  public TeamKillServiceActivator(TeamKillService teamKillService) {
    this.teamKillService = teamKillService;
  }

  @ServiceActivator(inputChannel = ChannelNames.TEAM_KILL_REPORT)
  public void reportTeamKill(TeamKillReport report, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    teamKillService.reportTeamKill(clientConnection.getUserDetails().getPlayer(), report.getTime(), report.getKillerId(), report.getVictimId());
  }
}
