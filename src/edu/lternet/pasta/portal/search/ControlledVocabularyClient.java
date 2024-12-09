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

package edu.lternet.pasta.portal.search;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.TreeSet;

import org.apache.commons.configuration.Configuration;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import edu.lternet.pasta.portal.ConfigurationListener;

public class ControlledVocabularyClient {

	/*
	 * Class variables
	 */

	private static final Logger logger = Logger
	    .getLogger(edu.lternet.pasta.portal.search.ControlledVocabularyClient.class);

    private final static Configuration options = ConfigurationListener.getOptions();

    private final static String host = options.getString("vocab.host");
    private final static Boolean ignore = options.getBoolean("vocab.ignore");
	
  // Controlled vocabulary settings
  private final static String BASE_SERVICE_URL = String.format("http://%s/webservice/keywordlist.php/", host);
  private final static String FETCH_DOWN_SERVICE_URL = String.format("http://%s/vocab/vocab/services.php?task=fetchDown&arg=", host);
  private final static String FETCH_TOP_TERMS_SERVICE_URL = String.format("http://%s/vocab/vocab/services.php?task=fetchTopTerms", host);
  private final static String PREFERRED_TERMS_SERVICE_URL = String.format("http://%s/webservice/preferredterms.php", host);
  private static String[] PREFERRED_TERMS_ARRAY = null;

 
	/*
	 * Closes the HTTP client
	 */
	private static void closeHttpClient(CloseableHttpClient httpClient) {
		try {
			httpClient.close();
		}
		catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}


