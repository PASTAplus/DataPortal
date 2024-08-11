/*
 *
 * Copyright 2020 the University of New Mexico.
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

import edu.lternet.pasta.portal.ConfigurationListener;
import org.apache.commons.configuration.Configuration;
import org.apache.http.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


public class CrossrefClient extends PastaClient {
    private static final Logger logger = Logger.getLogger(CrossrefClient.class);
    private static String crossrefUrl;

    /**
     * @param uid User ID, needed for the parent PastaClient class
     */
    public CrossrefClient(String uid) throws PastaConfigurationException, PastaAuthenticationException {
        super(uid);
        Configuration options = ConfigurationListener.getOptions();
        crossrefUrl = options.getString("crossref.url");
    }

    /**
     * Call the Crossref API
     *
     * @return DOI metadata as JSON.
     */
    public JSONObject fetchByDoi(String doi) throws CrossrefClientException, UnsupportedEncodingException
    {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String serviceURL = String.format("%s/%s", crossrefUrl, URLEncoder.encode(doi, "UTF-8"));
        HttpGet httpGet = new HttpGet(serviceURL);
        httpGet.setHeader(HttpHeaders.ACCEPT, "application/json");

        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            String reasonPhrase = statusLine.getReasonPhrase();

            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                throw new Exception("DOI Not Found");
            }

            if (statusCode != HttpStatus.SC_OK) {
                throw new Exception(String.format("%d %s", statusCode, reasonPhrase));
            }

            HttpEntity httpEntity = httpResponse.getEntity();
            String bodyStr = EntityUtils.toString(httpEntity, "UTF-8").trim();
            try {
                return new JSONObject(bodyStr);
            }
            catch (Exception e) {
                throw new Exception("Response is not valid JSON");
            }
        } catch (Exception e) {
            String errorMsg = String.format("Error fetching DOI: %s", e.getMessage());
            logger.error(errorMsg);
            JSONObject json = new JSONObject();
            json.put("error", errorMsg);
            return json;
        } finally {
            closeHttpClient(httpClient);
        }
    }
}
