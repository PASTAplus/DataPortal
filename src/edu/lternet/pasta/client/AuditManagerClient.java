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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.commons.configuration.PropertiesConfiguration;

import edu.lternet.pasta.common.*;
import edu.lternet.pasta.portal.ConfigurationListener;

/**
 * @author costa
 * @since June 26, 2014
 *
 *        The AuditManagerClient provides an interface to PASTA's Audit Manager
 *        service. Specifically, this class supports access to the Audit Manager
 *        reports.
 *
 */
public class AuditManagerClient extends PastaClient {

  /*
   * Class variables
   */

  private static final Logger logger = Logger.getLogger(edu.lternet.pasta.client.AuditManagerClient.class);

  /*
   * The cache of RecentUpload objects.
   */
  private static List<RecentUpload> recentInserts = null;
  private static List<RecentUpload> recentUpdates = null;
  private static long recentInsertsLastRefreshTime = 0L;
  private static long recentUpdatesLastRefreshTime = 0L;


  /*
   * Instance variables
   */

  private final String BASE_URL;


  /*
   * Constructors
   */

  /**
   * Creates a new AuditManagerClient object.
   *
   * @param uid
   *          The user's identifier as a String object.
   *
   * @throws PastaAuthenticationException
   * @throws PastaConfigurationException
   */
  public AuditManagerClient(String uid) throws PastaAuthenticationException, PastaConfigurationException {
    super(uid);
    String pastaUrl = PastaClient.composePastaUrl(this.pastaProtocol, this.pastaHost, this.pastaPort);
    this.BASE_URL = pastaUrl + "/audit";
  }


  /*
   * Class methods
   */

  /**
   * Retrieves a list of recent inserts.
   *
   * @param numberOfDays   the number of prior days to search for inserts, for example,
   *                       the past 100 days
   * @param limit          an upper limit on the number of matches returned
   * @param forceRefresh   if true, refresh the search results regardless of when
   *                       they were last refreshed
   *
   * @return A list of RecentUpload objects, where each upload was an insert,
   *         i.e. the serviceMethod for each is "createDataPackage".
   */
	synchronized public static List<RecentUpload> getRecentInserts(Integer numberOfDays, Integer limit, boolean forceRefresh) {
		if (forceRefresh ||
			recentInserts == null ||
			recentInserts.size() < limit ||
			shouldRefresh(recentInsertsLastRefreshTime)
		   ) {
			recentInserts = getRecentUploads("createDataPackage", numberOfDays, limit);
		    Date now = new Date();
		    recentInsertsLastRefreshTime = now.getTime();
		}
		return recentInserts;
	}


  /**
   * Retrieves a list of recent updates.
   *
   * @param numberOfDays   the number of prior days to search for inserts, for example,
   *                       the past 100 days
   * @param limit          an upper limit on the number of matches returned
   * @param forceRefresh   if true, refresh the search results regardless of when
   *                       they were last refreshed
   *
   * @return A list of RecentUpload objects, where each upload was an update,
   *         i.e. the serviceMethod for each is "updateDataPackage".
   */
	synchronized public static List<RecentUpload> getRecentUpdates(Integer numberOfDays, Integer limit, boolean forceRefresh) {
		if (forceRefresh ||
			recentUpdates == null ||
			recentUpdates.size() < limit ||
			shouldRefresh(recentUpdatesLastRefreshTime)
		   ) {
			recentUpdates = getRecentUploads("updateDataPackage", numberOfDays, limit);
		    Date now = new Date();
		    recentUpdatesLastRefreshTime = now.getTime();
		}
		return recentUpdates;
	}


  /*
   * Retrieves the current cache of RecentUpload objects. Generates a new
   * list of RecentUpload objects if the cache is empty or if it's time
   * to refresh the cache.
   */
	private static List<RecentUpload> getRecentUploads(
            String serviceMethod,
            Integer numberOfDays,
            Integer limit
    ) {
        List<RecentUpload> recentUploads = new ArrayList<RecentUpload>();
        PropertiesConfiguration options = ConfigurationListener.getOptions();
        if (options != null) {
            String publicId = options.getString("edi.public.id");
            String uploadType = serviceMethod.equals("createDataPackage") ? "inserts" : "updates";
            logger.info(String.format("Start refresh of recent %s", uploadType));
            try {
                DataPackageManagerClient dpmc = new DataPackageManagerClient(publicId);
                // recentUploads = auditManagerClient.recentUploads(serviceMethod, numberOfDays, limit);
                String recentUploadsXML = dpmc.listRecentUploads(serviceMethod, limit);
                if ((recentUploadsXML != null) && (!recentUploadsXML.isEmpty())) {
                    recentUploads = parseRecentUploadsXML(dpmc, recentUploadsXML);
                }
            } catch (Exception e) {
                logger.error("Error refreshing recent uploads: " + e.getMessage());
            }
            logger.info(String.format("Finish refresh of recent %s", uploadType));
        }
		return recentUploads;
	}


