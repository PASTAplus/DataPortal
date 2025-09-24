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

import edu.lternet.pasta.token.TokenManager;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URISyntaxException;

public class LogoutServlet extends DataPortalServlet
{

    /**
     * Class variables
     */

    private static final Logger logger = Logger.getLogger(edu.lternet.pasta.portal.LogoutServlet.class);
    private static final long serialVersionUID = 1L;
    private PropertiesConfiguration options;

    /**
     * Constructor of the object.
     */
    public LogoutServlet()
    {
        super();
    }

    /**
     * Destruction of the servlet. <br>
     */
    @Override
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
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        // Pass request onto "doPost".
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
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        HttpSession httpSession = request.getSession();
        String idProvider = (String) httpSession.getAttribute("idProvider");
        try {
            if (idProvider != null) {
                request.setAttribute("logoutMessage", getLogoutMessage(idProvider));
            }
            // While we allow multiple sessions to be logged in as the same user, we cannot delete the PASTA token when
            // one of them logs out. See ticket https://github.com/PASTAplus/DataPortal/issues/126
            // String uid = (String) httpSession.getAttribute("uid");
            // TokenManager.deleteToken(uid);

            // Remove EDI and PASTA authentication token cookies
            Cookie ediTokenCookie = new Cookie("edi-token", "");
            ediTokenCookie.setMaxAge(0);
            ediTokenCookie.setPath("/");
            response.addCookie(ediTokenCookie);

            Cookie authTokenCookie = new Cookie("auth-token", "");
            authTokenCookie.setMaxAge(0);
            authTokenCookie.setPath("/");
            response.addCookie(authTokenCookie);

        } catch (Exception e) {
            handleDataPortalError(logger, e);
        } finally {
            httpSession.invalidate();
        }

        RequestDispatcher requestDispatcher = request.getRequestDispatcher("./home.jsp");
        requestDispatcher.forward(request, response);
    }


    public String getLogoutMessage(String idProvider)
    {
        String providerMsg = options.getString("sso.logout." + idProvider);
        if (providerMsg != null) {
            return String.format("%s %s", options.getString("sso.logout.msg"), providerMsg);
        }
        return null;
    }


    /**
     * Initialization of the servlet. <br>
     *
     * @throws ServletException if an error occurs
     */
    @Override
    public void init() throws ServletException
    {
        this.options = ConfigurationListener.getOptions();
    }
}
