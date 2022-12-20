/*
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
 */

package edu.lternet.pasta.portal;

import edu.lternet.pasta.client.DataPackageManagerClient;
import edu.lternet.pasta.client.PastaAuthenticationException;
import edu.lternet.pasta.client.PastaConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DownloadSearchServlet extends DataPortalServlet {

  /*
   * Class variables
   */

  private static final Logger logger = Logger.getLogger(DownloadSearchServlet.class);
  private static final long serialVersionUID = 1L;

  private static String cwd = null;
  private static String xslPath = null;

  /*
   * Instance variables
   */


  /**
   * Constructor of the object.
   */
  public DownloadSearchServlet()
  {
    super();
  }


  /**
   * Destruction of the servlet. <br>
   */
  public void destroy()
  {
    super.destroy(); // Just puts "destroy" string in log
    // Put your code here
  }


  /**
   * The doGet method of the servlet. <br>
   * <p>
   * This method is called when a form has its tag value method equals to get.
   *
   * @param request  the request send by the client to the server
   * @param response the response send by the server to the client
   * @throws ServletException if an error occurred
   * @throws IOException      if an error occurred
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    HttpSession httpSession = request.getSession();

    String solrQuery = request.getParameter("q");

    InputStream inputStream;

    String uid = (String) httpSession.getAttribute("uid");
    if (uid == null || uid.isEmpty()) {
      uid = "public";
    }
    DataPackageManagerClient dpmClient;
    try {
      dpmClient = new DataPackageManagerClient(uid);
    } catch (PastaAuthenticationException | PastaConfigurationException e) {
      throw new RuntimeException(e);
    }
    try {
      inputStream = dpmClient.searchDataPackagesStream(solrQuery);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    response.setHeader("content-disposition",
        "attachment; filename=search-results.csv");

    OutputStream outputStream = response.getOutputStream();

    byte[] buf = new byte[8192];
    int length;
    while ((length = inputStream.read(buf)) > 0) {
      outputStream.write(buf, 0, length);
    }
    outputStream.flush();
  }

  /**
   * Initialization of the servlet. <br>
   *
   * @throws ServletException if an error occurs
   */
  public void init() throws ServletException
  {
    PropertiesConfiguration options = ConfigurationListener.getOptions();
    xslPath = options.getString("resultsetutility.xslpath");
    logger.debug("XSL PATH: " + xslPath);
    cwd = options.getString("system.cwd");
    logger.debug("CWD: " + cwd);
  }
}
