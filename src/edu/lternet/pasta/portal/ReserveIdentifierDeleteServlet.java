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

import edu.lternet.pasta.client.DataPackageManagerClient;


/**
 * @author Duane Costa
 * 
 * Servlet to manage end user deletion (i.e. release) of a reservation on 
 * a data package identifier. When an end user deletes (or releases) their
 * reservation on a data package identifier, the reservation record is 
 * deleted from the reservations table in PASTA.
 */
public class ReserveIdentifierDeleteServlet extends DataPortalServlet {

  /**
   * Class variables
   */

  private static final Logger logger = Logger.getLogger(edu.lternet.pasta.portal.ReserveIdentifierDeleteServlet.class);
  private static final long serialVersionUID = 1L;
  private static final String forward = "./reservations.jsp";

  private static String publicId;

  
  /**
   * Constructor of the object.
   */
  public ReserveIdentifierDeleteServlet() {
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
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String deleteMessage = null;
		String messageType = null;
		HttpSession httpSession = request.getSession();

		String uid = (String) httpSession.getAttribute("uid");

		if (uid == null || uid.isEmpty()) {
            uid = publicId;
        }

		String docid = request.getParameter("docid");
		if (docid != null) {
			String[] tokens = docid.split("\\.");
			if (tokens != null && tokens.length >= 2) {
				String scope = tokens[0];
				String identifierStr = tokens[1];
				Integer identifier = Integer.parseInt(identifierStr);

				if (uid.equals(publicId)) {
					messageType = "warning";
					request.setAttribute("message", LOGIN_WARNING);
				} 
				else {
					try {
						DataPackageManagerClient dpmc = new DataPackageManagerClient(uid);
						dpmc.deleteReservation(scope, identifier);
						deleteMessage = 
						  String.format("Reservation for data package identifier '<b>%s</b>' has been deleted.",
						  docid);
						messageType = "info";
					} 
					catch (Exception e) {
						handleDataPortalError(logger, e);
					}
				}
			}
		}

		request.setAttribute("deleteMessage", deleteMessage);
		request.setAttribute("messageType", messageType);

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
      publicId = options.getString("edi.public.id");
  }

}
