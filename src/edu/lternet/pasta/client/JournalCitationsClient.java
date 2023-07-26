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

package edu.lternet.pasta.client;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import edu.lternet.pasta.common.JournalCitation;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


/**
 * @author Duane Costa
 * @since December 20, 2017
 * 
 * Class for interacting with PASTA web services relating to Journal Citations.
 * 
 */
public class JournalCitationsClient extends PastaClient {

  /*
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.client.JournalCitationsClient.class);

  /*
   * Instance variables
   */

  private final String BASE_URL;
  private final String BASE_URL_ONE_CITATION;
  private final String BASE_URL_LIST_OF_CITATIONS;

  /*
   * Constructors
   */

  /**
   * Creates a new JournalCitationsClient object and sets the user's authentication token
   * if it exists; otherwise an error.
   * 
   * @param uid
   *          The user's identifier as a String object.
   * 
   * @throws PastaAuthenticationException
   * @throws PastaConfigurationException
   */
  public JournalCitationsClient(String uid)
      throws PastaAuthenticationException, PastaConfigurationException {

    super(uid);
    String pastaUrl = PastaClient.composePastaUrl(this.pastaProtocol, this.pastaHost, this.pastaPort);
    this.BASE_URL = pastaUrl + "/package";
    this.BASE_URL_ONE_CITATION = this.BASE_URL + "/citation/eml";
    this.BASE_URL_LIST_OF_CITATIONS = this.BASE_URL + "/citations/eml";
  }

  
  /*
   * Methods
   */

  /**
   * Create a new journal citation in PASTA
   * 
   * @param journalCitationXML
   *          The XML subscription as a String object.
   * 
   * @return The journal citation identifier as a String object.
   * 
   * @throws PastaEventException
   */
    public Integer create(String journalCitationXML) throws PastaEventException {
        Integer journalCitationId = null;
        Integer statusCode = null;
        Header[] headers = null;
        HttpEntity responseEntity = null;
        String statusMessage = null;
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponse response = null;
        HttpPost httpPost = new HttpPost(BASE_URL_ONE_CITATION);

        // Set header content
        if (this.token != null) {
            httpPost.setHeader("Cookie", "auth-token=" + this.token);
        }
        
        httpPost.setHeader("Content-Type", "application/xml");

        // Set subscription into the request entity
        StringEntity requestEntity = null;

        requestEntity = new StringEntity(journalCitationXML, "utf-8");

        httpPost.setEntity(requestEntity);

        try {
            response = httpClient.execute(httpPost);
            statusCode = (Integer) response.getStatusLine().getStatusCode();
            headers = response.getAllHeaders();
            responseEntity = response.getEntity();

            if (responseEntity != null) {
                statusMessage = EntityUtils.toString(responseEntity);
            }
        } 
        catch (ClientProtocolException e) {
            logger.error(e);
            e.printStackTrace();
        } 
        catch (IOException e) {
            logger.error(e);
            e.printStackTrace();
        } 
        finally {
            closeHttpClient(httpClient);
        }

        if (statusCode == HttpStatus.SC_CREATED) {
            String headerName = null;
            String headerValue = null;

            // Loop through all headers looking for the "Location" header.
            for (int i = 0; i < headers.length; i++) {
                headerName = headers[i].getName();

                if (headerName.equals("Location")) {
                    headerValue = headers[i].getValue();
                    String[] path = headerValue.split("/");
                    /*
                     * the journal citation identifier is
                     * in the last field of the path array
                     */
                    String journalCitationStr = path[path.length - 1];
                    try {
                        journalCitationId = Integer.parseInt(journalCitationStr);
                    }
                    catch (NumberFormatException e) {
                        String gripe = String.format(
                                "PASTA responded with response code '%d' and message '%s'.\n",
                                statusCode, statusMessage);
                        throw new PastaEventException(gripe);
                    }
                    break;         
                }
            }
        } 
        else { // Something went wrong; return message from the response
                 // entity
            String gripe = String.format(
                    "PASTA responded with response code '%d' and message '%s'.\n",
                    statusCode, statusMessage);
            throw new PastaEventException(gripe);
        }

        return journalCitationId;
    }

