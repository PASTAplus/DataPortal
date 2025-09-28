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
import edu.lternet.pasta.client.ReservationsManager;


/**
 * 
 * @author Duane Costa
 * 
 * Servlet to manage end user reservations on data package identifiers.
 *
 */
public class ReserveIdentifierServlet extends DataPortalServlet {

  /**
   * Class variables
   */

  private static final Logger logger = Logger.getLogger(edu.lternet.pasta.portal.ReserveIdentifierServlet.class);
  private static final long serialVersionUID = 1L;
  private static final String forward = "./reservations.jsp";

  private static String publicId;

  /**
   * Constructor of the object.
   */
  public ReserveIdentifierServlet() {
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
		if (uid == null || uid.isEmpty()) {
            uid = publicId;
        }
		String scope = request.getParameter("scope");
		String numberStr = request.getParameter("numberOfIdentifiers");
		int numberOfIdentifiers = Integer.parseInt(numberStr);
		String message = null;
		String type = "info";

		if (uid.equals(publicId)) {
			message = LOGIN_WARNING;
			type = "warning";
		} 
		else {
			try {
				DataPackageManagerClient dpmc = new DataPackageManagerClient(uid);
				StringBuilder sb = new StringBuilder("");
				for (int i = 1; i <= numberOfIdentifiers; i++) {
					String identifier = dpmc.createReservation(scope);
					sb.append(String.format("<code>%s.%s.1</code>", scope, identifier));
					if (i < numberOfIdentifiers) { 
						sb.append(", "); 
						Thread.sleep(200); 
					}
				}
					
				String identifiersList = sb.toString();
				if (numberOfIdentifiers == 1) {
					message = String.format("The following identifier has been reserved for user %s: %s", 
					 	               		uid, identifiersList);
				}
				else {
					message = String.format("The following %d identifiers have been reserved for user %s: %s", 
			                				numberOfIdentifiers, uid, identifiersList);
				}
			} 
			catch (Exception e) {
				handleDataPortalError(logger, e);
			}
		}

		request.setAttribute("reservationMessage", message);
		request.setAttribute("type", type);
		RequestDispatcher requestDispatcher = request.getRequestDispatcher(forward);
		requestDispatcher.forward(request, response);
	}

	
	/**
	 * Initialization of the servlet. <br>
	 * 
	 * @throws ServletException
	 *             if an error occurs
	 */
	public void init() throws ServletException {
		PropertiesConfiguration options = ConfigurationListener.getOptions();
        publicId = options.getString("edi.public.id");
        String propertyName = "dataportal.maxNumberOfReservations";
        String maxNumberOfReservations = options.getString(propertyName);
		if (maxNumberOfReservations != null && !maxNumberOfReservations.isEmpty()) {
			try {
				int maxNumber = Integer.parseInt(maxNumberOfReservations);
				// Sets the maximum number of reservations for one button click
				ReservationsManager.setMaxNumberOfReservations(maxNumber);
			}
			catch (NumberFormatException e) {
				logger.warn(String.format(
                        "Could not determine the value of property %s: %s",
						   propertyName, maxNumberOfReservations)
                );
			}
		}
	}

}
