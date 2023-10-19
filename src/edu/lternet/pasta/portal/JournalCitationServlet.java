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

import edu.lternet.pasta.client.JournalCitationsClient;
import edu.lternet.pasta.client.PastaAuthenticationException;
import edu.lternet.pasta.client.PastaConfigurationException;
import edu.lternet.pasta.client.PastaEventException;
import edu.lternet.pasta.common.JournalCitation;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Collectors;

public class JournalCitationServlet extends DataPortalServlet {
  private static final Logger logger = Logger.getLogger(JournalCitationServlet.class);
  private static final long serialVersionUID = 1L;

  public JournalCitationServlet()
  {
    super();
  }

  public void init() throws ServletException
  {
    PropertiesConfiguration options = ConfigurationListener.getOptions();
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException
  {
    request.setCharacterEncoding("UTF-8");
    // Method can only be called by authenticated users
    HttpSession httpSession = request.getSession();
    String uid = (String) httpSession.getAttribute("uid");
    if (uid == null || uid.isEmpty()) {
      plainTextError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied");
      return;
    }
    String journalCitationId = request.getParameter("journalCitationId");
    try {
      JournalCitationsClient journalCitationsClient = new JournalCitationsClient(uid);
      String journalCitationXml = journalCitationsClient.getCitationWithId(journalCitationId);
      JournalCitation journalCitation = new JournalCitation(journalCitationXml);
      String journalCitationJson = journalCitation.toJson().toString();
      response.getWriter().write(journalCitationJson);
    } catch (PastaEventException e) {
      logger.error(e.getMessage(), e);
      plainTextError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } catch (PastaConfigurationException | PastaAuthenticationException e) {
      plainTextError(response, HttpServletResponse.SC_BAD_REQUEST, "Internal error");
      throw new RuntimeException(e);
    }
  }

  // POST: Create a new journal citation
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    request.setCharacterEncoding("UTF-8");
    // Method can only be called by authenticated users
    HttpSession httpSession = request.getSession();
    String uid = (String) httpSession.getAttribute("uid");
    if (uid == null || uid.isEmpty()) {
      plainTextError(response, HttpServletResponse.SC_FORBIDDEN, "Method can only be called by authenticated users");
    }
    // Decode JSON body
    JSONObject json = getJson(request);
    // Make sure we're not trying to create a new citation while we already have a journalCitationId.
    assert Objects.equals(json.getString("journalCitationId"), "");
    // Validate input
    String validationResult = validateInput(json);
    if (validationResult != null) {
      plainTextError(response, HttpServletResponse.SC_BAD_REQUEST, validationResult);
      return;
    }
    // Create the journal citation
    JournalCitation journalCitation;
    try {
      journalCitation = new JournalCitation(json);
    }
    catch (ParseException e) {
      plainTextError(response, HttpServletResponse.SC_BAD_REQUEST, "Internal error");
      throw new RuntimeException(e);
    }
    String journalCitationXML = journalCitation.toXML(true);
    JournalCitationsClient journalCitationsClient;
    try {
      journalCitationsClient = new JournalCitationsClient(uid);
    }
    catch (PastaConfigurationException | PastaAuthenticationException e) {
      plainTextError(response, HttpServletResponse.SC_BAD_REQUEST, "Internal error");
      throw new RuntimeException(e);
    }
    try {
      Integer journalCitationId = journalCitationsClient.create(journalCitationXML);
      plainTextSuccess(response, journalCitationId);
    } catch (PastaEventException e ) {
      logger.error(e.getMessage(), e);
      plainTextError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }
  }