  public Integer update(String journalCitationXML) throws PastaEventException
  {
    JournalCitation journalCitation = new JournalCitation(journalCitationXML);
    int journalCitationId = journalCitation.getJournalCitationId();
    HttpPut httpPut = new HttpPut(BASE_URL_ONE_CITATION + "/" + journalCitationId);

    if (this.token != null) {
      httpPut.setHeader("Cookie", "auth-token=" + this.token);
    }
    httpPut.setHeader("Content-Type", "application/xml");

    StringEntity requestEntity = new StringEntity(journalCitationXML, "utf-8");

    httpPut.setEntity(requestEntity);

    CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    Integer statusCode = null;
    Header[] headers = null;
    String statusMessage = null;

    try {
      HttpResponse response = httpClient.execute(httpPut);
      statusCode = response.getStatusLine().getStatusCode();
      headers = response.getAllHeaders();
      HttpEntity responseEntity = response.getEntity();

      if (responseEntity != null) {
        statusMessage = EntityUtils.toString(responseEntity);
      }
    } catch (IOException e) {
      logger.error(e);
      e.printStackTrace();
    } finally {
      closeHttpClient(httpClient);
    }

    if (statusCode == HttpStatus.SC_CREATED) {
      String headerName = null;
      String headerValue = null;

      // Loop through all headers looking for the "Location" header.
      for (Header header : headers) {
        headerName = header.getName();

        if (headerName.equals("Location")) {
          headerValue = header.getValue();
          String[] path = headerValue.split("/");
          /*
           * the journal citation identifier is
           * in the last field of the path array
           */
          String journalCitationStr = path[path.length - 1];
          try {
            journalCitationId = Integer.parseInt(journalCitationStr);
          } catch (NumberFormatException e) {
            String gripe = String.format(
                "PASTA responded with response code '%d' and message '%s'.\n",
                statusCode, statusMessage);
            throw new PastaEventException(gripe);
          }
          break;
        }
      }
    }
    else { // Something went wrong; return message from the response
      // entity
      String gripe =
          String.format("PASTA responded with response code '%d' and message '%s'.\n",
              statusCode, statusMessage);
      throw new PastaEventException(gripe);
    }

    return journalCitationId;
  }

    
  /**
   * Executes the 'listDataPackageCitations' web service method.
   * 
   * @param scope
   *          the scope value, e.g. "knb-lter-lno"
   * @param identifier
   *          the identifier value, e.g. 10
   * @param revision
   *          the revision value, e.g. "1" or "newest"
   * @return an XML string containing a list of <journalCitation> elements encapsulated within a
   *         <journalCitations> element.
   * @see <a target="top"
   *      href="http://package.lternet.edu/package/docs/api">Data Package
   *      Manager web service API</a>
   */
  public String listDataPackageCitations(String scope, Integer identifier,
      String revision) throws Exception {
      CloseableHttpClient httpClient = HttpClientBuilder.create().build();
      String urlTail = makeUrlTail(scope, identifier.toString(), revision, null);
      String url = BASE_URL_LIST_OF_CITATIONS + "/" + urlTail + "?all";
      HttpGet httpGet = new HttpGet(url);
      String entityString = null;

      // Set header content
      if (this.token != null) {
          httpGet.setHeader("Cookie", "auth-token=" + this.token);
      }

      try {
          HttpResponse httpResponse = httpClient.execute(httpGet);
          int statusCode = httpResponse.getStatusLine().getStatusCode();
          HttpEntity httpEntity = httpResponse.getEntity();
          entityString = EntityUtils.toString(httpEntity, "utf-8");
          if (statusCode != HttpStatus.SC_OK) {
              handleStatusCode(statusCode, entityString);
          }
      } finally {
          closeHttpClient(httpClient);
      }

      return entityString;
  }

  
  /**
   * Executes the 'listPrincipalOwnerCitations' web service method.
   * 
   * @param principalOwner  the distinguished name of the user who is listed in the
   *          'principal_owner' field of the journal citations table
   * @return an XML string containing a list of <journalCitation> elements encapsulated within a
   *         <journalCitations> element.
   * @see <a target="top"
   *      href="http://package.lternet.edu/package/docs/api">Data Package
   *      Manager web service API</a>
   */
  public String listPrincipalOwnerCitations(String principalOwner) throws Exception {
      CloseableHttpClient httpClient = HttpClientBuilder.create().build();
      String url = BASE_URL_LIST_OF_CITATIONS + "/" + principalOwner;
      HttpGet httpGet = new HttpGet(url);
      String entityString = null;

      // Set header content
      if (this.token != null) {
          httpGet.setHeader("Cookie", "auth-token=" + this.token);
      }

      try {
          HttpResponse httpResponse = httpClient.execute(httpGet);
          int statusCode = httpResponse.getStatusLine().getStatusCode();
          HttpEntity httpEntity = httpResponse.getEntity();
          entityString = EntityUtils.toString(httpEntity, "utf-8");
          if (statusCode != HttpStatus.SC_OK) {
              handleStatusCode(statusCode, entityString);
          }
      } finally {
          closeHttpClient(httpClient);
      }

      return entityString;
  }

  
  /**
   * Returns the journal citation as a String object based on the journal
   * citation identifier.
   * 
   * @param journalCitationId
   *          The journal citation identifier as a String object.
   * 
   * @return The journal citation in its native XML format as String object.
   * 
   * @throws PastaEventException
   */
    public String getCitationWithId(String journalCitationId) throws PastaEventException {
        String entity = null;
        Integer statusCode = null;
        HttpEntity responseEntity = null;
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponse response = null;
        HttpGet httpGet = new HttpGet(BASE_URL_ONE_CITATION + "/" + journalCitationId);

        // Set header content
        if (this.token != null) {
            httpGet.setHeader("Cookie", "auth-token=" + this.token);
        }

        try {
            response = httpClient.execute(httpGet);
            statusCode = (Integer) response.getStatusLine().getStatusCode();
            responseEntity = response.getEntity();

            if (responseEntity != null) {
                entity = EntityUtils.toString(responseEntity);
            }

        } catch (ClientProtocolException e) {
            logger.error(e);
            e.printStackTrace();
        } catch (IOException e) {
            logger.error(e);
            e.printStackTrace();
        } finally {
            closeHttpClient(httpClient);
        }

        if (statusCode != HttpStatus.SC_OK) {
            // Something went wrong; return message from the response entity
            String gripe = "PASTA responded with response code '" + statusCode.toString() + "' and message '"
                    + entity + "'\n";
            throw new PastaEventException(gripe);
        }

        return entity;
    }

    
  /**
   * Deletes the event subscription identified by its subscription identifier.
   * 
   * @param id
   *          The subscription identifier as a String object.
   * 
   * @throws PastaEventException
   */
  public void deleteByJournalCitationId(String id) throws PastaEventException {

    String entity = null;
    Integer statusCode = null;
    HttpEntity responseEntity = null;

    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponse response = null;
    HttpDelete httpDelete = new HttpDelete(BASE_URL_ONE_CITATION + "/" + id);

    // Set header content
    if (this.token != null) {
      httpDelete.setHeader("Cookie", "auth-token=" + this.token);
    }

    try {

      response = httpClient.execute(httpDelete);
      statusCode = (Integer) response.getStatusLine().getStatusCode();
      responseEntity = response.getEntity();

      if (responseEntity != null) {
        entity = EntityUtils.toString(responseEntity);
      }

    } catch (ClientProtocolException e) {
      logger.error(e);
      e.printStackTrace();
    } catch (IOException e) {
      logger.error(e);
      e.printStackTrace();
    } finally {
        closeHttpClient(httpClient);
    }

    if (statusCode != HttpStatus.SC_OK) {

      // Something went wrong; return message from the response entity
      String gripe = "The EventManager responded with response code '"
          + statusCode.toString() + "' and message '" + entity + "'\n";
      throw new PastaEventException(gripe);

    }

  }

  
    public String citationsTableHTML() throws Exception {
        String html = "";

        if (this.uid != null && !this.uid.equals("public")) {
            StringBuilder sb = new StringBuilder("");
            String xmlString = listPrincipalOwnerCitations(this.uid);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                InputStream inputStream = IOUtils.toInputStream(xmlString, "UTF-8");
                Document document = documentBuilder.parse(inputStream);
                Element documentElement = document.getDocumentElement();
                NodeList citationsNodeList = documentElement.getElementsByTagName("journalCitation");
                int nJournalCitations = citationsNodeList.getLength();

                for (int i = 0; i < nJournalCitations; i++) {
                    Node journalCitationNode = citationsNodeList.item(i);
                    NodeList journalCitationChildren = journalCitationNode.getChildNodes();
                    String journalCitationId = "";
                    String packageId = "";
                    String articleDoi = "";
                    String articleUrl = "";
                    String articleTitle = "";
                    String journalTitle = "";
                    String relationType = "";
                    String pubDate = "";

                    for (int j = 0; j < journalCitationChildren.getLength(); j++) {
                        Node childNode = journalCitationChildren.item(j);
                        if (childNode instanceof Element) {
                            Element subscriptionElement = (Element) childNode;

                            if (subscriptionElement.getTagName().equals("journalCitationId")) {
                                Text text = (Text) subscriptionElement.getFirstChild();
                                if (text != null) {
                                    journalCitationId = text.getData().trim();
                                }
                            } 
                            else if (subscriptionElement.getTagName().equals("packageId")) {
                                Text text = (Text) subscriptionElement.getFirstChild();
                                if (text != null) {
                                    packageId = text.getData().trim();
                                }
                            } 
                            else if (subscriptionElement.getTagName().equals("articleDoi")) {
                                Text text = (Text) subscriptionElement.getFirstChild();
                                if (text != null) {
                                    articleDoi = text.getData().trim();
                                }
                            } 
                            else if (subscriptionElement.getTagName().equals("articleUrl")) {
                                Text text = (Text) subscriptionElement.getFirstChild();
                                if (text != null) {
                                    articleUrl = text.getData().trim();
                                }
                           }
                            else if (subscriptionElement.getTagName().equals("articleTitle")) {
                                Text text = (Text) subscriptionElement.getFirstChild();
                                if (text != null) {
                                    articleTitle = text.getData().trim();
                                }
                           }
                            else if (subscriptionElement.getTagName().equals("journalTitle")) {
                                Text text = (Text) subscriptionElement.getFirstChild();
                                if (text != null) {
                                    journalTitle = text.getData().trim();
                                }
                           }
                            else if (subscriptionElement.getTagName().equals("relationType")) {
                                Text text = (Text) subscriptionElement.getFirstChild();
                                if (text != null) {
                                    relationType = text.getData().trim();
                                }
                           }
                            else if (subscriptionElement.getTagName().equals("pubDate")) {
                                Text text = (Text) subscriptionElement.getFirstChild();
                                if (text != null) {
                                    pubDate = text.getData().trim();
                                }
                           }
                        }
                    }

                    sb.append("<tr>\n");

                    sb.append("<td class='nis' align='center'>");
                    sb.append(journalCitationId);
                    sb.append("</td>\n");

                    sb.append("<td class='nis' align='center'>");
                    sb.append(packageId);
                    sb.append("</td>\n");

                    sb.append("<td class='nis'>");
                    sb.append(relationType);
                    sb.append("</td>\n");

                    sb.append("<td class='nis'>");
                    sb.append(articleDoi);
                    sb.append("</td>\n");

                    sb.append("<td class='nis'>");
                    sb.append(articleUrl);
                    sb.append("</td>\n");

                    sb.append("<td class='nis'>");
                    sb.append(articleTitle);
                    sb.append("</td>\n");

                    sb.append("<td class='nis'>");
                    sb.append(journalTitle);
                    sb.append("</td>\n");

                    sb.append("<td class='nis'>");
                    sb.append(pubDate);
                    sb.append("</td>\n");

                    sb.append("</tr>\n");
                }

                html = sb.toString();
            } catch (Exception e) {
                logger.error("Exception:\n" + e.getMessage());
                e.printStackTrace();
                throw new PastaEventException(e.getMessage());
            }
        }