  /**
   * Case One: Simple Search
   * 
   * Simple search should set hasExact to true, all other boolean
   * arguments to false.
   * 
   * 
   * Case Two: Advanced Search
   * 
   * Extend the set of search values by accessing the controlled vocabulary
   * web service using the following matrix.
   * 
   * The http://vocab.lternet.edu/webservice/keywordlist.php 
   * web service options map as follows (as per John Porter):
   *
   *          Checkbox   X=checked   -=unchecked
   *          
   *  MoreSpecific    Related    Related+specific     Relation option
   *            -         -        -                    exact
   *            X         -        -                    narrow
   *            -         X        -                    related
   *            -         -        X                    all
   *            X         X        -                    narrowrelated
   *            X         -        X                    all
   *            -         X        X                    all
   *            X         X        X                    all    
   * 
   * @param  searchValue   the original search value
   * @return searchValues  a set of search values, including the original
   */
  public static TreeSet<String> webServiceSearchValues(String searchValue,
                                                 boolean hasExact,
                                                 boolean hasNarrow,
                                                 boolean hasRelated,
                                                 boolean hasNarrowRelated,
                                                 boolean hasAll
                                                ) {
    HttpGet httpGet = null;
    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    String format = "list";
    TreeSet<String> searchValues = new TreeSet<String>();

    if (ignore) {
        return searchValues;
    }

    // Map both narrow and related selection to all
    if (hasNarrowRelated) {
        hasAll = true;
      }
    
    String exactServiceURL = BASE_SERVICE_URL + "exact" + "/" + format;
    String narrowServiceURL = BASE_SERVICE_URL + "narrow" + "/" + format;
    String relatedServiceURL = BASE_SERVICE_URL + "related" + "/" + format;
    String narrowRelatedServiceURL = BASE_SERVICE_URL + "narrowrelated" + "/" + format;
    String allServiceURL = BASE_SERVICE_URL + "all" + "/" + format;
    
    if (searchValue != null && !searchValue.equals("")) {
      try {
        String encodedValue = URLEncoder.encode(searchValue, "UTF-8");
        exactServiceURL += "/" + encodedValue;
        narrowServiceURL += "/" + encodedValue;
        relatedServiceURL += "/" + encodedValue;
        narrowRelatedServiceURL += "/" + encodedValue;
        allServiceURL += "/" + encodedValue;
      }
      catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
      
      try { 
        
        if (hasAll) {
          httpGet = new HttpGet(allServiceURL);
          HttpResponse httpResponse = httpClient.execute(httpGet);
          int statusCode = httpResponse.getStatusLine().getStatusCode();
          String reasonPhrase = httpResponse.getStatusLine().getReasonPhrase();
          if (statusCode == HttpStatus.SC_OK) {
            HttpEntity httpEntity = httpResponse.getEntity();
            String entityString = EntityUtils.toString(httpEntity);
            if (entityString != null) {
              String[] alls = entityString.split("\n");
              for (String all : alls) {
                if (all != null && !all.equals("")) {
                  searchValues.add(all);
                }
              }
            }
          }
          else {
              String msg = String.format("Non-OK response from controlled vocabulary service with HTTP status" +
                              "code: %d, reason phrase: %s", statusCode, reasonPhrase);
              logger.error(msg);
          }
        }
        
        if (hasExact) {
          httpGet = new HttpGet(exactServiceURL);
          HttpResponse httpResponse = httpClient.execute(httpGet);
          int statusCode = httpResponse.getStatusLine().getStatusCode();
          String reasonPhrase = httpResponse.getStatusLine().getReasonPhrase();
          if (statusCode == HttpStatus.SC_OK) {
            HttpEntity httpEntity = httpResponse.getEntity();
            String entityString = EntityUtils.toString(httpEntity);
            if (entityString != null) {
              String[] synonyms = entityString.split("\n");
              for (String synonym : synonyms) {
                if (synonym != null && !synonym.equals("")) {
                  searchValues.add(synonym);
                }
              }
            }
          }
          else {
              String msg = String.format("Non-OK response from controlled vocabulary service with HTTP status" +
                      "code: %d, reason phrase: %s", statusCode, reasonPhrase);
              logger.error(msg);
          }
        }
        
        if (hasNarrow) {
          httpGet = new HttpGet(narrowServiceURL);
          HttpResponse httpResponse = httpClient.execute(httpGet);
          int statusCode = httpResponse.getStatusLine().getStatusCode();
          String reasonPhrase = httpResponse.getStatusLine().getReasonPhrase();
          if (statusCode == HttpStatus.SC_OK) {
            HttpEntity httpEntity = httpResponse.getEntity();
            String entityString = EntityUtils.toString(httpEntity);
            if (entityString != null) {
              String[] narrows = entityString.split("\n");
              for (String narrow : narrows) {
                if (narrow != null && !narrow.equals("")) {
                  searchValues.add(narrow);
                }
              }
            }
          }
          else {
              String msg = String.format("Non-OK response from controlled vocabulary service with HTTP status" +
                      "code: %d, reason phrase: %s", statusCode, reasonPhrase);
              logger.error(msg);
          }
        }
        
        if (hasRelated) {
          httpGet = new HttpGet(relatedServiceURL);
          HttpResponse httpResponse = httpClient.execute(httpGet);
          int statusCode = httpResponse.getStatusLine().getStatusCode();
          String reasonPhrase = httpResponse.getStatusLine().getReasonPhrase();
          if (statusCode == HttpStatus.SC_OK) {
            HttpEntity httpEntity = httpResponse.getEntity();
            String entityString = EntityUtils.toString(httpEntity);
            if (entityString != null) {
              String[] relateds = entityString.split("\n");
              for (String related : relateds) {
                if (related != null && !related.equals("")) {
                  searchValues.add(related);
                }
              }
            }
          }
          else {
              String msg = String.format("Non-OK response from controlled vocabulary service with HTTP status" +
                      "code: %d, reason phrase: %s", statusCode, reasonPhrase);
              logger.error(msg);
          }
        }
          
        if (hasRelated) {
          httpGet = new HttpGet(narrowRelatedServiceURL);
          HttpResponse httpResponse = httpClient.execute(httpGet);
          int statusCode = httpResponse.getStatusLine().getStatusCode();
          String reasonPhrase = httpResponse.getStatusLine().getReasonPhrase();
          if (statusCode == HttpStatus.SC_OK) {
            HttpEntity httpEntity = httpResponse.getEntity();
            String entityString = EntityUtils.toString(httpEntity);
            if (entityString != null) {
              String[] narrowRelateds = entityString.split("\n");
              for (String narrowRelated : narrowRelateds) {
                if (narrowRelated != null && !narrowRelated.equals("")) {
                  searchValues.add(narrowRelated);
                }
              }
            }
          }
          else {
              String msg = String.format("Non-OK response from controlled vocabulary service with HTTP status" +
                      "code: %d, reason phrase: %s", statusCode, reasonPhrase);
              logger.error(msg);
          }
        }
   
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      finally {
  		closeHttpClient(httpClient);
   	  }
    }
    
    return searchValues;
  }


  /**
   * Calls the Controlled Vocabulary web service that returns a newline-separated
   * list of preferred terms and returns them in a string array.
   * 
   * @return  A string array holding the LTER Controlled Vocabulary list of
   *          preferred terms as returned by the web service.
   */
  public static String[] webServicePreferredTerms() throws Exception {
    HttpGet httpGet = null;
    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    String[] preferredTerms = null;

    if (ignore) {
        return preferredTerms;
    }
    
    if (PREFERRED_TERMS_ARRAY != null) {
      preferredTerms = PREFERRED_TERMS_ARRAY;
    }
    else {
      try {
        httpGet = new HttpGet(PREFERRED_TERMS_SERVICE_URL);
        HttpResponse httpResponse = httpClient.execute(httpGet);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_OK) {
          HttpEntity httpEntity = httpResponse.getEntity();
          String entityString = EntityUtils.toString(httpEntity);
          if (entityString != null) {
            preferredTerms = entityString.split("\n");
            PREFERRED_TERMS_ARRAY = preferredTerms;
          }
        }
      }
      finally {
  		closeHttpClient(httpClient);
   	  }
    }

    return preferredTerms;
  }