	private static List<RecentUpload> parseRecentUploadsXML(DataPackageManagerClient dpmc, String xml) {
		List<RecentUpload> recentUploads = new ArrayList<RecentUpload>();
		String[] lines = xml.split("\n");
		StringBuilder sb = new StringBuilder("");

		for (String line : lines) {
			line = line + "\n";
			if (line.contains("<dataPackage>")) {
				sb = new StringBuilder(line);
			}
			else if (line.contains("</dataPackage>")) {
				sb.append(line);
				String dataPackageXML = sb.toString();
				String uploadDate = parseElement(dataPackageXML, "date");
				String serviceMethod = parseElement(dataPackageXML, "serviceMethod");
				String scope = parseElement(dataPackageXML, "scope");
				Integer identifier = new Integer(parseElement(dataPackageXML, "identifier"));
				Integer revision = new Integer(parseElement(dataPackageXML, "revision"));
				RecentUpload recentUpload = new RecentUpload(dpmc, uploadDate, serviceMethod, scope, identifier, revision);
				recentUploads.add(recentUpload);
			}
			else {
				sb.append(line);
			}
		}

		return recentUploads;
	}


	private static String parseElement(String xml, String elementName) {
		String elementText = "";

		String startTag = String.format("<%s>", elementName);
		String endTag = String.format("</%s>", elementName);
		int start = xml.indexOf(startTag) + startTag.length();
		int end = xml.indexOf(endTag);
		elementText = xml.substring(start, end);

		return elementText;
	}


  /*
   * Boolean to determine whether the cache of recent uploads is due to be
   * refreshed. Returns true is the current time has advanced
   * past the last refresh time plus a time-to-live period.
   */
  private static boolean shouldRefresh(long lastRefreshTime) {
	  int hours = 1;
	  boolean shouldRefresh = false;
	  final long refreshInterval = (long) (hours * 60 * 60 * 1000);
	  Date now = new Date();
	  long nowTime = now.getTime();
	  long refreshTime = lastRefreshTime + refreshInterval;

	  if (refreshTime < nowTime) {
		  shouldRefresh = true;
	  }

	  return shouldRefresh;
  }


  /*
   * Instance Methods
   */


  /*
   * Compose a fromTime date string to use with generating the list of
   * recent uploads.
   */
  private String composeFromTime(int numberOfDays) {
	  String fromTimeStr = "";
	  SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	  final long nDays = numberOfDays * 24 * 60 * 60 * 1000L; // set the time period for recent uploads

	  Date now = new Date();
	  long nowTime = now.getTime();
	  long fromTime = nowTime - nDays;
	  Date fromTimeDate = new Date(fromTime);
	  fromTimeStr = simpleDateFormat.format(fromTimeDate);

	  return fromTimeStr;
  }


  /**
   * Returns an audit report based on the provided oid (object id) value
   *
   * @param  oid 	the oid (object id) value of the audit report to be returned
   * @return The XML document of the report as a String object.
   * @throws PastaEventException
   */
  public String reportByOid(String oid) throws PastaEventException {

    String entity = null;
    Integer statusCode = null;
    HttpEntity responseEntity = null;

    CloseableHttpClient httpClient =
        HttpClientBuilder.create().setUserAgent("PASTA AuditManagerClient").build();
    HttpResponse response = null;
    HttpGet httpGet = new HttpGet(BASE_URL + "/report/" + oid);

    // Set header content
    if (this.token != null && this.ediToken != null) {
      httpGet.setHeader("Cookie", makePastaCookie(this.token, this.ediToken));
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
    }
    finally {
		closeHttpClient(httpClient);
 	}

    if (statusCode != HttpStatus.SC_OK) {

      // Something went wrong; return message from the response entity
      String gripe = "The AuditManager responded with response code '"
          + statusCode.toString() + "' and message '" + entity + "'\n";
      throw new PastaEventException(gripe);

    }

    return entity;

  }


