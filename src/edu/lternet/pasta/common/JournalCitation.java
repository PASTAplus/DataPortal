package edu.lternet.pasta.common;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.xpath.CachedXPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.lternet.pasta.common.eml.DataPackage;
import edu.lternet.pasta.common.eml.DataPackage.DataSource;


public class JournalCitation {
    
    /*
     * Class variables
     */
    
    private static Logger logger = Logger.getLogger(JournalCitation.class);

    
    /*
     * Instance variables
     */
    
    int journalCitationId;
    String articleTitle;
    String articleDoi;
    String articleUrl;
    String principalOwner;
    LocalDateTime dateCreated;
    String packageId;
    String journalTitle;
    

    /*
     * Constructors
     */
    
    /**
     * Create a new JournalCitation object. The empty constructor.
     * 
      */
    public JournalCitation() {
        super();
    }
    
    
    /**
     * Create a new JournalCitation object by parsing the journal citation XML string.
     * 
     * @param xml   an XML string that conforms to the journal citation format, typically sent in
     *              a web service request body
     */
    public JournalCitation(String xml) {
        parseDocument(xml);
    }
    
    
    /*
     * Class methods
     */
    
    public static ArrayList<JournalCitation> xmlToJournalCitations(String journalCitationsXML) {
        ArrayList<JournalCitation> journalCitations = new ArrayList<JournalCitation>();

        if (journalCitationsXML != null && !journalCitationsXML.isEmpty()) {
            InputStream inputStream = null;
            try {
                inputStream = IOUtils.toInputStream(journalCitationsXML, "UTF-8");
                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                CachedXPathAPI xpathapi = new CachedXPathAPI();

                Document document = null;
                document = documentBuilder.parse(inputStream);

                if (document != null) {
                    NodeList journalCitationNodes = xpathapi.selectNodeList(document, "//journalCitation");

                    int nCitations = journalCitationNodes.getLength();
                    for (int i = 0; i < nCitations; i++) {
                        JournalCitation journalCitation = new JournalCitation();
                        String packageId = null;
                        String articleTitle = null;
                        String articleDoi = null;
                        String articleUrl = null;
                        String journalTitle = null;
                        String principalOwner = null;
                        Node journalCitationNode = journalCitationNodes.item(i);
 
                        Node packageIdNode = xpathapi.selectSingleNode(journalCitationNode, "packageId");
                        if (packageIdNode != null) {
                            packageId = packageIdNode.getTextContent();
                            journalCitation.setPackageId(packageId);
                        }

                        Node articleDoiNode = xpathapi.selectSingleNode(journalCitationNode, "articleDoi");
                        if (articleDoiNode != null) {
                            articleDoi = articleDoiNode.getTextContent();
                            journalCitation.setArticleDoi(articleDoi);
                        }

                        Node articleUrlNode = xpathapi.selectSingleNode(journalCitationNode, "articleUrl");
                        if (articleUrlNode != null) {
                            articleUrl = articleUrlNode.getTextContent();
                            journalCitation.setArticleUrl(articleUrl);
                        }

                        Node articleTitleNode = xpathapi.selectSingleNode(journalCitationNode, "articleTitle");
                        if (articleTitleNode != null) {
                            articleTitle = articleTitleNode.getTextContent();
                            journalCitation.setArticleTitle(articleTitle);
                        }

                        Node journalTitleNode = xpathapi.selectSingleNode(journalCitationNode, "journalTitle");
                        if (journalTitleNode != null) {
                            journalTitle = journalTitleNode.getTextContent();
                            journalCitation.setJournalTitle(journalTitle);
                        }

                        Node principalOwnerNode = xpathapi.selectSingleNode(journalCitationNode, "principalOwner");
                        if (principalOwnerNode != null) {
                            principalOwner = principalOwnerNode.getTextContent();
                            journalCitation.setPrincipalOwner(principalOwner);
                        }

                        journalCitations.add(journalCitation);
                    }
                }
            } catch (Exception e) {
                logger.error("Error parsing journal citations XML: " + e.getMessage());
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        ;
                    }
                }
            }
        }

        return journalCitations;
    }
    
    
    /*
     * Instance methods
     */
    
    
    /**
     * Parses an EML document.
     * 
     * @param   xml          The XML string representation of the EML document
     * @return  dataPackage  a DataPackage object holding parsed values
     */
    private void parseDocument(String xml) {
      if (xml != null) {
        try {
          InputStream inputStream = IOUtils.toInputStream(xml, "UTF-8");
          parseDocument(inputStream);
        }
        catch (Exception e) {
          logger.error("Error parsing journal citation metadata: " + e.getMessage());
        }
      }
    }

 
    /**
     * Parses an EML document.
     * 
     * @param   inputStream          the input stream to the EML document
     * @return  dataPackage          a DataPackage object holding parsed values
     */
    private void parseDocument(InputStream inputStream) 
            throws Exception {
      
      DocumentBuilder documentBuilder = 
                DocumentBuilderFactory.newInstance().newDocumentBuilder();
      CachedXPathAPI xpathapi = new CachedXPathAPI();

      Document document = null;

      try {
        document = documentBuilder.parse(inputStream);
        
        if (document != null) {

            Node journalCitationIdNode = xpathapi.selectSingleNode(document, "//journalCitationId");
            if (journalCitationIdNode != null) {
              String journalCitationIdStr = journalCitationIdNode.getTextContent();
              setJournalCitationId(Integer.parseInt(journalCitationIdStr));
            }
            
            Node packageIdNode = xpathapi.selectSingleNode(document, "//packageId");
            if (packageIdNode != null) {
              String packageId = packageIdNode.getTextContent();
              setPackageId(packageId);
            }
            
            Node articleDoiNode = xpathapi.selectSingleNode(document, "//articleDoi");
            if (articleDoiNode != null) {
              String articleDoi = articleDoiNode.getTextContent();
              setArticleDoi(articleDoi);
            }

            Node articleUrlNode = xpathapi.selectSingleNode(document, "//articleUrl");
            if (articleUrlNode != null) {
              String articleUrl = articleUrlNode.getTextContent();
              setArticleUrl(articleUrl);
            }

            Node articleTitleNode = xpathapi.selectSingleNode(document, "//articleTitle");
            if (articleTitleNode != null) {
              String articleTitle = articleTitleNode.getTextContent();
              setArticleTitle(articleTitle);
            }

            Node journalTitleNode = xpathapi.selectSingleNode(document, "//journalTitle");
            if (journalTitleNode != null) {
              String journalTitle = journalTitleNode.getTextContent();
              setJournalTitle(journalTitle);
            }
            
            Node dateCreatedNode = xpathapi.selectSingleNode(document, "//dateCreated");
            if (dateCreatedNode != null) {
              String dateCreated = dateCreatedNode.getTextContent();
              setDateCreated(LocalDateTime.parse(dateCreated));
            }
            
        }
      }
      catch (SAXException e) {
          logger.error("Error parsing document: SAXException");
          e.printStackTrace();
          throw(e);
        } 
        catch (IOException e) {
          logger.error("Error parsing document: IOException");
          e.printStackTrace();
          throw(e);
        }
        catch (TransformerException e) {
          logger.error("Error parsing document: TransformerException");
          e.printStackTrace();
          throw(e);
        }
    }
    
    
    /**
     * Composes the XML representation of this JournalCitation object 
     * 
     * @return  an XML string representation
     */
    public String toXML(boolean includeDeclaration) {
        String firstLine = includeDeclaration ? "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" : "";
        StringBuilder sb = new StringBuilder(firstLine);
        sb.append("<journalCitation>\n");
        
        if (this.journalCitationId > 0)
            { sb.append(String.format("    <journalCitationId>%d</journalCitationId>\n", this.journalCitationId)); } 
        
        sb.append(String.format("    <packageId>%s</packageId>\n", this.packageId)); 
        sb.append(String.format("    <principalOwner>%s</principalOwner>\n", this.principalOwner)); 
        sb.append(String.format("    <dateCreated>%s</dateCreated>\n", getDateCreatedStr())); 
        
        if (this.articleDoi != null)
            { sb.append(String.format("    <articleDoi>%s</articleDoi>\n", this.articleDoi)); }
        
        if (this.articleTitle != null)
            { sb.append(String.format("    <articleTitle>%s</articleTitle>\n", this.articleTitle)); } 
        
        if (this.articleUrl != null)
            { sb.append(String.format("    <articleUrl>%s</articleUrl>\n", this.articleUrl)); } 
    
        if (this.journalTitle != null)
            { sb.append(String.format("    <journalTitle>%s</journalTitle>\n", this.journalTitle)); } 
        
        sb.append("</journalCitation>\n");

        String xml = sb.toString();
        return xml;
    }
    
    
    public String toHTML() {
        String html = null;
        StringBuffer sb = new StringBuffer("");
        String articleDoiUrl = null;
        String articleTitle = getArticleTitle();
        String journalTitle = getJournalTitle();
        
        String articleDoi = getArticleDoi();
        if (articleDoi != null && !articleDoi.isEmpty()) {
            articleDoiUrl = String.format("http://dx.doi.org/%s", articleDoi);
        }
        
        if (articleUrl != null) {
            if (articleTitle != null && !articleTitle.isEmpty()) {
                sb.append(String.format("<a class='searchsubcat' href='%s'>%s</a>", articleUrl, articleTitle));
            }
            else {
                sb.append(String.format("<a class='searchsubcat' href='%s'>%s</a>", articleUrl, articleUrl));
            }
        }
        else if (articleDoiUrl != null) {
            if (articleTitle != null && !articleTitle.isEmpty()) {
                sb.append(String.format("<a class='searchsubcat' href='%s'>%s</a>", articleDoiUrl, articleTitle));
            }
            else {
                sb.append(String.format("<a class='searchsubcat' href='%s'>%s</a>", articleDoiUrl, articleDoiUrl));
            }
        }
        else {
            sb.append(articleTitle);
        }
        
        if (journalTitle != null && !journalTitle.isEmpty()) {
            sb.append(String.format(", %s", journalTitle));
        }
        
        html = sb.toString();
        return html;
    }
    
    
    private String getDateCreatedStr() {
        String dateCreatedStr = "";
        if (this.dateCreated != null) {
            dateCreatedStr = dateCreated.toString();
        }
        
        return dateCreatedStr;
    }
    
    
    /*
     * Accessors
     */
    
    public String getArticleTitle() {
        return articleTitle;
    }

    public void setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
    }

    public String getArticleDoi() {
        return articleDoi;
    }

    public void setArticleDoi(String articleDoi) {
        this.articleDoi = articleDoi;
    }
    
    public String getArticleUrl() {
        return articleUrl;
    }

    public void setArticleUrl(String articleUrl) {
        this.articleUrl = articleUrl;
    }
    
    public int getJournalCitationId() {
        return journalCitationId;
    }
    
    public void setJournalCitationId(int val) {
        this.journalCitationId = val;
    }

    public String getPrincipalOwner() {
        return principalOwner;
    }

    public void setPrincipalOwner(String principalOwner) {
        this.principalOwner = principalOwner;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime localDateTime) {
        this.dateCreated = localDateTime;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getJournalTitle() {
        return journalTitle;
    }

    public void setJournalTitle(String journalTitle) {
        this.journalTitle = journalTitle;
    }

}
