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
import edu.lternet.pasta.common.JournalCitation;


public class JournalCitationAddServlet extends DataPortalServlet {

  /**
   * Class variables
   */

  private static final Logger logger = Logger.getLogger(edu.lternet.pasta.portal.JournalCitationAddServlet.class);
  private static final long serialVersionUID = 1L;
  private static final String forward = "./journalCitations.jsp";

  private static String publicId;

  
  /**
   * Constructor of the object.
   */
  public JournalCitationAddServlet() {
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
        request.setCharacterEncoding("UTF-8");
        HttpSession httpSession = request.getSession();
        String uid = (String) httpSession.getAttribute("uid");
        if (uid == null || uid.isEmpty()) {
            uid = publicId;
        }
        String message = null;
        String messageType = "info";
        String createMessage = "";
        String packageId = request.getParameter("packageid");
        String articleDoi = request.getParameter("articledoi");
        String articleUrl = request.getParameter("articleurl");
        String articleTitle = request.getParameter("articletitle");
        String journalTitle = request.getParameter("journaltitle");
        String relationType = request.getParameter("relationtype");
        
        /*
         * Check for valid input
         */
        String inputMessage = validateInput(packageId, articleDoi, articleUrl, articleTitle, journalTitle, relationType);

        JournalCitation journalCitation = new JournalCitation();
        journalCitation.setPackageId(packageId);
        journalCitation.setArticleDoi(articleDoi);
        journalCitation.setArticleUrl(articleUrl);
        journalCitation.setArticleTitle(articleTitle);
        journalCitation.setJournalTitle(journalTitle);
        journalCitation.setRelationType(relationType);

        boolean includeDeclaration = true;
        String journalCitationXML = journalCitation.toXML(includeDeclaration);

        if (uid.equals(publicId)) {
            message = LOGIN_WARNING;
            messageType = "warning";
            request.setAttribute("message", message);
        }
        else if (!inputMessage.isEmpty()) {
            createMessage = inputMessage;
            messageType = "input-error";
        }
        else {
            try {
                JournalCitationsClient journalCitationsClient = new JournalCitationsClient(uid);
                Integer journalCitationId = journalCitationsClient.create(journalCitationXML);
                String mapbrowseUrl = MapBrowseServlet.getRelativeURL(packageId);
                String mapbrowseHTML = String.format("<a class='searchsubcat' href='%s'>%s</a>", mapbrowseUrl, packageId);
                createMessage = String.format(
                    "A journal citation entry with identifier '<strong>%d</strong>' was created for data package %s", 
                    journalCitationId, mapbrowseHTML);
            } 
            catch (Exception e) {
                handleDataPortalError(logger, e);
            }
        }

        request.setAttribute("createMessage", createMessage);
        request.setAttribute("messageType", messageType);
        RequestDispatcher requestDispatcher = request.getRequestDispatcher(forward);
        requestDispatcher.forward(request, response);
    }
  
    
    /*
     * Validate user input. An empty msg value ("") means that validation succeeded.
     */
    private String validateInput(String packageId, String articleDoi, String articleUrl,
                               String articleTitle, String journalTitle, String relationType) {
        String msg = null;
        StringBuffer msgBuffer = new StringBuffer("");
        
        boolean hasArticleDoi = ((articleDoi != null) && !articleDoi.isEmpty());
        boolean hasArticleUrl = ((articleUrl != null) && !articleUrl.isEmpty());
        
        if (!(hasArticleDoi || hasArticleUrl)) {
            msgBuffer.append("Either an article DOI or an article URL is required.");
        }
       
        msg = msgBuffer.toString();
        return msg;
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
