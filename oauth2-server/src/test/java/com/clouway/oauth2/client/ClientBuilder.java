package com.clouway.oauth2.client;

/**
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
public class ClientBuilder {

  public static ClientBuilder aNewClient() {
    return new ClientBuilder();
  }

  private String clientId;
  private String redirectUrl = "::redirect_url::";
  private String clientSecret;

  public ClientBuilder withId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  public ClientBuilder withSecret(String clientSecret) {
    this.clientSecret = clientSecret;
    return this;
  }

  public ClientBuilder withRedirectUrl(String redirectUrl) {
    this.redirectUrl = redirectUrl;
    return this;
  }

  public Client build() {
    return new Client(clientId, clientSecret, "test name", "::url::", "::desc::", redirectUrl);
  }
}