        return html;
    }

    
    public String citationsOptionsHTML() throws Exception {
        String html = "";

        if (this.uid != null && !this.uid.equals("public")) {
            StringBuilder sb = new StringBuilder("");
            String xmlString = listPrincipalOwnerCitations(this.uid);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                InputStream inputStream = IOUtils.toInputStream(xmlString, "UTF-8");
                Document document = documentBuilder.parse(inputStream);
                Element documentElement = document.getDocumentElement();
                NodeList citationsList = documentElement.getElementsByTagName("journalCitation");
                int nCitations = citationsList.getLength();

                for (int i = 0; i < nCitations; i++) {
                    Node journalCitationNode = citationsList.item(i);
                    NodeList journalCitationChildren = journalCitationNode.getChildNodes();
                    String journalCitationId = "";
                    for (int j = 0; j < journalCitationChildren.getLength(); j++) {
                        Node childNode = journalCitationChildren.item(j);
                        if (childNode instanceof Element) {
                            Element journalCitationElement = (Element) childNode;
                            if (journalCitationElement.getTagName().equals("journalCitationId")) {
                                Text text = (Text) journalCitationElement.getFirstChild();
                                if (text != null) {
                                    journalCitationId = text.getData().trim();
                                }
                            }
                        }
                    }

                    sb.append(String.format("<option value='%s'>%s</option>\n", journalCitationId, journalCitationId));
                }

                html = sb.toString();
            } catch (Exception e) {
                logger.error("Exception:\n" + e.getMessage());
                e.printStackTrace();
                throw new PastaEventException(e.getMessage());
            }
        }

        return html;
    }

}
