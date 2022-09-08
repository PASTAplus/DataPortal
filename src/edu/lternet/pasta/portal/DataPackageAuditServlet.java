/*
 *
 * $Date: 2012-08-30 09:55:43 -0700 (Thu, 30 Aug 2012) $
 * $Author: dcosta $
 * $Revision: 2325 $
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
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Objects;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.lternet.pasta.client.*;
import edu.lternet.pasta.common.MyPair;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;


public class DataPackageAuditServlet extends DataPortalServlet {

  /**
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.portal.DataPackageAuditServlet.class);
  private static final long serialVersionUID = 1L;

  private static String cwd = null;
  private static String limit = null;
  private static String xslpath = null;

  private static final String PACKAGE = "readDataPackage";
  private static final String METADATA = "readMetadata";
  private static final String REPORT = "readDataPackageReport";
  private static final String ENTITY = "readDataEntity";

  /**
   * Constructor of the object.
   */
  public DataPackageAuditServlet() {
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
   * <p>
   * This method is called when a form has its tag value method equals to post.
   *
   * @param request  the request send by the client to the server
   * @param response the response send by the server to the client
   * @throws ServletException if an error occurred
   * @throws IOException      if an error occurred
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    AuditManagerClient auditManagerClient = null;
    HttpSession httpSession = request.getSession();
    String xml = null;
    StringBuffer filter = new StringBuffer();
    String message = null;
    String uid = (String) httpSession.getAttribute("uid");

    if (uid == null || uid.isEmpty()) {
      request.setAttribute("reportMessage", LOGIN_WARNING);
      RequestDispatcher requestDispatcher = request.getRequestDispatcher("./login.jsp");
      requestDispatcher.forward(request, response);
      return;
    }

    try {
      auditManagerClient = new AuditManagerClient(uid);
    } catch (PastaAuthenticationException | PastaConfigurationException e) {
      handleDataPortalError(logger, e);
      return;
    }

    String pastaUriHead = auditManagerClient.getPastaUriHead();

    //
    // Process filter parameters
    //

    // Encode empty request parameters with SQL regex string "%"
    String scope = "%25";
    String identifier = "%25";
    String revision = "%25";
    String resourceId = null;

    String value = "";

    value = request.getParameter("scope");
    if (value != null && !value.isEmpty()) {
      scope = value;
    }

    value = request.getParameter("identifier");
    if (value != null && !value.isEmpty()) {
      identifier = value;
    }

    value = request.getParameter("revision");
    if (value != null && !value.isEmpty()) {
      revision = value;
    }

    String packageId = scope + "." + identifier + "." + revision;

    String begin = (String) request.getParameter("begin");
    if (begin != null && !begin.isEmpty()) {
      filter.append("fromTime=" + begin + "&");
    }

    String end = (String) request.getParameter("end");
    if (end != null && !end.isEmpty()) {
      filter.append("toTime=" + end + "&");
    }

    filter.append("category=info&");

    Boolean packageResource = getBooleanParameter(request, "package", false);
    Boolean metadataResource = getBooleanParameter(request, "metadata", false);
    Boolean dataResource = getBooleanParameter(request, "entity", false);
    Boolean reportResource = getBooleanParameter(request, "report", false);

    boolean includeAllResources = false;

    if (!(packageResource || metadataResource || dataResource || reportResource)) {
      includeAllResources = true;
    }

    if (packageResource || includeAllResources) {
      filter.append("serviceMethod=readDataPackage&");
      resourceId = getResourceId(pastaUriHead, packageId, PACKAGE);
      filter.append("resourceId=" + resourceId + "&");
    }

    if (metadataResource || includeAllResources) {
      filter.append("serviceMethod=readMetadata&");
      resourceId = getResourceId(pastaUriHead, packageId, METADATA);
      filter.append("resourceId=" + resourceId + "&");
    }

    if (dataResource || includeAllResources) {
      filter.append("serviceMethod=readDataEntity&");
      resourceId = getResourceId(pastaUriHead, packageId, ENTITY);
      filter.append("resourceId=" + resourceId + "&");
    }

    if (reportResource || includeAllResources) {
      filter.append("serviceMethod=readDataPackageReport&");
      resourceId = getResourceId(pastaUriHead, packageId, REPORT);
      filter.append("resourceId=" + resourceId + "&");
    }

    String affiliation = (String) request.getParameter("affiliation");
    if (affiliation == null || affiliation.isEmpty()) {
      affiliation = "LTER";
    }

    String userIdParam = (String) request.getParameter("userId");
    if (userIdParam != null && !userIdParam.isEmpty()) {
      String userParam = "public";
      if (!userIdParam.equalsIgnoreCase(userParam)) {
        userParam = PastaClient.composeDistinguishedName(userIdParam, affiliation);
      }
      filter.append("user=" + userParam + "&");
    }

    String userAgentParam = (String) request.getParameter("userAgent");
    if (userAgentParam != null && !userAgentParam.isEmpty()) {
      String userAgentNegateParam = (String) request.getParameter("userAgentNegate");
      if (Objects.equals(userAgentNegateParam, "1")) {
        if (filter.length() == 0) {
          filter.append("userAgentNegate=" + userAgentParam);
        }
        else {
          filter.append("&userAgentNegate=" + userAgentParam);
        }
      }
      else {
        if (filter.length() == 0) {
          filter.append("userAgent=" + userAgentParam);
        }
        else {
          filter.append("&userAgent=" + userAgentParam);
        }
      }
    }

    String includeRobotsParam = (String) request.getParameter("includeRobots");
    if (Objects.equals(includeRobotsParam, "1")) {
      if (filter.length() == 0) {
          filter.append("robots");
        }
        else {
          filter.append("&robots");
        }
      }

    boolean isDownload = getBooleanParameter(request, "download", false);
    if (isDownload) {
      InputStream inputStream = null;
      try {
        inputStream = auditManagerClient.reportByFilterCsv(filter.toString());
      } catch (PastaEventException e) {
        handleDataPortalError(logger, e);
      }
      response.setHeader("content-disposition", "attachment; filename=auditreport.csv");
      OutputStream outputStream = response.getOutputStream();
      byte[] buf = new byte[8192];
      int length;
      while ((length = inputStream.read(buf)) > 0) {
        outputStream.write(buf, 0, length);
      }
      outputStream.flush();
      return;
    }

    if (limit != null && !limit.isEmpty()) {
      if (filter.length() == 0) {
        filter.append("limit=" + limit);
      }
      else {
        filter.append("&limit=" + limit);
      }
    }

    // startRowId

    String startRowIdParam = (String) request.getParameter("startRowId");
    if (startRowIdParam == null || startRowIdParam.isEmpty()) {
      startRowIdParam = "0";
    }
    String getPrevParam = (String) request.getParameter("getPrev");
    if (Objects.equals(getPrevParam, "1")) {
      if (filter.length() == 0) {
        filter.append("roid=" + startRowIdParam);
      }
      else {
        filter.append("&roid=" + startRowIdParam);
      }
    }
    else {
      if (filter.length() == 0) {
        filter.append("startOid=" + startRowIdParam);
      }
      else {
        filter.append("&startOid=" + startRowIdParam);
      }
    }

    MyPair<String, MyPair<Integer, Integer>> pair = null;
    try {
      pair = auditManagerClient.reportByFilter(filter.toString());
    } catch (PastaEventException e) {
      e.printStackTrace();
    }
    xml = pair.t;

    ReportUtility reportUtility = null;
    try {
      reportUtility = new ReportUtility(xml);
    } catch (ParseException e) {
      handleDataPortalError(logger, e);
    }

    message = reportUtility.xmlToHtmlTable(cwd + xslpath);

    request.setAttribute("reportMessage", message);
    request.setAttribute("firstRowId", pair.u.t);
    request.setAttribute("lastRowId", pair.u.u);
    request.setAttribute("serviceMethod", "");
    request.setAttribute("debug", "");
    request.setAttribute("info", "");
    request.setAttribute("warn", "");
    request.setAttribute("error", "");
    request.setAttribute("code", "");
    request.setAttribute("userId", userIdParam == null ? "" : userIdParam);
    request.setAttribute("affiliation", affiliation);
    request.setAttribute("beginDate", "");
    request.setAttribute("beginTime", "");
    request.setAttribute("endDate", "");
    request.setAttribute("endTime", "");

    request.setAttribute("scope", scope);
    request.setAttribute("identifier", identifier);
    request.setAttribute("revision", revision);

    request.setAttribute("package", packageResource ? "1" : "0");
    request.setAttribute("metadata", metadataResource ? "1" : "0");
    request.setAttribute("entity", dataResource ? "1" : "0");
    request.setAttribute("report", reportResource ? "1" : "0");

    request.setAttribute("userAgent", userAgentParam);
    request.setAttribute("userAgentNegate", "0");
    
    request.setAttribute("includeRobots", "0");

    request.setAttribute("pageIdx", getIntegerParameter(request, "pageIdx", 0));

    String forward = "./auditReportTable.jsp";
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
    // limits the number of audit records returned
    limit = options.getString("auditreport.limit");

    xslpath = options.getString("datapackageaudit.xslpath");
    cwd = options.getString("system.cwd");
  }

  private String getResourceId(String pastaUriHead, String packageId, String serviceMethod) {

  	String resourceId = null;

  	String [] packageParts = packageId.split("\\.");

		if (packageParts.length == 3) {
			if (serviceMethod.equals(PACKAGE)) {
				resourceId = pastaUriHead + "eml/" + packageParts[0] + "/" + packageParts[1]
			    + "/" + packageParts[2];
			} else if (serviceMethod.equals(METADATA)) {
				resourceId = pastaUriHead + "metadata/eml/" + packageParts[0] + "/" + packageParts[1]
				    + "/" + packageParts[2];
			} else if (serviceMethod.equals(REPORT)) {
				resourceId = pastaUriHead + "report/eml/" + packageParts[0] + "/" + packageParts[1]
				    + "/" + packageParts[2];
			} else { //ENTITY
				resourceId = pastaUriHead + "data/eml/" + packageParts[0] + "/" + packageParts[1]
				    + "/" + packageParts[2] + "/%25";
			}
		}

  	return resourceId;

  }

  /**
   * Get a String parameter, with a fallback value. Never throws an exception.
   * Can pass a distinguished value to default to enable checks of whether it was supplied.
   *
   * @param request    current HTTP request
   * @param name       the name of the parameter
   * @param defaultVal the default value to use as fallback
   */
  public static String getStringParameter(HttpServletRequest request, String name,
                                          String defaultVal)
  {
    String v = request.getParameter(name);
    return (v != null ? v : defaultVal);
  }

  /**
   * Get an Integer parameter, with a fallback value. Never throws an exception.
   * Can pass a distinguished value to default to enable checks of whether it was supplied.
   *
   * @param request    current HTTP request
   * @param name       the name of the parameter
   * @param defaultVal the default value to use as fallback
   */
  public static Integer getIntegerParameter(HttpServletRequest request, String name, Integer defaultVal)
  {
    String v = getStringParameter(request, name, defaultVal.toString());
    try {
      return Integer.valueOf(v);
    } catch (NumberFormatException e) {
      return defaultVal;
    }
  }

  /**
   * Get a Boolean parameter, with a fallback value. Never throws an exception.
   *
   * @param request    current HTTP request
   * @param name       the name of the parameter
   * @param defaultVal the default value to use as fallback
   */
  public static Boolean getBooleanParameter(HttpServletRequest request, String name, Boolean defaultVal)
  {
    String v = getStringParameter(request, name, "");
    if (v.equals("")) {
      return defaultVal;
    }
    return v.equalsIgnoreCase(name) || v.equals("1");
  }
}
