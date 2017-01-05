package com.faforever.server.legacyadapter;

import com.faforever.server.entity.GlobalRating;
import com.faforever.server.entity.Ladder1v1Rating;
import com.faforever.server.entity.Player;
import com.faforever.server.entity.User;
import com.faforever.server.geoip.GeoIpService;
import com.faforever.server.integration.ChannelNames;
import com.faforever.server.request.LoginRequest;
import com.faforever.server.security.FafUserDetails;
import com.google.common.collect.ImmutableMap;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Map;

@MessageEndpoint
public class LegacyMessageEndpoint {

  private final AuthenticationManager authenticationManager;
  private final GeoIpService geoIpService;

  @Inject
  public LegacyMessageEndpoint(AuthenticationManager authenticationManager, GeoIpService geoIpService) {
    this.authenticationManager = authenticationManager;
    this.geoIpService = geoIpService;
  }

  @ServiceActivator(inputChannel = ChannelNames.LEGACY_SESSION_REQUEST, outputChannel = ChannelNames.LEGACY_OUTBOUND)
  public Map<String, Serializable> askSession() {
    // The client nor the server ever cares about the actual session ID, but the client may still expect it.
    return ImmutableMap.of("command", "session", "session", 1);
  }

  @ServiceActivator(inputChannel = ChannelNames.LEGACY_LOGIN_REQUEST, outputChannel = ChannelNames.CLIENT_OUTBOUND)
  public Map<String, Serializable> loginRequest(LoginRequest loginRequest) {
    try {
      Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.getLogin(), loginRequest.getPassword())
      );
      FafUserDetails userDetails = (FafUserDetails) authentication.getPrincipal();
      User user = userDetails.getUser();
      Player player = userDetails.getPlayer();

      GlobalRating globalRating = player.getGlobalRating();
      Ladder1v1Rating ladder1v1Rating = player.getLadder1v1Rating();

      return ImmutableMap.of(
        "command", "welcome",
        "id", user.getId(),
        "login", userDetails.getUsername(),
        "me", ImmutableMap.builder()
          .put("id", user.getId())
          .put("login", userDetails.getUsername())
          .put("global_rating", new double[]{globalRating.getMean(), globalRating.getDeviation()})
          .put("ladder_rating", new double[]{ladder1v1Rating.getMean(), ladder1v1Rating.getDeviation()})
          .put("number_of_games", globalRating.getNumGames())
          // FIXME implement
          .put("avatar", "")
          .put("country", geoIpService.getCountry(user.getIp()))
          .put("clan", "")
          .build()
      );
    } catch (BadCredentialsException e) {
      return error("Login not found or password incorrect. They are case sensitive.");
    }
  }

  private Map<String, Serializable> error(String text) {
    return ImmutableMap.of(
      "command", "notice",
      "style", "error",
      "text", text
    );
  }
}