  /**
   * Returns an audit report based on the provided query parameter filter.
   *
   * @param filter The query parameter filter as a String object.
   * @return The XML document of the report as a String object.
   * @throws PastaEventException
   */
  public MyPair<String, MyPair<Integer, Integer>> reportByFilter(String filter)
      throws PastaEventException
  {
    String entity = null;
    Integer statusCode = null;
    HttpEntity responseEntity = null;

    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponse response = null;
    HttpGet httpGet = new HttpGet(BASE_URL + "/report?" + filter);

    // Set header content
    if (this.token != null && this.ediToken != null) {
      httpGet.setHeader("Cookie", makePastaCookie(this.token, this.ediToken));
    }

		int firstOidInt = 0;
		int lastOidInt = 0;

    try {
      response = httpClient.execute(httpGet);
      statusCode = (Integer) response.getStatusLine().getStatusCode();
      responseEntity = response.getEntity();

      Header[] firstOidHeaders = response.getHeaders("PASTA-First-OID");
			if (firstOidHeaders.length > 0) {
				String firstOidString = firstOidHeaders[0].getValue();
				firstOidInt = Integer.parseInt(firstOidString);
			}

      Header[] lastOidHeaders = response.getHeaders("PASTA-Last-OID");
			if (lastOidHeaders.length > 0) {
				String lastOidString = lastOidHeaders[0].getValue();
				lastOidInt = Integer.parseInt(lastOidString);
			}

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
      String gripe =
          "The AuditManager responded with response code '" + statusCode.toString() +
              "' and message '" + entity + "'\n";
      throw new PastaEventException(gripe);

    }

    return new MyPair<>(entity, new MyPair<>(firstOidInt, lastOidInt));
  }

  /**
   * Returns an audit report based on the provided query parameter filter.
   *
   * @param filter The query parameter filter as a String object.
   * @return The CSV file stream.
   * @throws PastaEventException
   */
  public InputStream reportByFilterCsv(String filter)
      throws PastaEventException
  {
    Integer statusCode = null;
    HttpEntity responseEntity = null;
    InputStream inputStream = null;

    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponse response = null;
    HttpGet httpGet = new HttpGet(BASE_URL + "/csvreport?" + filter);

    // Set header content
    if (this.token != null && this.ediToken != null) {
      httpGet.setHeader("Cookie", makePastaCookie(this.token, this.ediToken));
    }

    try {
      response = httpClient.execute(httpGet);
      statusCode = (Integer) response.getStatusLine().getStatusCode();
      responseEntity = response.getEntity();

      if (responseEntity != null) {
        inputStream = responseEntity.getContent();
      }
    } catch (IOException e) {
      logger.error(e);
      e.printStackTrace();
    } finally {
//      closeHttpClient(httpClient);
    }

    if (statusCode != HttpStatus.SC_OK) {
      // Something went wrong; return message from the response entity
      String gripe =
          "The AuditManager responded with response code '" + statusCode.toString() + "'\n";
      throw new PastaEventException(gripe);
    }

    return inputStream;
  }

  /**
   * Returns an audit report based on the provided query parameter filter.
   *
   * @param scope   the data package scope value
   * @param identifier the data package identifier value
   * @return The XML document of the docId reads report.
   * @throws PastaEventException
   */
  public String getDocIdReads(String scope, String identifier)
		  throws PastaEventException {
	  String serviceUrl = String.format("%s/reads/%s/%s",
	    		                          BASE_URL, scope, identifier);
	  String readsXML = getReadsXML(serviceUrl);
	  return readsXML;
  }


  /**
   * Returns an audit report based on the provided query parameter filter.
   *
   * @param scope   the data package scope value
   * @param identifier the data package identifier value
   * @param revision the data package revision value
   * @return The XML document of the packageId reads report.
   * @throws PastaEventException
   */
  public String getPackageIdReads(String scope,
		                          String identifier,
		                          String revision)
		  throws PastaEventException {
	  String serviceUrl = String.format("%s/reads/%s/%s/%s",
	    		                          BASE_URL, scope, identifier, revision);
	  String readsXML = getReadsXML(serviceUrl);
	  return readsXML;
  }


  /**
   * Returns an audit report based on the provided query parameter filter.
   *
   * @param serviceUrl   the audit service URL for the resource reads service
   * @return The XML document of the docId or packageId reads report.
   * @throws PastaEventException
   */
	private String getReadsXML(String serviceUrl) throws PastaEventException {

		String entity = null;
		Integer statusCode = null;
		HttpEntity responseEntity = null;

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		HttpGet httpGet = new HttpGet(serviceUrl);

		// Set header content
		if (this.token != null && this.ediToken != null) {
			httpGet.setHeader("Cookie", makePastaCookie(this.token, this.ediToken));
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
			String gripe = "The AuditManager responded with response code '" +
			               statusCode.toString() +
			               "' and message '" +
			               entity +
			               "'\n";
			logger.error(gripe);
//			throw new PastaEventException(gripe);

		}

		return entity;
	}

}
