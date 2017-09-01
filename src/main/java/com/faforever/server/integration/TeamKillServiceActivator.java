package com.faforever.server.integration;

import com.faforever.server.game.TeamKillReport;
import com.faforever.server.security.FafUserDetails;
import com.faforever.server.teamkill.TeamKillService;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.core.Authentication;

import static com.faforever.server.integration.MessageHeaders.USER_HEADER;

/**
 * Message endpoint that takes team kill messages and calls the respective methods on the {@link TeamKillService}.
 */
@MessageEndpoint
public class TeamKillServiceActivator {
  private final TeamKillService teamKillService;

  public TeamKillServiceActivator(TeamKillService teamKillService) {
    this.teamKillService = teamKillService;
  }

  @ServiceActivator(inputChannel = ChannelNames.TEAM_KILL_REPORT)
  public void reportTeamKill(TeamKillReport report, @Header(USER_HEADER) Authentication authentication) {
    teamKillService.reportTeamKill(((FafUserDetails) authentication.getPrincipal()).getPlayer(), report.getTime(), report.getKillerId(), report.getVictimId());
  }
}