	/**
	 * Calls the Controlled Vocabulary web service that returns an XML document
	 * of terms below a specified term. The XML looks like:
          <vocabularyservices>
            <result>
              <term>
                <term_id>238</term_id>
                <string>habitats</string>
                <lang></lang>
                <relation_type_id>3</relation_type_id>
                <relation_type>NT</relation_type>
                <relation_code></relation_code>
                <relation_label></relation_label>
                <hasMoreDown>0</hasMoreDown>
              </term>            
            .
            . 
     * 
	 * @return An XML document string.
	 */
	public static String webServiceFetchDown(String termId) {
		HttpGet httpGet = null;
	    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String xmlString = null;

        if (ignore) {
            return xmlString;
        }

		try {
			String serviceURL = FETCH_DOWN_SERVICE_URL + termId;
			httpGet = new HttpGet(serviceURL);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				HttpEntity httpEntity = httpResponse.getEntity();
				xmlString = EntityUtils.toString(httpEntity).trim();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	    finally {
			closeHttpClient(httpClient);
	   	}

		return xmlString;
	}


	/**
	 * Calls the Controlled Vocabulary web service that returns an XML document
	 * of top terms. The XML looks like:
          <vocabularyservices>
            <result>
              <term>
                <term_id>799</term_id>
                <code></code>
                <lang>en</lang>
                <string>organizational units</string>
                </term>
              <term>
                <term_id>651</term_id>
                <code></code>
                <lang>en</lang>
                <string>disciplines</string>
              </term>
              .
              . 
     * 
	 * @return An XML document string of top terms.
	 */
	public static String webServiceFetchTopTerms() throws Exception {
		HttpGet httpGet = null;
	    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String xmlString = null;

        if (ignore) {
            return xmlString;
        }

		try {
			httpGet = new HttpGet(FETCH_TOP_TERMS_SERVICE_URL);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				HttpEntity httpEntity = httpResponse.getEntity();
				xmlString = EntityUtils.toString(httpEntity).trim();
			}
		}
		finally {
			closeHttpClient(httpClient);
		}

		return xmlString;
	}

}