  // PUT: Update an existing journal citation
  public void doPut(HttpServletRequest request, HttpServletResponse response)
      throws IOException
  {
    request.setCharacterEncoding("UTF-8");
    // Method can only be called by authenticated users
    HttpSession httpSession = request.getSession();
    String uid = (String) httpSession.getAttribute("uid");
    if (uid == null || uid.isEmpty()) {
      plainTextError(response, HttpServletResponse.SC_FORBIDDEN, "");
      return;
    }
    // Decode JSON body
    JSONObject json = getJson(request);
    // Make sure we have the journalCitationId to update.
    assert !Objects.equals(json.getString("journalCitationId"), "");
    // Validate input
    String validationResult = validateInput(json);
    if (validationResult != null) {
      plainTextError(response, HttpServletResponse.SC_BAD_REQUEST, validationResult);
      return;
    }
    // Update the journal citation
    JournalCitation journalCitation;
    try {
      journalCitation = new JournalCitation(json);
    } catch (ParseException e) {
      plainTextError(response, HttpServletResponse.SC_BAD_REQUEST, "Internal error");
      throw new RuntimeException(e);
    }
    journalCitation.setDateCreated(LocalDateTime.now());
    String journalCitationXML = journalCitation.toXML(true);
    JournalCitationsClient journalCitationsClient;
    try {
      journalCitationsClient = new JournalCitationsClient(uid);
    } catch (PastaConfigurationException | PastaAuthenticationException e) {
      plainTextError(response, HttpServletResponse.SC_BAD_REQUEST, "Internal error");
      throw new RuntimeException(e);
    }
    try {
      Integer journalCitationId = journalCitationsClient.update(journalCitationXML);
      plainTextSuccess(response, journalCitationId);
    } catch (PastaEventException e) {
      logger.error(e.getMessage(), e);
      plainTextError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }
  }

  // DELETE: Delete an existing journal citation
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException
  {
    request.setCharacterEncoding("UTF-8");
    // Method can only be called by authenticated users
    HttpSession httpSession = request.getSession();
    String uid = (String) httpSession.getAttribute("uid");
    if (uid == null || uid.isEmpty()) {
      plainTextError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied");
      return;
    }
    // Decode JSON body
    JSONObject json = getJson(request);
    // Make sure we received a journalCitationId to delete.
    String journalCitationId = json.getString("journalCitationId");
    assert !Objects.equals(journalCitationId, "");
    try {
      JournalCitationsClient journalCitationsClient = new JournalCitationsClient(uid);
      journalCitationsClient.deleteByJournalCitationId(journalCitationId);
      plainTextSuccess(response, Integer.parseInt(journalCitationId));
    } catch (PastaEventException e) {
      logger.error(e.getMessage(), e);
      plainTextError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } catch (PastaConfigurationException | PastaAuthenticationException e) {
      plainTextError(response, HttpServletResponse.SC_BAD_REQUEST, "Internal error");
      throw new RuntimeException(e);
    }
  }

  // Calling response.sendError() causes the response to be wrapped in HTML,
  // so we create a plain text response manually.
  private void plainTextSuccess(HttpServletResponse response, Integer journalCitationId)
      throws IOException
  {
    response.setContentType("text/plain");
    response.setStatus(200);
    String json = "{\"journalCitationId\": " + journalCitationId + "}";
    response.getWriter().write(json);
  }

  private void plainTextError(HttpServletResponse response, Integer status, String message)
      throws IOException
  {
    response.setContentType("text/plain");
    response.setStatus(status);
    response.getWriter().write(message);
  }

  // Decode request body containing JSON
  private JSONObject getJson(HttpServletRequest request) throws IOException
  {
    String jsonStr =
        request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
    JSONObject json = new JSONObject(jsonStr);
    return json;
  }

  // Validate user input. Returns null if validation succeeded and a string if
  // validation failed.
  private String validateInput(JSONObject json)
  {
    String doi = json.getString("articleDoi");
    String url = json.getString("articleUrl");
    String relationType = json.getString("relationType");
    StringBuilder msgBuffer = new StringBuilder();
    if (doi.isEmpty() && url.isEmpty()) {
      msgBuffer.append("Either an article DOI or an article URL is required. ");
    }
    if (relationType.isEmpty()) {
      msgBuffer.append("RelationType is required. ");
    }
    if (!doi.isEmpty() && !doi.matches("10\\.\\d{4,9}/.+")) {
      msgBuffer.append("DOI format is invalid. ");
    }
    String msg = msgBuffer.toString();
    return msg.isEmpty() ? null : msg;
  }
}
