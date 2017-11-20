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

import org.apache.commons.lang3.text.StrTokenizer;
import org.apache.log4j.Logger;

import edu.lternet.pasta.client.DataPackageManagerClient;
import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;

public class UserBrowseServlet extends DataPortalServlet {

  /**
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.portal.UserBrowseServlet.class);
  private static final long serialVersionUID = 1L;

  /**
   * Constructor of the object.
   */
  public UserBrowseServlet() {
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
      throws ServletException, IOException {

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
		HttpSession httpSession = request.getSession();
		String uid = (String) httpSession.getAttribute("uid");
		String distinguishedName = (String) httpSession.getAttribute("distinguishedName");
		String forward = null;
		String browseMessage = "View a data package you have uploaded.";

		if (uid == null || uid.isEmpty() || uid.equals("public") || distinguishedName == null
				|| distinguishedName.isEmpty()) {
			String message = LOGIN_WARNING;
			forward = "./login.jsp";
			request.setAttribute("message", message);
			request.setAttribute("from", "userBrowseServlet");
		} 
		else {
			forward = "./dataPackageBrowser.jsp";

			String text = null;
			String html = null;
			Integer count = 0;

			try {

				DataPackageManagerClient dpmClient = new DataPackageManagerClient(uid);
				text = dpmClient.listUserDataPackages(distinguishedName);
				StrTokenizer tokens = new StrTokenizer(text);
				html = "<ol>\n";
				EmlPackageIdFormat epif = new EmlPackageIdFormat();

				while (tokens.hasNext()) {
					String packageId = tokens.nextToken();
					EmlPackageId epid = epif.parse(packageId);
					String scope = epid.getScope();
					Integer identifier = epid.getIdentifier();
					Integer revision = epid.getRevision();
					html += String.format(
							"<li><a class=\"searchsubcat\" href=\"./mapbrowse?scope=%s&identifier=%d&revision=%d\">%s</a></li>\n",
							scope, identifier, revision, packageId);
					count++;
				}

				html += "</ol>\n";
				request.setAttribute("html", html);
				request.setAttribute("count", count.toString());
				if (count < 1) {
					browseMessage = String.format("No data packages have been uploaded by user '%s'.",
							                      distinguishedName);
				}
				request.setAttribute("browsemessage", browseMessage);
			} catch (Exception e) {
				handleDataPortalError(logger, e);
			}
		}

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
    // Put your code here
  }

}
