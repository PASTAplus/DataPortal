package edu.lternet.pasta.portal;

import edu.lternet.pasta.client.RidareClient;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;


public class JournalCitationsUtil
{
    private static final Logger logger = Logger.getLogger(JournalCitationsUtil.class);
    private static final String NOT_FOUND = "<not found>";

    public static Document parseXML(String xml) throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        try (StringReader stringReader = new StringReader(xml)) {
            InputSource inputSource = new InputSource(stringReader);
            return builder.parse(inputSource);
        }
    }


    public static Map<String, Object> getMetaFromRidare(String packageId)
    {
        // Cardinalities:
        // - title is 1-inf
        // - pubDate is 1
        // Strategy:
        // Pick the first title
        // Pick the only pubDate

        Map<String, Object> metaMap = new TreeMap<>();

        Document datasetDoc = getRidareDataset(packageId);

        if (datasetDoc == null) {
            metaMap.put("title", NOT_FOUND);
            metaMap.put("pubDate", NOT_FOUND);
            metaMap.put("creators", Collections.singletonList(NOT_FOUND));
            return metaMap;
        }

        Element rootEl = datasetDoc.getDocumentElement();

        String title = safeGetElementText(rootEl, "title");
        metaMap.put("title", title.isEmpty() ? NOT_FOUND : title);

        String pubDate = safeGetElementText(rootEl, "pubDate");
        metaMap.put("pubDate", title.isEmpty() ? NOT_FOUND : pubDate);

        List<String> creators = getCreatorNames(datasetDoc);
        metaMap.put("creators", creators.isEmpty() ? Collections.singletonList(NOT_FOUND) : creators);

        return metaMap;
    }

    public static Document getRidareDataset(String packageId)
    {
        try {
            RidareClient ridareClient = new RidareClient("public");
            return ridareClient.fetchXml(packageId, "//dataset");
        } catch (Exception e) {
			logger.error("Exception:\n" + e.getMessage());
			e.printStackTrace();
            return null;
		}
    }

    public static List<String> getCreatorNames(Document doc)
    {
        List<String> creators = new ArrayList<>();
        NodeList creatorNodes = doc.getElementsByTagName("creator");
        for (int i = 0; i < creatorNodes.getLength(); i++) {
            Element creatorEl = (Element) creatorNodes.item(i);
            NodeList individualNameNodes = creatorEl.getElementsByTagName("individualName");
            for (int j = 0; j < individualNameNodes.getLength(); j++) {
                Element individualNameEl = (Element) individualNameNodes.item(j);
                creators.add(formatIndividualName(individualNameEl));
            }
        }
        return creators;
    }

    // Cardinalities:
    // - creator is 1-inf
    // -- individualName is 1-inf
    // --- salutation is 0-inf
    // --- givenName is 0-inf
    // --- surName is 1
    // Strategy:
    // Ignore salutation.
    // Start string with "surName"
    // Add "," after "surName" if givenName is present
    // sort givenName into two groups, single character and not
    // Add multi-character givenNames to the string, with space between each
    // Add single character givenNames to the string, with space between each
    public static String formatIndividualName(Element individualNameEl)
    {
        List<String> middleGivenNames = new ArrayList<>();
        List<String> longGivenNames = new ArrayList<>();
        NodeList givenNameNodes = individualNameEl.getElementsByTagName("givenName");
        for (int i = 0; i < givenNameNodes.getLength(); i++) {
            Element givenNameEl = (Element) givenNameNodes.item(i);
            String givenName = givenNameEl.getTextContent();
            if (givenName.length() == 1) {
                middleGivenNames.add(givenName);
            } else if (givenName.length() > 1) {
                longGivenNames.add(givenName);
            }
        }
        List<String> nameParts = new ArrayList<>();
        String surName = safeGetElementText(individualNameEl, "surName");
        if (!surName.isEmpty()) {
            nameParts.add(surName + (givenNameNodes.getLength() > 0 ? "," : ""));
        }
        nameParts.addAll(longGivenNames);
        nameParts.addAll(middleGivenNames);
        return String.join(" ", nameParts);
    }


    public static String safeGetElementText(Element element, String tagName)
    {
        try {
            return element.getElementsByTagName(tagName).item(0).getTextContent();
        } catch (Exception e) {
            return "";
        }
    }
}
