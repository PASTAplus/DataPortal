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

import edu.lternet.pasta.client.*;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.stream.Collectors;

public class DoiServlet extends DataPortalServlet {
  private static final Logger logger = Logger.getLogger(DoiServlet.class);
  private static final long serialVersionUID = 1L;

  public DoiServlet()
  {
    super();
  }

  public void init() throws ServletException
  {
    PropertiesConfiguration options = ConfigurationListener.getOptions();
  }

  // GET: Get metadata by DOI
  public void doPost(
      HttpServletRequest request,
      HttpServletResponse response
  ) throws ServletException, IOException
  {
    request.setCharacterEncoding("UTF-8");
    // Method can only be called by authenticated users
    HttpSession httpSession = request.getSession();
    String uid = (String) httpSession.getAttribute("uid");
    if (uid == null || uid.isEmpty()) {
      plainTextError(
          response, HttpServletResponse.SC_FORBIDDEN,
          "Method can only be called by authenticated users"
      );
      return;
    }
    // Decode JSON body
    JSONObject json = getJson(request);
    // Get DOI metadata
    CrossrefClient crossrefClient;
    try {
      crossrefClient = new CrossrefClient(uid);
    } catch (PastaConfigurationException | PastaAuthenticationException e) {
      plainTextError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
      throw new RuntimeException(e);
    }
    try {
      JSONObject doiJson = crossrefClient.fetchByDoi(json.getString("doi"));
      response.setContentType("application/json");
      response.setStatus(HttpStatus.SC_OK);
      response.getWriter().write(doiJson.toString());
    } catch (CrossrefClientException e) {
      logger.error(e.toString());
      plainTextError(response, e.getStatus(), e.getMessage());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void plainTextError(
      HttpServletResponse response,
      Integer status,
      String message
  ) throws IOException
  {
    response.setContentType("text/plain");
    response.setStatus(status);
    response.getWriter().write(message);
  }

  // Decode request body containing JSON
  private JSONObject getJson(HttpServletRequest request) throws IOException
  {
    String jsonStr = request.getReader().lines().collect(
        Collectors.joining(System.lineSeparator()));
    JSONObject json = new JSONObject(jsonStr);
    return json;
  }
}
