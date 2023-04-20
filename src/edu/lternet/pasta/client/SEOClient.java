/*
 *
 * $Date: 2012-06-22 12:23:25 -0700 (Fri, 22 June 2012) $
 * $Author: dcosta $
 * $Revision: 2145 $
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

package edu.lternet.pasta.client;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.commons.configuration.Configuration;

import edu.lternet.pasta.portal.ConfigurationListener;


/**
 * 
 * @author Duane Costa
 * 
 * The SEOClient class fetches JSON from the SEO server, making it
 * available to the embed as a script element within the head element 
 * of the Data Portal landing page.
 *
 */
public class SEOClient extends PastaClient {

    /*
     * Class variables
     */

    private static final Logger logger = Logger
        .getLogger(edu.lternet.pasta.client.SEOClient.class);

    // SEO base service URL
    private static String seoUrl;
    
    
    /*
     * Instance variables
     */
    private String tier = null;


    /*
     * Constructors
     */

    /**
     * @param uid   User ID, needed for the parent PastaClient class
     * 
     * @throws PastaAuthenticationException
     * @throws PastaConfigurationException
    */
    public SEOClient(String uid)
            throws PastaAuthenticationException, PastaConfigurationException {
        super(uid);

        Configuration options = ConfigurationListener.getOptions();

        seoUrl = options.getString("seo.url");

        if (this.pastaHost.startsWith("pasta.")) {
            tier = "p";
        }
        else if (this.pastaHost.startsWith("pasta-d.") ||
                 this.pastaHost.startsWith("localhost")) {
            tier = "d";
        }
        else if (this.pastaHost.startsWith("pasta-s.")) {
            tier = "s";
        }
        else {
            String msg = String.format("Unknown PASTA host: %s",
                                       this.pastaHost);
            throw new PastaConfigurationException(msg);
        }
    }


    /**
     * Calls the SEO schema.org web service that returns a dataset JSON-LD string
     * 
     * @return A JSON string to be inserted into the head element of
     *         the landing page HTML.
     */
    public String fetchDatasetJSON(String packageId)
            throws Exception {
        HttpGet httpGet = null;
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String jsonString = null;
        String serviceURL = String.format("%s?pid=%s&env=%s", seoUrl + "/dataset", packageId, this.tier);

        try {
            httpGet = new HttpGet(serviceURL);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                HttpEntity httpEntity = httpResponse.getEntity();
                jsonString = EntityUtils.toString(httpEntity).trim();
            }
            else {
                String msg = String.format("SEO server URL '%s' returned status code %d",
                                           serviceURL, statusCode);
                throw new SEOClientException(msg);
            }
        } catch (Exception e) {
            logger.error(String.format("Error fetching JSON: %s", e.getMessage()));
            throw(e);
        } finally {
            closeHttpClient(httpClient);
        }

        return jsonString;
    }
    
    
    /**
     * Calls the SEO schema.org web service that returns a repository JSON-LD string
     *
     * @return A JSON string to be inserted into the head element of
     *         the landing page HTML.
     */
    public String fetchRepositoryJSON()
            throws Exception {
        HttpGet httpGet = null;
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String jsonString = null;
        String serviceURL = seoUrl + "/repository";

        try {
            httpGet = new HttpGet(serviceURL);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                HttpEntity httpEntity = httpResponse.getEntity();
                jsonString = EntityUtils.toString(httpEntity).trim();
            }
            else {
                String msg = String.format("SEO server URL '%s' returned status code %d",
                                           serviceURL, statusCode);
                throw new SEOClientException(msg);
            }
        } catch (Exception e) {
            logger.error(String.format("Error fetching JSON: %s", e.getMessage()));
            throw(e);
        } finally {
            closeHttpClient(httpClient);
        }

        return jsonString;
    }


    public static void main(String[] args) {
        ConfigurationListener.configure();
        String uid = "public";
        String packageId = args[0];
        String jsonString = "";
        
        try {
            SEOClient seoClient = new SEOClient(uid);
            jsonString = seoClient.fetchDatasetJSON(packageId);
        }
        catch (Exception e) {
           e.printStackTrace();
        }
        
        System.out.println(jsonString);
    }

}
