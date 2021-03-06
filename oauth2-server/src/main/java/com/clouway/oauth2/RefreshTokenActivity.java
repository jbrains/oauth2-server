package com.clouway.oauth2;

import com.clouway.oauth2.client.Client;
import com.clouway.oauth2.http.Request;
import com.clouway.oauth2.http.Response;
import com.clouway.oauth2.token.Token;
import com.clouway.oauth2.token.Tokens;
import com.google.common.base.Optional;

import java.util.Date;

/**
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
class RefreshTokenActivity implements ClientActivity {

  private final Tokens tokens;

  RefreshTokenActivity(Tokens tokens) {
    this.tokens = tokens;
  }

  @Override
  public Response execute(Client client, Request request, DateTime instant) {
    String refreshToken = request.param("refresh_token");

    Optional<Token> opt = tokens.refreshToken(refreshToken, instant);

    if (!opt.isPresent()) {
      return OAuthError.invalidGrant();
    }

    Token token = opt.get();

    return new BearerTokenResponse(token.value, token.expiresInSeconds, token.refreshToken);
  }
}
