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

package edu.lternet.pasta.portal;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.lternet.pasta.client.PastaImATeapotException;
import edu.lternet.pasta.common.edi.EdiToken;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import edu.lternet.pasta.client.LoginClient;
import edu.lternet.pasta.client.PastaAuthenticationException;
import edu.lternet.pasta.client.PastaClient;
import edu.lternet.pasta.token.TokenManager;

/**
 * The LoginServlet manages user authentication between the NIS Data Portal
 * web-browser interface and the NIS Data Portal/PASTA LoginService.
 * 
 * @author servilla
 * @since Mar 14, 2012
 * 
 */
public class LoginServlet extends DataPortalServlet {

  /**
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.portal.LoginServlet.class);
  private static final long serialVersionUID = 1L;
  private Integer maxInactiveIntervalMinutes;
  /**
   * Class methods
   */


  // Instance variables
  private final String authServer;
  private final String authTarget;
  private final String dataportalTarget;
  private final boolean ediUseAuth;
  private final String ediAuthenticatedId;
  private final String ediVettedId;


    /**
   * Utility method to derive the uid value from a full distringuished name value.
   * 
   * @param dn    
   *          the full distinguished name, e.g. "uid=jsmith,o=LTER,dc=ecoinformatics,dc=org"
   *            
   * @return  the uid value, e.g. "jsmith"
   */
  public static String uidFromDistinguishedName(String dn) {
      String uid = null;
      
      if (dn != null && dn.startsWith("uid=")) {
          String[] dnTokens = dn.split(",");
          if (dnTokens != null && dnTokens.length > 0) {
              String uidComponent = dnTokens[0];
              String[] uidTokens = uidComponent.split("=");
              if (uidTokens != null && uidTokens.length == 2) {
                  uid = uidTokens[1];
              }
          }
      }
      
      return uid;
  }

  
  /**
   * Constructor of the object.
   */
  public LoginServlet() {
    super();

      Configuration options = ConfigurationListener.getOptions();

      String authHost = options.getString("auth.hostname");
      String authProtocol = options.getString("auth.protocol");
      int authPort = options.getInt("auth.port");
      this.authTarget = options.getString("auth.target");
      this.authServer = PastaClient.composePastaUrl(authProtocol, authHost, authPort);
      this.ediUseAuth = Boolean.parseBoolean(options.getString("edi.auth.use"));
      this.ediAuthenticatedId = options.getString("edi.authenticated.id");
      this.ediVettedId = options.getString("edi.vetted.id");
      String dataportalHostName = options.getString("dataportal.hostname");
      String dataportalProtocol = options.getString("dataportal.protocol");
      int dataportalPort = options.getInt("dataportal.port");
      String dataportalContext = options.getString("dataportal.context");
      String dataportalHost = PastaClient.composePastaUrl(dataportalProtocol, dataportalHostName, dataportalPort);
      this.dataportalTarget = dataportalHost + "/" + dataportalContext;
  }

  /**
   * Destruction of the servlet. <br>
   */
  @Override
  public void destroy() {
    super.destroy(); // Just puts "destroy" string in log
    // Put your code here
  }

