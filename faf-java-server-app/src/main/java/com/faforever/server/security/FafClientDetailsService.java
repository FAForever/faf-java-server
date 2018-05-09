package com.faforever.server.security;

import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.stereotype.Service;

@Service
public class FafClientDetailsService implements ClientDetailsService {

  private final ClientRepository clientRepository;

  public FafClientDetailsService(ClientRepository clientRepository) {
    this.clientRepository = clientRepository;
  }

  @Override
  public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
    return clientRepository.findOneById(clientId)
      .map(oAuthClient -> new FafClientDetails(oAuthClient, oAuthClient.getDefaultScope(), oAuthClient.getRedirectUris()))
      .orElseThrow(() -> new ClientRegistrationException("Client could not be found: " + clientId));
  }
}
