package com.faforever.server.user;

import com.faforever.server.config.ServerProperties;
import com.faforever.server.player.PlayerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.stereotype.Service;

import javax.management.MXBean;
import java.util.Map;

@Service
@Slf4j
@MXBean
public class UserService {
  private static final String KEY_USER_ID = "user_id";

  private final PlayerService playerService;
  private final MacSigner macSigner;
  private final ObjectMapper objectMapper;

  public UserService(PlayerService playerService, ServerProperties properties, ObjectMapper objectMapper) {
    this.playerService = playerService;
    this.macSigner = new MacSigner(properties.getJwt().getSecret());
    this.objectMapper = objectMapper;
  }

  @SneakyThrows
  @SuppressWarnings("unchecked")
  public void login(String uniqueId, String jwtString) {
    Jwt jwt = JwtHelper.decodeAndVerify(jwtString, macSigner);

    Map<String, Object> claims = objectMapper.readValue(jwt.getClaims(), Map.class);
    Object userId = claims.get(KEY_USER_ID);


//    Requests.verify(!playerService.isPlayerOnline(loginRequest.getLogin()), ErrorCode.USER_ALREADY_CONNECTED, loginRequest.getLogin());

//    log.debug("Processing login request from user: {}", loginRequest.getLogin());
//    try {
//      UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginRequest.getLogin(), loginRequest.getPassword());
//
//      Authentication authentication = authenticationManager.authenticate(token);
//      FafUserDetails userDetails = (FafUserDetails) authentication.getPrincipal();
//
//      clientConnection.setAuthentication(authentication);
//      Player player = userDetails.getPlayer();
//      player.setClientConnection(clientConnection);
//      geoIpService.lookupCountryCode(clientConnection.getClientAddress()).ifPresent(player::setCountry);
//
//      uniqueIdService.verify(player, loginRequest.getUniqueId());
//      chatService.updateIrcPassword(userDetails.getUsername(), loginRequest.getPassword());
//
//      eventPublisher.publishEvent(new PlayerOnlineEvent(this, player));
//    } catch (BadCredentialsException e) {
//      throw new RequestException(e, ErrorCode.INVALID_LOGIN);
//    }
  }
}
