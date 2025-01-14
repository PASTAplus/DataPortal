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

import edu.lternet.pasta.client.DataPackageManagerClient;
import edu.lternet.pasta.client.ResultSetUtility;
import edu.lternet.pasta.portal.search.Search;
import edu.lternet.pasta.portal.search.SimpleSearch;
import edu.lternet.pasta.portal.search.TermsList;
import edu.lternet.pasta.portal.user.SavedData;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class SimpleSearchServlet extends DataPortalServlet {

  /*
   * Class variables
   */

  private static final Logger logger =
      Logger.getLogger(edu.lternet.pasta.portal.SimpleSearchServlet.class);
  private static final long serialVersionUID = 1L;

  private static String cwd = null;
  private static String xslpath = null;

  private static final String forward = "./searchResult.jsp";


  /*
   * Instance variables
   */


  /**
   * Constructor of the object.
   */
  public SimpleSearchServlet()
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
    ResultSetUtility resultSetUtility = null;
    String termsListHTML = "";
    String htmlTable = "";
    String mapButtonHTML = "";
    String relevanceHTML = "";
    String queryText = request.getQueryString();
    String sort = Search.DEFAULT_SORT;

    String q = (String) request.getParameter("q");

    if (q == null || q.equals("")) {
      doPost(request, response);
    }
    else {
      String uid = (String) httpSession.getAttribute("uid");
      if (uid == null || uid.isEmpty()) {
        uid = "public";
      }

      resultSetUtility = executeQuery(uid, queryText, sort);
      if (resultSetUtility != null) {
        mapButtonHTML = resultSetUtility.getMapButtonHTML();
        relevanceHTML = resultSetUtility.getRelevanceHTML();
        htmlTable = resultSetUtility.getHTMLTable();
      }

      httpSession.setAttribute("termsListHTML", termsListHTML);
      httpSession.setAttribute("queryText", queryText);

      dispatchRequest(request, response, mapButtonHTML, relevanceHTML, htmlTable);
    }
  }


  private void dispatchRequest(HttpServletRequest request, HttpServletResponse response,
                               String mapButtonHTML, String relevanceHTML,
                               String htmlTable) throws ServletException, IOException
  {
    request.setAttribute("mapButtonHTML", mapButtonHTML);
    request.setAttribute("relevanceHTML", relevanceHTML);
    request.setAttribute("searchResult", htmlTable);
    RequestDispatcher requestDispatcher = request.getRequestDispatcher(forward);
    requestDispatcher.forward(request, response);
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
    String termsListHTML = "";
    String htmlTable = "";
    String mapButtonHTML = "";
    String relevanceHTML = "";
    HttpSession httpSession = request.getSession();
    ResultSetUtility resultSetUtility = null;

    String uid = (String) httpSession.getAttribute("uid");
    if (uid == null || uid.isEmpty()) {
      uid = "public";
    }

    String termsParam = (String) request.getParameter("terms");
    String startParam = (String) request.getParameter("start");
    String rowsParam = (String) request.getParameter("rows");
    String sort = (String) request.getParameter("sort");
    int start, rows;

    if (startParam == null || startParam.equals("")) {
      start = Search.DEFAULT_START;
    }
    else {
      start = Integer.parseInt(startParam);
    }

    if (rowsParam == null || rowsParam.equals("")) {
      rows = Search.DEFAULT_ROWS;
    }
    else {
      rows = Integer.parseInt(rowsParam);
    }

    if (sort == null || sort.equals("")) {
      sort = Search.DEFAULT_SORT;
    }

    if (termsParam == null || termsParam.equals("")) {
      String queryText = null;
      String q = (String) request.getParameter("q");

      if (q == null || q.equals("")) {
        // if no q param was passed, look for query stored in the session
        queryText = (String) httpSession.getAttribute("queryText");
        termsListHTML = (String) httpSession.getAttribute("termsListHTML");
      }

      if (queryText != null) {
        queryText =
            String.format("%s&start=%d&rows=%d&sort=%s", queryText, start, rows, sort);
        resultSetUtility = executeQuery(uid, queryText, sort);
        if (resultSetUtility != null) {
          mapButtonHTML = resultSetUtility.getMapButtonHTML();
          relevanceHTML = resultSetUtility.getRelevanceHTML();
          htmlTable = resultSetUtility.getHTMLTable();
        }
      }
    }
    else {
      SimpleSearch simpleSearch = new SimpleSearch();
      String queryText = simpleSearch.buildSolrQuery(termsParam, false);
      TermsList termsList = simpleSearch.getTermsList();
      termsListHTML = termsList.toHTML("simpleSearch");
      httpSession.setAttribute("termsListHTML", termsListHTML);
      httpSession.setAttribute("queryText", queryText);
      queryText = String.format("%s&start=%d&rows=%d&sort=%s", queryText, 0,
          Search.DEFAULT_ROWS, sort);
      resultSetUtility = executeQuery(uid, queryText, sort);
      if (resultSetUtility != null) {
        mapButtonHTML = resultSetUtility.getMapButtonHTML();
        //relevanceHTML = resultSetUtility.getRelevanceHTML();
        htmlTable = resultSetUtility.getHTMLTable();
      }
    }

    dispatchRequest(request, response, mapButtonHTML, relevanceHTML, htmlTable);
  }


  /*
   * Executes the query via the DataPackageManagerClient object
   */
  private ResultSetUtility executeQuery(String uid, String queryText, String sort)
      throws ServletException
  {
    ResultSetUtility resultSetUtility = null;

    try {
      DataPackageManagerClient dpmClient = new DataPackageManagerClient(uid);
      String xml = dpmClient.searchDataPackages(queryText);
      if (uid.equals("public")) {
        resultSetUtility = new ResultSetUtility(xml, sort);
      }
      else {
        boolean isSavedDataPage = false;
        SavedData savedData = new SavedData(uid);
        resultSetUtility = new ResultSetUtility(xml, sort, savedData, isSavedDataPage);
      }

      resultSetUtility.xmlToHtmlTable(cwd + xslpath);
    } catch (Exception e) {
      handleDataPortalError(logger, e);
    }

    return resultSetUtility;
  }


  /**
   * Initialization of the servlet. <br>
   *
   * @throws ServletException if an error occurs
   */
  public void init() throws ServletException
  {
    PropertiesConfiguration options = ConfigurationListener.getOptions();
    xslpath = options.getString("resultsetutility.xslpath");
    logger.debug("XSLPATH: " + xslpath);
    cwd = options.getString("system.cwd");
    logger.debug("CWD: " + cwd);
  }


}
