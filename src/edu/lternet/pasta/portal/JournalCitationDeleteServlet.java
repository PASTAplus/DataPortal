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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import edu.lternet.pasta.client.JournalCitationsClient;


public class JournalCitationDeleteServlet extends DataPortalServlet {

  /**
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.portal.JournalCitationDeleteServlet.class);
  private static final long serialVersionUID = 1L;

  private static final String forward = "./journalCitations.jsp";

  /**
   * Constructor of the object.
   */
  public JournalCitationDeleteServlet() {
    super();
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
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession httpSession = request.getSession();

    String uid = (String) httpSession.getAttribute("uid");

    if (uid == null || uid.isEmpty())
      uid = "public";

    String journalCitationId = request.getParameter("journalcitationid");

    String msg = null;
    String type = null;

    if (uid.equals("public")) {
      msg = LOGIN_WARNING;
      type = "warning";
    } 
    else {
        
      try {
        JournalCitationsClient journalCitationsClient = new JournalCitationsClient(uid);
        //journalCitationsClient.deleteByJournalCitationId(journalCitationId);
        msg = String.format("Journal citation entry with identifier '<b>%s</b>' has been deleted.", 
                                journalCitationId);
        type = "info";
      } 
      catch (Exception e) {
          handleDataPortalError(logger, e);
      }

    }

    request.setAttribute("deletemessage", msg);
    request.setAttribute("type", type);

    RequestDispatcher requestDispatcher = request.getRequestDispatcher(forward);
    requestDispatcher.forward(request, response);

  }

  /**
   * Initialization of the servlet. <br>
   * 
   * @throws ServletException
   *           if an error occurs
   */
  public void init() throws ServletException {

    PropertiesConfiguration options = ConfigurationListener.getOptions();

  }

}
