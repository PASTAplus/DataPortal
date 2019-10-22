/*
 *
 * $Date$
 * $Author$
 * $Revision$
 *
 * Copyright 2011,2012 the University of New Mexico.
 *
 * This work was supported by National Science Foundation Cooperative
 * Agreements #DEB-0832652 and #DEB-0936498.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 *
 */

package edu.lternet.pasta.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import org.apache.commons.configuration.Configuration;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

import edu.lternet.pasta.portal.ConfigurationListener;
import edu.lternet.pasta.token.TokenManager;



/**
 * @author servilla
 * @since Mar 13, 2012
 * 
 *        The LoginService brokers user authentication between the NIS Data
 *        Portal and PASTA. A successful login will return a PASTA
 *        authentication token, which is then stored in the "tokenstore".
 * 
 */
public class LoginClient {

  /*
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.client.LoginClient.class);

  private static final int TEAPOT = 418;

  /*
   * Instance variables
   */

  private final String LOGIN_URL;
  private String authHost = null; // EDI authentication server
  private String authProtocol = null; // EDI authentication web protocol
  private int authPort; // EDI authentication web port
  private String authUri = null;

  /*
   * Constructors
   */

  /**
   * Create an new LoginService object with the user's credentials and, if the
   * user's authentication is successful, place the user's authentication token
   * into the "tokenstore" for future use.
   * 
   * @param distinguishedName
   *          The user distinguished name, e.g. "uid=EDI,o=EDI,dc=edirepository,dc=org"
   * @param password
   *          The user password.
   * 
   * @throws PastaAuthenticationException
   */
  public LoginClient(String distinguishedName, String password)
          throws PastaAuthenticationException, PastaImATeapotException {

    Configuration options = ConfigurationListener.getOptions();

    this.authHost = options.getString("auth.hostname");
    this.authProtocol = options.getString("auth.protocol");
    this.authPort = options.getInt("auth.port");
    this.authUri = options.getString("auth.uriTail");

    String pastaUrl = PastaClient.composePastaUrl(this.authProtocol, this.authHost, this.authPort);
    this.LOGIN_URL = pastaUrl + this.authUri;

    String token = this.login(distinguishedName, password);

    if (token == null) {
      String gripe = "User '" + distinguishedName + "' did not successfully authenticate.";
      throw new PastaAuthenticationException(gripe);
    } else if (token.equals(String.valueOf(TEAPOT))){
      String gripe = "I'm a teapot, coffee is ready!";
      throw new PastaImATeapotException(gripe);
    } else {
      TokenManager tokenManager = new TokenManager(token);
      try {
        tokenManager.storeToken();
      } catch (SQLException e) {
        logger.error(e);
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        logger.error(e);
        e.printStackTrace();
      }
    }

  }

  
  /*
   * Class methods
   */

	/*
	 * Closes the HTTP client
	 */
	private static void closeHttpClient(CloseableHttpClient httpClient) {
		try {
			httpClient.close();
		}
		catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}


  /*
   * Instance methods
   */

  /**
   * Perform a PASTA login operation using the user's credentials. Because there
   * is not a formal PASTA login method, we will use a simple query for the Data
   * Package Manager service, which will perform the necessary user
   * authentication (this step should be replaced with a formal PASTA
   * "login service method").
   * 
   * @param username
   *          The user distinguished name, e.g. "uid=EDI,o=EDI,dc=edirepository,dc=org"
   * @param password
   *          The user password.
   * 
   * @return The authentication token as a String object if the login is
   *         successful.
   */
  private String login(String username, String password) {

    String token = null;

    /*
     * The following set of code sets up Preemptive Authentication for the HTTP
     * CLIENT and is done so at the warning stated within the Apache
     * Http-Components Client tutorial here:
     * http://hc.apache.org/httpcomponents-
     * client-ga/tutorial/html/authentication.html#d5e1031
     */

    // Define host parameters
    HttpHost httpHost = new HttpHost(this.authHost, this.authPort, this.authProtocol);
    CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    // Define user authentication credentials that will be used with the host
    AuthScope authScope = new AuthScope(httpHost.getHostName(),
        httpHost.getPort());
    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
        username, password);
    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(authScope, credentials);
    
    // Create AuthCache instance
    AuthCache authCache = new BasicAuthCache();

    // Generate BASIC scheme object and add it to the local auth cache
    BasicScheme basicAuth = new BasicScheme();
    authCache.put(httpHost, basicAuth);

    // Add AuthCache to the execution context
    HttpClientContext context = HttpClientContext.create();
    context.setCredentialsProvider(credentialsProvider);
    context.setAuthCache(authCache);

    HttpGet httpGet = new HttpGet(this.LOGIN_URL);
    HttpResponse response = null;
    Header[] headers = null;
    Integer statusCode = null;

    try {

      response = httpClient.execute(httpHost, httpGet, context);
      headers = response.getAllHeaders();
      statusCode = (Integer) response.getStatusLine().getStatusCode();
      logger.info("STATUS: " + statusCode.toString());

    } catch (UnsupportedEncodingException e) {
      logger.error(e);
      e.printStackTrace();
    } catch (ClientProtocolException e) {
      logger.error(e);
      e.printStackTrace();
    } catch (IOException e) {
      logger.error(e);
      e.printStackTrace();
    } finally {
		closeHttpClient(httpClient);
    }

    if (statusCode == HttpStatus.SC_OK) {

      String headerName = null;
      String headerValue = null;

      Header cookie = response.getFirstHeader("Set-Cookie");
      if (cookie != null) {
        headerValue = cookie.getValue();
        token = this.getAuthToken(headerValue);
      }

    } else if (statusCode == TEAPOT) {
      token = String.valueOf(TEAPOT);
    }

    return token;
  }

  /**
   * Parse the "Set-Cookie" header looking for the "auth-token" key-value pair
   * and return the base64 encrypted token value.
   * 
   * @param setCookieHeader
   *          The full "Set-Cookie" header.
   * 
   * @return The "auth-token" value as a String object.
   */
  private String getAuthToken(String setCookieHeader) {

    String authToken = null;

    String[] headerParts = setCookieHeader.split(";");

    for (int i = 0; i < headerParts.length; i++) {

      // Extract token value from the key-value pair.
      if (headerParts[i].startsWith("auth-token=")) {
        int start = "auth-token=".length();
        int end = headerParts[i].length();
        authToken = headerParts[i].substring(start, end);
      }

    }

    return authToken;

  }

}
