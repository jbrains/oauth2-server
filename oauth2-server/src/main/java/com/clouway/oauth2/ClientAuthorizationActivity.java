package com.clouway.oauth2;

import com.clouway.oauth2.authorization.Authorization;
import com.clouway.oauth2.authorization.ClientAuthorizationRepository;
import com.clouway.oauth2.client.Client;
import com.clouway.oauth2.client.ClientRepository;
import com.clouway.oauth2.http.Request;
import com.clouway.oauth2.http.Response;
import com.clouway.oauth2.http.RsRedirect;
import com.google.common.base.Optional;

/**
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
class ClientAuthorizationActivity implements IdentityActivity {
  private final ClientRepository clientRepository;
  private final ClientAuthorizationRepository clientAuthorizationRepository;

  ClientAuthorizationActivity(ClientRepository clientRepository, ClientAuthorizationRepository clientAuthorizationRepository) {
    this.clientRepository = clientRepository;
    this.clientAuthorizationRepository = clientAuthorizationRepository;
  }

  @Override
  public Response execute(String userId, Request request) {
    String responseType = request.param("response_type");
    String clientId = request.param("client_id");

    Optional<Client> optClient = clientRepository.findById(clientId);

    if (!optClient.isPresent()) {
      return OAuthError.unathorizedClient();
    }

    Client client = optClient.get();

    Optional<Authorization> opt = clientAuthorizationRepository.authorize(client, userId, responseType);

    // RFC-6749 - Section: 4.2.2.1
    // The authorization server redirects the user-agent by
    // sending the following HTTP response:
    // HTTP/1.1 302 Found
    // Location: https://client.example.com/cb#error=access_denied&state=xyz
    if (!opt.isPresent()) {
      return new RsRedirect(client.redirectURI + "?error=access_denied");
    }

    Authorization authorization = opt.get();
    return new RsRedirect(String.format("%s?code=%s", client.redirectURI, authorization.code));
  }
}
