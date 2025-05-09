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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.lternet.pasta.client.DataPackageManagerClient;
import edu.lternet.pasta.common.UserErrorException;

public class DataViewerServlet extends DataPortalServlet {

  /**
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.portal.DataViewerServlet.class);
  private static final long serialVersionUID = 1L;

  /**
   * Constructor of the object.
   */
  public DataViewerServlet() {
    super();
  }

  /**
   * Destruction of the servlet. <br>
   */
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
  public void doGet(HttpServletRequest request, HttpServletResponse response)
	  throws ServletException, IOException
  {
	  // Prevent GET method for from being handled as POST for this servlet.
	  response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "HTTP method not supported.");
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
		if (uid == null || uid.isEmpty()) uid = "public";
		String packageId = request.getParameter("packageid");
		String entityId = request.getParameter("entityid");
		String scope = null;
		Integer identifier = null;
		String revision = null;
		String[] tokens = packageId.split("\\.");

		try {
			if (tokens.length == 3 && entityId != null && !(entityId.isEmpty())) {
				scope = tokens[0];
				identifier = Integer.valueOf(tokens[1]);
				revision = tokens[2];

				String userAgent = request.getHeader("User-Agent");
				DataPackageManagerClient dpmClient = new DataPackageManagerClient(uid, userAgent);
				dpmClient.readDataEntity(scope, identifier, revision, entityId,
						response);
			}
			else {
				String message = 
						String.format(
								"packageId '%s' is not in the correct form of 'scope.identifier.revision' (e.g., knb-lter-lno.1.1)",
						        packageId);
		        throw new UserErrorException(message);
			}
		}
		catch (Exception e) {
			handleDataPortalError(logger, e);
		}
	}
  

  /**
   * Initialization of the servlet. <br>
   * 
   * @throws ServletException
   *           if an error occurs
   */
  public void init() throws ServletException {
    // Put your code here
  }

}