  /**
   * The doGet method of the servlet. <br>
   * 
   * This method is called when a form has its tag value method equals to get.
   * 
   * @param request
   *          the request send by the client to the server
   * @param response
   *          the response send by the server to the client
   * @throws ServletException
   *           if an error occurred
   * @throws IOException
   *           if an error occurred
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    // Pass request onto "doPost".
    doPost(request, response);

  }

  /**
   * The doPost method of the servlet. <br>
   * 
   * This method is called when a form has its tag value method equals to post.
   * 
   * @param request
   *          the request send by the client to the server
   * @param response
   *          the response send by the server to the client
   * @throws ServletException
   *           if an error occurred
   * @throws IOException
   *           if an error occurred
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String uid = null;
    String distinguishedName = null;
    TokenManager tokenManager = null;
    boolean isTeapot = false;

    HttpSession httpSession = request.getSession();

    // Set return to originating page, if set
    String forward = (String) httpSession.getAttribute("from");
    httpSession.removeAttribute("from");

    String extToken = request.getParameter("token");
    String ediToken = request.getParameter("edi_token");
    String cname = request.getParameter("common_name");
    String idProvider = request.getParameter("idp");
    String idProviderToken = request.getParameter("idp_token");
    String errorMsg = request.getParameter("error");

    if (errorMsg != null) {
        httpSession.setAttribute("message", errorMsg);
        response.sendRedirect("./login.jsp");
        return;
    }

    if (extToken != null && ediToken != null) { // Other 3rd party login
        HashMap<String, String> tokenSet = new HashMap<String, String>(2);
        tokenSet.put("auth-token", extToken);
        tokenSet.put("edi-token", ediToken);
        tokenManager = new TokenManager(tokenSet);
        try {
            tokenManager.storeToken();
        } catch (SQLException | ClassNotFoundException e) {
            logger.error(e.getMessage());
        }
        distinguishedName = tokenManager.getUid();

    } else { // EDI LDAP login

        uid = request.getParameter("uid");
        String affiliation = "EDI";

        if (uid != null) {
            uid = uid.trim();
            cname = uid;  // Set common name to uid value
        }

        String password = request.getParameter("password");

        try {

          distinguishedName = PastaClient.composeDistinguishedName(uid, affiliation);
          new LoginClient(distinguishedName, password);
          HashMap<String, String> tokenSet = TokenManager.getTokenSet(distinguishedName);
          tokenManager = new TokenManager(tokenSet);

        } catch (PastaAuthenticationException e) {
            String message = "<em>Login failed for user</em> " + uid;
            forward = "./login.jsp";
            httpSession.setAttribute("message", message);
        } catch (PastaImATeapotException e) {
            logger.error(e);
            String gripe = "I'm a teapot for " + distinguishedName;
            logger.error(gripe);
            isTeapot = true;
        } catch (SQLException | ClassNotFoundException e) {
            logger.error(e.getMessage());
        }

        if (isTeapot) {
            String acceptUrl = this.authServer + "/auth/accept?uid=" + distinguishedName + "&target=" + this.authTarget;
            response.sendRedirect(acceptUrl);
            return;
        }

    }

    boolean authenticated = false;
    boolean vetted = false;
    if (tokenManager != null) {
        ArrayList<String> groups = tokenManager.getGroups();
        for (String group : groups) {
            if (group.equals("authenticated")) {
                authenticated = true;
            }
            if (group.equals("vetted")) {
                vetted = true;
            }
        }
        if (this.ediUseAuth  && ediToken != null) {
            EdiToken et = new EdiToken(ediToken);
            List<String> principals = et.getPrincipals();
            authenticated = false;
            vetted = false;
            for (int i=0; i<principals.size(); i++) {
                if (principals.get(i).equals(ediAuthenticatedId)) {
                    authenticated = true;
                }
                if (principals.get(i).equals(ediVettedId)) {
                    vetted = true;
                }
            }
        }
        httpSession.setAttribute("authenticated", authenticated);
        httpSession.setAttribute("vetted", vetted);
        httpSession.setAttribute("uid", distinguishedName);
        httpSession.setAttribute("cname", cname);
        httpSession.setAttribute("idProvider", idProvider);
        httpSession.setAttribute("idProviderToken", idProviderToken);

        httpSession.setMaxInactiveInterval(maxInactiveIntervalMinutes * 60);
        logger.info(
            String.format("Session %s: Logged in session MaxInactiveInterval set to %d minutes",
                httpSession.getId(), maxInactiveIntervalMinutes
            )
        );
    }

    /* Allows redirect back to page that forced a login action */
    if (forward == null || forward.isEmpty()) {
        forward = "./home.jsp";
    }

    try {
        HashMap<String, String> tokenSet = TokenManager.getTokenSet(distinguishedName);
        tokenManager = new TokenManager(tokenSet);
        logger.info(tokenManager.getToken());
        logger.info(tokenManager.getUid());
        logger.info(tokenManager.getAuthSystem());
        logger.info(tokenManager.getTtl());
        ArrayList<String> groups = tokenManager.getGroups();
        for (String group : groups) {
            logger.info(group);
        }
        logger.info(tokenManager.getSignature());
    }
    catch (ClassNotFoundException | SQLException e) {
        logger.error(e);
    }

    forward = forward.replace("./", this.dataportalTarget + "/");
    response.sendRedirect(forward);
  }

  /**
   * Initialization of the servlet. <br>
   * 
   * @throws ServletException
   *           if an error occurs
   */
  @Override
  public void init() throws ServletException {
    PropertiesConfiguration options = ConfigurationListener.getOptions();
    this.maxInactiveIntervalMinutes = options.getInt("dataportal.session.timeout");
  }
}
