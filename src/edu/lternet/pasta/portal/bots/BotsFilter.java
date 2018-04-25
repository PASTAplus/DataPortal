/*
 *
 * $Author: dcosta $
 *
 * Copyright 2010-2018 the University of New Mexico.
 *
 * This work was supported by National Science Foundation Cooperative Agreements
 * #DEB-0832652 and #DEB-0936498.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.lternet.pasta.portal.bots;

import org.apache.log4j.Logger;

import edu.lternet.pasta.common.security.access.UnauthorizedException;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import java.io.*;
import java.util.*;

/**
 * @author Duane Costa
 * 
 * A filter class to ensure that all requests to the Data Portal are scanned
 * for bots and, when detected, reported to PASTA.
 *
 */
@WebFilter(filterName = "BotsFilter", urlPatterns = { "/*" })
public final class BotsFilter implements Filter
{

	/*
	 * Class variables
	 */
    private static Logger logger = Logger.getLogger(BotsFilter.class);

    
    /*
     * Instance variables
     */
    private FilterConfig filterConfig;


    /*
     * Instance methods
     */

    /**
     * Overridden init method that sets the filterConfig.
     */
    @Override
    public void init(FilterConfig config) throws ServletException {
        filterConfig = config;
        String appPath = filterConfig.getServletContext().getRealPath("/");
        
        try {
            BotMatcher.initializeRobotPatterns(appPath + "/WEB-INF/conf/robotPatterns.txt");
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new ServletException(e.getMessage());
       }
    }

    
    /**
     * Overridden destroy method that free's the filterConfig.
     */
    @Override
    public void destroy() {
        filterConfig = null;
    }

    
    /**
     * Overridden doFilter method.
     * @param request ServletRequest representing the incoming user http(s)
     *                request.
     * @param request ServletResponse representing the associated response
     *                                that will eventually be passed on to the
     *                                next servlet.
     */
    @Override
    public void doFilter(ServletRequest request, 
                         ServletResponse response,
                         FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;                   
        Cookie internalCookie = null;
        PastaRequestWrapper pastaRequestWrapper = new PastaRequestWrapper(httpServletRequest, internalCookie);

        String robot = BotMatcher.findRobot(httpServletRequest);
        
        if (robot == null) {
            /*
             * This value is interpreted by PASTA's Gatekeeper filter to mean
             * that the Data Portal has already checked for robots and didn't
             * find any. When the Gatekeeper sees a "No robot" value, it knows
             * that it can skip its own checking for robots.
             */
            robot = "No robot";
        }
        
        pastaRequestWrapper.putHeader("Robot", robot);      

        filterChain.doFilter(pastaRequestWrapper, response);
    }

    
  public static class PastaRequestWrapper extends HttpServletRequestWrapper {

        /*
         * Instance variables
         */
        private Cookie cookie;
        private final Map<String, String> customHeaders;

        
        /*
         * Constructors
         */

        public PastaRequestWrapper(HttpServletRequest request, Cookie cookie) {
            super(request);
            this.cookie = cookie;
            this.customHeaders = new HashMap<String, String>();
        }

        
        /*
         * Instance methods
         */
        
        public String getHeader(String name) {

            // Check the custom headers first, e.g. if name is "robot"
            String headerValue = customHeaders.get(name);
        
            if (headerValue != null){
                return headerValue;
            }

            if (name.equals(HttpHeaders.AUTHORIZATION)) 
                return null;

            String header = super.getHeader(name);

            if (name.equals(HttpHeaders.COOKIE) && 
                header != null &&
                header.isEmpty() && 
                (cookie != null)
               ) {
                header = cookie.getName();
            }  

            return header;
        }

        
        public Enumeration<String> getHeaders(String name) {
            Enumeration<String> enumStr = super.getHeaders(name);

            if (name.equalsIgnoreCase("Robot")) {
                List<String> ls = new ArrayList<String>();
                String value = getHeader(name);
                ls.add(value);
                enumStr = Collections.enumeration(ls);
            }

            if (name.equals(HttpHeaders.AUTHORIZATION)) {
                List<String> ls = new ArrayList<String>();
                enumStr = Collections.enumeration(ls);
            }

            if (!name.equals(HttpHeaders.COOKIE) || (cookie == null)) {
                return enumStr;
            }
            else {
                ArrayList<String> list = Collections.list(enumStr);
                list.add(cookie.getName() + "=" + cookie.getValue());
                return Collections.enumeration(list);
            }
        }

        
        public Enumeration<String> getHeaderNames() {
            // Create a set of the custom header names
            Set<String> set = new HashSet<String>(customHeaders.keySet());
        
            // Now add the headers from the wrapped request object
            @SuppressWarnings("unchecked")
            Enumeration<String> e = ((HttpServletRequest) getRequest()).getHeaderNames();
            while (e.hasMoreElements()) {
                // add the names of the request headers into the list
                String n = e.nextElement();
                set.add(n);
            }

            if (!set.contains(HttpHeaders.COOKIE) && (cookie != null)) {
                set.add(HttpHeaders.COOKIE);
            }

            return Collections.enumeration(set);
        }

        
        public Cookie[] getCookies() {       	
            Cookie[] cookies = super.getCookies();

            if (cookie == null) {
                return cookies;
            } 
            else {
                ArrayList<Cookie> cookieList = (cookies == null) ? new ArrayList<Cookie>()
                                               : new ArrayList<Cookie>(Arrays.asList(cookies));
                cookieList.add(cookie);
                cookies = new Cookie[cookieList.size()];
                return cookieList.toArray(cookies);
            }
        }

        
        /**
         * Adds a custom header name and corresponding value to the wrapper
         * class
         * 
         * @param name   the custom header name
         * @param value  the custom header value
         */
        public void putHeader(String name, String value){
            this.customHeaders.put(name, value);
        }

    } // end PastaRequestWrapper inner class

}
