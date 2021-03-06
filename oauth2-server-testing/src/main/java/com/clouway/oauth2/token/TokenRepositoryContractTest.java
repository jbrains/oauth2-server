package com.clouway.oauth2.token;

import com.clouway.oauth2.DateTime;
import com.clouway.oauth2.Duration;
import com.google.common.base.Optional;
import org.junit.Test;

import java.util.Date;

import static com.clouway.oauth2.Duration.hours;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

/**
 * @author Mihail Lesikov (mlesikov@gmail.com)
 */
public abstract class TokenRepositoryContractTest {

  private static final Duration oneHour = hours(1);

  private Tokens repository;

  @Test
  public void happyPath() throws Exception {
    repository = createRepo(oneHour);

    DateTime anyInstantTime = new DateTime();
    Token issuedToken = repository.issueToken("::user1::", anyInstantTime);

    Optional<Token> tokenOptional = repository.getNotExpiredToken(issuedToken.value, anyInstantTime.plusSeconds(20));

    assertThat(tokenOptional.get().value, is(equalTo(issuedToken.value)));
    assertThat(tokenOptional.get().type, is(equalTo(issuedToken.type)));
    assertThat(tokenOptional.get().refreshToken, is(equalTo(issuedToken.refreshToken)));
    assertThat(tokenOptional.get().creationDate, is(equalTo(anyInstantTime)));
    assertThat(tokenOptional.get().expiresInSeconds, is(equalTo(3600L)));
  }

  @Test
  public void refreshToken() throws Exception {
    repository = createRepo(oneHour);

    DateTime anyInstantTime = new DateTime();

    Token newlyIssuedToken = repository.issueToken("identityId", anyInstantTime);

    Optional<Token> tokenOptional = repository.refreshToken(newlyIssuedToken.refreshToken, anyInstantTime.plusSeconds(2));

    assertThat(tokenOptional.get().value, is(equalTo(newlyIssuedToken.value)));
    assertThat(tokenOptional.get().type, is(equalTo(newlyIssuedToken.type)));
    assertThat(tokenOptional.get().refreshToken, is(equalTo(newlyIssuedToken.refreshToken)));
    assertThat(tokenOptional.get().creationDate, is(equalTo(anyInstantTime)));
    assertThat(tokenOptional.get().expiresInSeconds, is(equalTo(oneHour.seconds)));
  }

  @Test
  public void tryToRefreshNonExistingToken() throws Exception {
    repository = createRepo(oneHour);
    Optional<Token> tokenOptional = repository.refreshToken("refreshToken.value", new DateTime());

    assertFalse(tokenOptional.isPresent());
  }

  @Test
  public void expiredToken() throws Exception {
    //created two hours ago
    final DateTime creationDate = new DateTime(System.currentTimeMillis() - hours(2).asMills());
    final Token token = new Token("9c5084d190264d0de737a8049ed630fd", TokenType.BEARER, "refresh", "identityId", oneHour.seconds, creationDate);

    Date currentDate = new Date(System.currentTimeMillis() + 9000000);
    repository = createRepo(oneHour);

    repository.issueToken("identityId", creationDate);

    Optional<Token> tokenOptional = repository.getNotExpiredToken(token.value, new DateTime());

    assertFalse(tokenOptional.isPresent());
  }

  @Test
  public void refreshExpiredToken() throws Exception {
    //created two hours ago
    DateTime currentDate = new DateTime(System.currentTimeMillis() + 9000000);

    repository = createRepo(oneHour);
    Token token = repository.issueToken("::user2::", currentDate);

    Optional<Token> tokenOptional = repository.refreshToken(token.refreshToken, currentDate);

    assertThat(tokenOptional.get().value, is(equalTo(token.value)));
    assertThat(tokenOptional.get().type, is(equalTo(token.type)));
    assertThat(tokenOptional.get().refreshToken, is(equalTo(token.refreshToken)));
  }

  protected abstract Tokens createRepo(Duration duration);

}

