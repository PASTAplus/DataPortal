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
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.ArrayList;


public class RidareClient extends PastaClient {
  /*
   * Class variables
   */

  private static final Logger logger = Logger.getLogger(RidareClient.class);

  private static String ridareUrl;


  /*
   * Instance variables
   */
  private final String tier;


  /*
   * Constructors
   */

  /**
   * @param uid User ID, needed for the parent PastaClient class
   * @throws PastaAuthenticationException
   * @throws PastaConfigurationException
   */
  public RidareClient(String uid)
      throws PastaAuthenticationException, PastaConfigurationException
  {
    super(uid);

        Configuration options = ConfigurationListener.getOptions();

        ridareUrl = options.getString("ridare.url");

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
      String msg = String.format("Unknown PASTA host: %s", this.pastaHost);
      throw new PastaConfigurationException(msg);
    }
  }


  /**
   * Calls the Ridare web service that returns an HTML formatted TextType element.
   *
   * @return A String with an HTML fragment.
   */
  public String fetchTextType(String packageId, String textTypeXpath) throws Exception
  {
    return fetch(packageId, textTypeXpath, false);
  }

  /**
   * Calls the Ridare web service that returns an XML fragment rooted in any element.
   *
   * @return A XML DOM Document object.
   */
  public Document fetchXml(String packageId, String xpath) throws Exception
  {
    String xml = fetch(packageId, xpath, true);
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    try (StringReader stringReader = new StringReader(xml)) {
        InputSource inputSource = new InputSource(stringReader);
        return builder.parse(inputSource);
    }
  }

  /**
   * Calls the Ridare web service.
   *
   * @param packageId The package identifier.
   *                  Example: "knb-lter-lno.1.1"
   * @param xpath     The XPath expression.
   *                  Example: "//dataset"
   * @param isXml     A boolean, true if the result should be XML, false if it should be HTML.
   * @return A String with an HTML or XML fragment.
   */
  public String fetch(String packageId, String xpath, Boolean isXml) throws Exception
  {
    HttpGet httpGet;
    CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    String serviceURL = String.format(
        "%s/%s%s/%s?env=%s",
        ridareUrl,
        isXml ? "raw/" : "",
        packageId,
        URLEncoder.encode(xpath, "UTF-8"),
        this.tier
    );

    String htmlStr;
    try {
      httpGet = new HttpGet(serviceURL);
      httpGet.setHeader(HttpHeaders.ACCEPT, "text/plain");
      HttpResponse httpResponse = httpClient.execute(httpGet);
      int statusCode = httpResponse.getStatusLine().getStatusCode();
      if (statusCode == HttpStatus.SC_OK) {
        HttpEntity httpEntity = httpResponse.getEntity();
        htmlStr = EntityUtils.toString(httpEntity, "UTF-8").trim();
      } else {
        String msg =
            String.format("Ridare server URL '%s' returned status code %d", serviceURL,
                statusCode);
        throw new SEOClientException(msg);
      }
    } catch (Exception e) {
      logger.error(String.format("Error fetching citation: %s", e.getMessage()));
      throw (e);
    } finally {
      closeHttpClient(httpClient);
    }

    return htmlStr;
  }

  /**
   * Fetch the first available xpath
   */
  public String fetchFirstAvailable(String packageId, ArrayList<String> textTypeXpathList)
  {
    for (String textTypeXpath : textTypeXpathList) {
      try {
        return fetchTextType(packageId, textTypeXpath);
      } catch (Exception e) {
        logger.error(
            String.format("Error fetching abstract from Ridare server for %s %s: %s",
                packageId, textTypeXpath, e.getMessage()));
      }
    }
    return null;
  }

  public static void main(String[] args)
  {
    ConfigurationListener.configure();
    String uid = "public";
    String packageId = args[0];
    String textTypeXpath = args[1];
    String htmlStr = "";

    try {
      RidareClient ridareClient = new RidareClient(uid);
      htmlStr = ridareClient.fetchTextType(packageId, textTypeXpath);
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println(htmlStr);
  }

}
