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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.lternet.pasta.client.PastaImATeapotException;
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

  
  /**
   * Class methods
   */


  // Instance variables
  private final String authServer;
  private final String authTarget;
  private final String dataportalTarget;


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
    String cname = request.getParameter("cname");

    if (extToken != null && cname != null) { // Other 3rd party login
        tokenManager = new TokenManager(extToken);
        try {
            tokenManager.storeToken();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        distinguishedName = tokenManager.getUid();

    } else { // PASTA login

        uid = request.getParameter("uid");
//        String affiliation = request.getParameter("affiliation");
        String affiliation = "EDI";

        if (uid != null) {
            uid = uid.trim();
            cname = uid;
        }

        String password = request.getParameter("password");

        try {

          distinguishedName = PastaClient.composeDistinguishedName(uid, affiliation);
          new LoginClient(distinguishedName, password);
          extToken = TokenManager.getExtToken(distinguishedName);
          tokenManager = new TokenManager(extToken);

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
            e.printStackTrace();
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
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).equals("authenticated")) {
                authenticated = true;
            }
            if (groups.get(i).equals("vetted")) {
                vetted = true;
            }
        }
        httpSession.setAttribute("authenticated", authenticated);
        httpSession.setAttribute("vetted", vetted);
        httpSession.setAttribute("uid", distinguishedName);
        httpSession.setAttribute("cname", cname);

        httpSession.setMaxInactiveInterval(60 * 60 * 12);
        logger.info(String.format("Session %s: Logged in session MaxInactiveInterval set to 12 hours", httpSession.getId()));
    }

    /* Allows redirect back to page that forced a login action */
    if (forward == null || forward.isEmpty()) {
        forward = "./home.jsp";
    }

    try {
        extToken = TokenManager.getExtToken(distinguishedName);
        TokenManager tm = new TokenManager(extToken);
        logger.info(tm.getToken());
        logger.info(tm.getUid());
        logger.info(tm.getAuthSystem());
        logger.info(tm.getTtl());

        ArrayList<String> groups = tm.getGroups();

        for (String group : groups) {
            logger.info(group);
        }

        logger.info(tm.getSignature());

    }
    catch (ClassNotFoundException | SQLException e) {
        e.printStackTrace();
    }

//  TODO: remove following lines once confirmed their removal does not cause unwanted side-effects
//  RequestDispatcher requestDispatcher = request.getRequestDispatcher(forward);
//  requestDispatcher.forward(request, response);

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

  }

}
