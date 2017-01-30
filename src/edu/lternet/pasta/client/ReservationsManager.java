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

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
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
 * @since January 27, 2017
 * 
 *        The ReservationsManager supports the management of data package
 *        identifier reservations. The user must be authorized and have a
 *        valid authentication token to utilize this service.
 * 
 */
public class ReservationsManager extends PastaClient {

  /*
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.client.ReservationsManager.class);
  
  
  /* 
   * The maximum number of identifiers that an end user can reserve with a
   * single button click. This is the default value, but it can be set to
   * a different value by reading the value of dataportal.maxNumberOfReservations 
   * from the dataportal.properties file and calling the setter access method
   * below.
   */
  private static int maxNumberOfReservations = 3;

  
  /*
   * Instance variables
   */


  /*
   * Constructors
   */

  /**
   * Creates a new EventService object and sets the user's authentication token
   * if it exists; otherwise an error.
   * 
   * @param uid
   *          The user's identifier as a String object.
   * 
   * @throws PastaAuthenticationException
   * @throws PastaConfigurationException
   */
  public ReservationsManager(String uid)
      throws PastaAuthenticationException, PastaConfigurationException {
    super(uid);
  }
  
  
  /*
   * Class Methods
   */
  
  
  /**
   * Sets the maxNumberOfReservations value to the specified integer.
   * 
   * @param n  the maximum number of identifiers that an end user can ask
   *           to have reserved with a single button click
   */
  public static void setMaxNumberOfReservations(int n) {
	  maxNumberOfReservations = n;
  }
  

  /*
   * Instance Methods
   */

  
	/**
	 * Reads the list of active reservations in PASTA.
	 * 
	 * @return the list of active reservations in PASTA as an XML string
	 * 
	 * @throws Exception
	 */
	public String listActiveReservations() throws Exception {
		DataPackageManagerClient dpmc = new DataPackageManagerClient(uid);
		String xml = dpmc.listActiveReservations();

		return xml;
	}

	
    /**
     * Return the number of subscriptions for a given user.
     * 
     * @return  the number of subscriptions for this user.
     */
	public int numberOfReservations() throws Exception {
		int numberOfReservations = 0;
		final String principalTest = String.format("uid=%s,", this.uid);

		if (this.uid != null && !this.uid.equals("public")) {
			String xmlString = listActiveReservations();

			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

			try {
				DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
				InputStream inputStream = IOUtils.toInputStream(xmlString, "UTF-8");
				Document document = documentBuilder.parse(inputStream);
				Element documentElement = document.getDocumentElement();
				NodeList reservations = documentElement.getElementsByTagName("reservation");
				int nReservations = reservations.getLength();

				for (int i = 0; i < nReservations; i++) {
					Node reservationNode = reservations.item(i);
					NodeList reservationChildren = reservationNode.getChildNodes();
					String principal = "";
					for (int j = 0; j < reservationChildren.getLength(); j++) {
						Node childNode = reservationChildren.item(j);
						if (childNode instanceof Element) {
							Element reservationElement = (Element) childNode;
							
							if (reservationElement.getTagName().equals("principal")) {
								Text text = (Text) reservationElement.getFirstChild();
								if (text != null) {
									principal = text.getData().trim();
									if (principal.startsWith(principalTest)) {
										numberOfReservations++;
									}
								}
							}
						}
					}
				}
			}
			catch (Exception e) {
				logger.error("Exception:\n" + e.getMessage());
				e.printStackTrace();
				throw new PastaEventException(e.getMessage());
			}
		}
		
		return numberOfReservations;
	}

	
	
	/**
	 * Composes HTML table rows to render the list of active reservations for
	 * this user.
	 * 
	 * @return an HTML snippet of table row (<tr>) elements, one per
	 *         active data package identifier reservation for this user.
	 * @throws Exception
	 */
	public String reservationsTableHTML() throws Exception {
		String html;
		StringBuilder sb = new StringBuilder("");

		final String principalTest = String.format("uid=%s,", this.uid);

		if (this.uid != null && !this.uid.equals("public")) {
			String xmlString = listActiveReservations();

			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

			try {
				DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
				InputStream inputStream = IOUtils.toInputStream(xmlString, "UTF-8");
				Document document = documentBuilder.parse(inputStream);
				Element documentElement = document.getDocumentElement();
				NodeList reservations = documentElement.getElementsByTagName("reservation");
				int nReservations = reservations.getLength();

				for (int i = 0; i < nReservations; i++) {
					Node reservationNode = reservations.item(i);
					NodeList reservationChildren = reservationNode.getChildNodes();
					String docid = "";
					String principal = "";
					String dateReserved = "";
					boolean include = false;
					for (int j = 0; j < reservationChildren.getLength(); j++) {
						Node childNode = reservationChildren.item(j);
						if (childNode instanceof Element) {
							Element reservationElement = (Element) childNode;
							
							if (reservationElement.getTagName().equals("principal")) {
								Text text = (Text) reservationElement.getFirstChild();
								if (text != null) {
									principal = text.getData().trim();
									if (principal.startsWith(principalTest)) {
										include = true;
									}
								}
							}
							else if (reservationElement.getTagName().equals("docid")) {
								Text text = (Text) reservationElement.getFirstChild();
								if (text != null) {
									docid = text.getData().trim();
								}
							}
							else if (reservationElement.getTagName().equals("dateReserved")) {
								Text text = (Text) reservationElement.getFirstChild();
								if (text != null) {
									dateReserved = text.getData().trim();
								}
							}
						}
					}
					
					if (include) {
						sb.append("<tr>\n");

						sb.append("  <td class='nis' align='center'>");
						sb.append(docid);
						sb.append("</td>\n");

						sb.append("  <td class='nis' align='center'>");
						sb.append(principal);
						sb.append("</td>\n");

						sb.append("  <td class='nis'>");
						sb.append(dateReserved);
						sb.append("</td>\n");

						sb.append("</tr>\n");
					}
				}		
			}
			catch (Exception e) {
				logger.error("Exception:\n" + e.getMessage());
				e.printStackTrace();
				throw new PastaEventException(e.getMessage());
			}
		}
		
		html = sb.toString();
		return html;
	}

	
	/**
	 * Builds an options list for the number of reservations the end user is
	 * requesting with a single button click.
	 * 
	 * @return the options HTML to be inserted into the <select> element
	 * @throws PastaEventException
	 */
	public String reservationsOptionsHTML() 
			throws PastaEventException {
		String html = "";
		StringBuffer sb = new StringBuffer("");
		
		for (int i = 1; i <= maxNumberOfReservations; i++) {
			sb.append(String.format("  <option value=\"%d\">%d</option>\n", i, i));
		}
		
		html = sb.toString();
		return html;
	}

}
