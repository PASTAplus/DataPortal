package edu.lternet.pasta.common;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.xpath.CachedXPathAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.encoder.Encode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class JournalCitation {
    public static class ArticleAuthor {
        Integer sequence;
        String given;
        String family;
        String suffix;
        String shortOrcid;

        public ArticleAuthor(Integer sequence, String given, String family, String suffix, String orcid) {
            orcid = emptyToNull(orcid);
            String shortOrcid = orcidUrlToShort(orcid);
            assertOrcidCheckDigit(shortOrcid);
            this.sequence = sequence;
            this.given = emptyToNull(given);
            this.family = emptyToNull(family);
            this.suffix = emptyToNull(suffix);
            this.shortOrcid = emptyToNull(shortOrcid);
        }

        private String emptyToNull(String s) {
            if (s == null || s.isEmpty()) {
                return null;
            }
            return s;
        }
        public Integer getSequence() {
            return sequence;
        }

        public String getGiven() {
            return given;
        }

        public String getFamily() {
            return family;
        }

        public String getSuffix() {
            return suffix;
        }

        public String getShortOrcid() {
            return shortOrcid;
        }

        public String getOrcidUrl() {
            return orcidShortToUrl(shortOrcid);
        }

        public static String orcidUrlToShort(String orcidUrl) {
            if (orcidUrl != null) {
                String orcidShortStr = orcidUrl.replaceAll("^https?://orcid.org/|-", "");
                assert orcidShortStr.length() == 16;
                return orcidShortStr;
            }
            return null;
        }

        public static String orcidShortToUrl(String orcidStr) {
            if (orcidStr != null) {
                assert orcidStr.length() == 16;
                String formattedOrcid = orcidStr.replaceAll("(.{4})(?!$)", "$1-");
                return "https://orcid.org/" + formattedOrcid;
            }
            return null;
        }

        public static void assertOrcidCheckDigit(String orcidStr) {
            assert orcidStr == null || orcidStr.charAt(15) == generateOrcidCheckDigit(orcidStr);
        }

        public static char generateOrcidCheckDigit(String orcidStr) {
            assert orcidStr.length() == 16;
            int total = 0;
            for (int i = 0; i < 15; i++) {
                int digit = Character.getNumericValue(orcidStr.charAt(i));
                total = (total + digit) * 2;
            }
            int remainder = total % 11;
            int result = (12 - remainder) % 11;
            return (result == 10) ? 'X' : (char) ('0' + result);
        }
    }

    /*
     * Class variables
     */

    private static Logger logger = Logger.getLogger(JournalCitation.class);

    /*
     * Instance variables
     */

    Integer journalCitationId;
    String articleTitle;
    String articleDoi;
    String articleUrl;
    String principalOwner;
    LocalDateTime dateCreated;
    String packageId;
    String journalTitle;
    String relationType;
    Integer journalPubYear;
    ArrayList<ArticleAuthor> articleAuthorList = new ArrayList<>();
    String journalIssue;
    String journalVolume;
    String articlePages;

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

    public JournalCitation(JSONObject json) throws ParseException {
        try {
            this.journalCitationId = json.getInt("journalCitationId");
        } catch (JSONException ignored) {
        }
        this.packageId = json.getString("packageId");
        this.articleDoi = json.getString("articleDoi");
        this.articleUrl = json.getString("articleUrl");
        this.articleTitle = json.getString("articleTitle");
        this.journalTitle = json.getString("journalTitle");
        this.relationType = json.getString("relationType");
        setJournalPubYear(json.getString("journalPubYear"));
        if (!json.isNull("articleAuthorList")) {
            setArticleAuthorList(json.getJSONArray("articleAuthorList"));
        }
        setJournalIssue(json.getString("journalIssue"));
        setJournalVolume(json.getString("journalVolume"));
        setArticlePages(json.getString("articlePages"));
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
        ArrayList<JournalCitation> journalCitations = new ArrayList<>();

        if (journalCitationsXML != null && !journalCitationsXML.isEmpty()) {
            InputStream inputStream = null;
            try {
                inputStream = IOUtils.toInputStream(journalCitationsXML, "UTF-8");
                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                CachedXPathAPI xpathapi = new CachedXPathAPI();

                Document document = documentBuilder.parse(inputStream);

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
                        String relationType = null;
                        String principalOwner = null;
                        String journalIssue = null;
                        String journalVolume = null;
                        String articlePages = null;

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

                        Node relationTypeNode = xpathapi.selectSingleNode(document, "//relationType");
                        if (relationTypeNode != null) {
                          relationType = relationTypeNode.getTextContent();
                          journalCitation.setRelationType(relationType);
                        }

                        Node pubDateNode = xpathapi.selectSingleNode(journalCitationNode, "pubDate");
                        if (pubDateNode != null) {
                            journalCitation.setJournalPubYear(pubDateNode.getTextContent());
                        }

                        Node principalOwnerNode = xpathapi.selectSingleNode(journalCitationNode, "principalOwner");
                        if (principalOwnerNode != null) {
                            principalOwner = principalOwnerNode.getTextContent();
                            journalCitation.setPrincipalOwner(principalOwner);
                        }

                        Node journalIssueNode = xpathapi.selectSingleNode(journalCitationNode, "journalIssue");
                        if (journalIssueNode != null) {
                            journalIssue = journalIssueNode.getTextContent();
                            journalCitation.setJournalIssue(journalIssue);
                        }

                        Node journalVolumeNode = xpathapi.selectSingleNode(journalCitationNode, "journalVolume");
                        if (journalVolumeNode != null) {
                            journalVolume = journalVolumeNode.getTextContent();
                            journalCitation.setJournalVolume(journalVolume);
                        }

                        Node articlePagesNode = xpathapi.selectSingleNode(journalCitationNode, "articlePages");
                        if (articlePagesNode != null) {
                            articlePages = articlePagesNode.getTextContent();
                            journalCitation.setArticlePages(articlePages);
                        }

                        Node articleAuthorNode = xpathapi.selectSingleNode(journalCitationNode, "articleAuthors");
                        if (articleAuthorNode != null) {
                            journalCitation.setArticleAuthorList(articleAuthorNode);
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
              String citationIdStr = journalCitationIdNode.getTextContent();
              setJournalCitationId(Integer.parseInt(citationIdStr));
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

            Node articleTitleNode = xpathapi.selectSingleNode(document, "//articleTitle");
            if (articleTitleNode != null) {
              String articleTitle = articleTitleNode.getTextContent();
              setArticleTitle(articleTitle);
            }

            Node articleUrlNode = xpathapi.selectSingleNode(document, "//articleUrl");
            if (articleUrlNode != null) {
              String articleUrl = articleUrlNode.getTextContent();
              setArticleUrl(articleUrl);
            }

            Node journalTitleNode = xpathapi.selectSingleNode(document, "//journalTitle");
            if (journalTitleNode != null) {
              String journalTitle = journalTitleNode.getTextContent();
              setJournalTitle(journalTitle);
            }

            Node pubDateNode = xpathapi.selectSingleNode(document, "//pubDate");
            if (pubDateNode != null) {
              String pubDate = pubDateNode.getTextContent();
              setJournalPubYear(pubDate);
            }

            Node dateCreatedNode = xpathapi.selectSingleNode(document, "//dateCreated");
            if (dateCreatedNode != null) {
              String dateCreated = dateCreatedNode.getTextContent();
              setDateCreated(LocalDateTime.parse(dateCreated));
            }

            Node articleAuthorNode = xpathapi.selectSingleNode(document, "//articleAuthors");
            if (articleAuthorNode != null) {
                setArticleAuthorList(articleAuthorNode);
            }

            Node relationTypeNode = xpathapi.selectSingleNode(document, "//relationType");
            if (relationTypeNode != null) {
                setRelationType(relationTypeNode.getTextContent());
            }

            Node journalIssueNode = xpathapi.selectSingleNode(document, "//journalIssue");
            if (journalIssueNode != null) {
                setJournalIssue(journalIssueNode.getTextContent());
            }

            Node journalVolumeNode = xpathapi.selectSingleNode(document, "//journalVolume");
            if (journalVolumeNode != null) {
                setJournalVolume(journalVolumeNode.getTextContent());
            }

            Node articlePagesNode = xpathapi.selectSingleNode(document, "//articlePages");
            if (articlePagesNode != null) {
                setArticlePages(articlePagesNode.getTextContent());
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

    private void setArticleAuthorList(Node articleAuthorNode) throws TransformerException {
        CachedXPathAPI xpathapi = new CachedXPathAPI();
        NodeList authorNodeList = xpathapi.selectNodeList(articleAuthorNode, "author");

        articleAuthorList.clear();

        for (int i = 0; i < authorNodeList.getLength(); i++) {
            Node authorNode = authorNodeList.item(i);
            Node node;
            node = xpathapi.selectSingleNode(authorNode, "sequence");
            Integer sequence = node != null ? Integer.parseInt(node.getTextContent()) : null;
            node = xpathapi.selectSingleNode(authorNode, "given");
            String given = node != null ? node.getTextContent() : null;
            node = xpathapi.selectSingleNode(authorNode, "family");
            String family = node != null ? node.getTextContent() : null;
            node = xpathapi.selectSingleNode(authorNode, "suffix");
            String suffix = node != null ? node.getTextContent() : null;
            node = xpathapi.selectSingleNode(authorNode, "orcid");
            String orcid = node != null ? node.getTextContent() : null;
            articleAuthorList.add(new ArticleAuthor(sequence, given, family, suffix, orcid));
        }
    }


    /**
     * Composes the XML representation of this JournalCitation object
     *
     * @return  an XML string representation
     */
    public String toXML(boolean includeDeclaration) {
        String firstLine = includeDeclaration ? "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" : "";
        StringBuilder xmlBuilder = new StringBuilder(firstLine);
        xmlBuilder.append("<journalCitation>\n");
        xmlBuilder.append(String.format("    <journalCitationId>%s</journalCitationId>\n", encodeForXml(this.journalCitationId)));
        xmlBuilder.append(String.format("    <packageId>%s</packageId>\n", encodeForXml(this.packageId)));
        xmlBuilder.append(String.format("    <principalOwner>%s</principalOwner>\n", encodeForXml(this.principalOwner)));
        xmlBuilder.append(String.format("    <dateCreated>%s</dateCreated>\n", encodeForXml(getDateCreatedStr())));
        xmlBuilder.append(String.format("    <articleDoi>%s</articleDoi>\n", encodeForXml(this.articleDoi)));
        xmlBuilder.append(String.format("    <articleTitle>%s</articleTitle>\n", encodeForXml(this.articleTitle)));
        xmlBuilder.append(String.format("    <articleUrl>%s</articleUrl>\n", encodeForXml(this.articleUrl)));
        xmlBuilder.append(String.format("    <journalTitle>%s</journalTitle>\n", encodeForXml(this.journalTitle)));
        xmlBuilder.append(String.format("    <relationType>%s</relationType>\n", encodeForXml(this.relationType)));
        xmlBuilder.append(String.format("    <pubDate>%s</pubDate>\n", encodeForXml(this.journalPubYear)));
        xmlBuilder.append(String.format("    <journalIssue>%s</journalIssue>\n", encodeForXml(this.journalIssue)));
        xmlBuilder.append(String.format("    <journalVolume>%s</journalVolume>\n", encodeForXml(this.journalVolume)));
        xmlBuilder.append(String.format("    <articlePages>%s</articlePages>\n", encodeForXml(this.articlePages)));
        xmlBuilder.append("    <articleAuthors>\n");
        if (this.articleAuthorList != null) {
            for (ArticleAuthor author : this.articleAuthorList) {
                xmlBuilder.append("        <author>\n");
                xmlBuilder.append(String.format("            <sequence>%s</sequence>\n", encodeForXml(author.getSequence())));
                xmlBuilder.append(String.format("            <given>%s</given>\n", encodeForXml(author.getGiven())));
                xmlBuilder.append(String.format("            <family>%s</family>\n", encodeForXml(author.getFamily())));
                xmlBuilder.append(String.format("            <suffix>%s</suffix>\n", encodeForXml(author.getSuffix())));
                xmlBuilder.append(String.format("            <orcid>%s</orcid>\n", encodeForXml(author.getOrcidUrl())));
                xmlBuilder.append("        </author>\n");
            }
        }
        xmlBuilder.append("    </articleAuthors>\n");
        xmlBuilder.append("</journalCitation>\n");
        return xmlBuilder.toString();
    }

    String encodeForXml(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        return Encode.forXml(s);
    }

    String encodeForXml(Integer i) {
        return encodeForXml(i == null ? "" : i.toString());
    }

    public static int sequenceToInt(String sequenceStr) {
        if (Objects.equals(sequenceStr, "first")) {
            return 0;
        }
        if (Objects.equals(sequenceStr, "additional")) {
            return 1;
        }
        return 2;
    }

    // Elsevir – Standard numbered style
    // https://booksite.elsevier.com/9780081019375/content/Elsevier%20Standard%20Reference%20Styles.pdf
    //
    // articleUrl
    // articleTitle
    // articleDoi
    // journalTitle
    // journalPubYear
    // shortAuthorList
    // journalIssue
    // journalVolume
    // articlePages
    // packageId

    // Multiple Sources and Forms of Nitrogen Sustain Year-Round Kelp Growth on the Inner Continental Shelf of the Santa
    // Barbara Channel (10.5670/oceanog.2013.53), Oceanography (edi.36.16)


    public String toHTML() {
        StringBuilder sb = new StringBuilder();
        String shortAuthorList = getShortArticleAuthorList();
        if (shortAuthorList != null && !shortAuthorList.isEmpty()) {
            sb.append(String.format("%s, ", shortAuthorList));
        }
        String articleUrl = getArticleUrl();
        String articleTitle = getArticleTitle();
        if (articleUrl != null) {
            if (articleTitle != null && !articleTitle.isEmpty()) {
                sb.append(String.format("<a class='searchsubcat' href='%s'>%s</a>", articleUrl, articleTitle));
            }
            else {
                sb.append(String.format("<a class='searchsubcat' href='%s'>%s</a>", articleUrl, articleUrl));
            }
        }
        else {
            if (articleTitle != null && !articleTitle.isEmpty()) {
                sb.append(String.format("%s", articleTitle));
            }
        }
        String articleDoi = getArticleDoi();
        if (articleDoi != null && !articleDoi.isEmpty()) {
            sb.append(String.format(" (%s)", articleDoi));
        }
        String journalTitle = getJournalTitle();
        if (journalTitle != null && !journalTitle.isEmpty()) {
            sb.append(String.format(", %s", journalTitle));
        }
        String journalIssue = getJournalIssue();
        if (journalIssue != null && !journalIssue.isEmpty()) {
            sb.append(String.format(", %s", journalIssue));
        }
        String journalVolume = getJournalVolume();
        if (journalVolume != null && !journalVolume.isEmpty()) {
            sb.append(String.format(" %s", journalVolume));
        }
        String articlePages = getArticlePages();
        if (articlePages != null && !articlePages.isEmpty()) {
            sb.append(String.format(" %s", articlePages));
        }
        Integer journalPubYear = getJournalPubYear();
        if (journalPubYear != null) {
            sb.append(String.format(" %d", journalPubYear));
        }
        String packageId = getPackageId();
        sb.append(String.format(" <em>(%s)</em>", packageId));

        return sb.toString();
    }


    private String getDateCreatedStr() {
        String dateCreatedStr = "";
        if (this.dateCreated != null) {
            dateCreatedStr = dateCreated.toString();
        }

        return dateCreatedStr;
    }


    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        json.put("journalCitationId", this.journalCitationId);
        json.put("packageId", this.packageId);
        json.put("principalOwner", this.principalOwner);
        json.put("dateCreated", getDateCreatedStr());
        json.put("articleDoi", jsonNull(this.articleDoi));
        json.put("articleTitle", jsonNull(this.articleTitle));
        json.put("articleUrl", jsonNull(this.articleUrl));
        json.put("journalTitle", jsonNull(this.journalTitle));
        json.put("relationType", jsonNull(this.relationType));
        json.put("journalPubYear", jsonNull(this.journalPubYear));
        json.put("journalIssue", jsonNull(this.journalIssue));
        json.put("journalVolume", jsonNull(this.journalVolume));
        json.put("articlePages", jsonNull(this.articlePages));

        JSONArray authorJsonArray = new JSONArray();

        for (ArticleAuthor author : this.articleAuthorList) {
            JSONObject authorJson = new JSONObject();
            authorJson.put("sequence", author.getSequence());
            authorJson.put("given", jsonNull(author.getGiven()));
            authorJson.put("family", jsonNull(author.getFamily()));
            authorJson.put("suffix", jsonNull(author.getSuffix()));
            authorJson.put("orcid", jsonNull(author.getOrcidUrl()));
            authorJsonArray.put(authorJson);
        }

        json.put("articleAuthorList", authorJsonArray);
        json.put("shortArticleAuthorList", getShortArticleAuthorList());

        return json;
    }

    // The JSON library we're using is poorly implemented, and one of the problems it has is that setting a key as
    // Java's native null causes the key to be removed altogether. By translating native null to JSONObject.NULL, we
    // preserve null value keys.
    private static Object jsonNull(String s) {
        return s == null || s.isEmpty() ? JSONObject.NULL : s;
    }
    private static Object jsonNull(Integer s) {
        return s == null ? JSONObject.NULL : s;
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
        String url = null;

        if (this.articleUrl != null && !this.articleUrl.isEmpty()) {
            url = articleUrl;
        }
        else {
            url = deriveUrlFromDoi();
        }

        return url;
    }

    private String deriveUrlFromDoi() {
        String url = null;

        if (this.articleDoi != null) {
            if (this.articleDoi.startsWith("http")) {
                url = articleDoi;
            }
            else {
                url = String.format("https://doi.org/%s", this.articleDoi);
            }
        }

        return url;
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

    public String getRelationType() { return relationType; }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public Integer getJournalPubYear() {
        return this.journalPubYear;
    }

    public void setJournalPubYear(Integer journalPubYear) {
        this.journalPubYear = journalPubYear;
    }

    /**
     * Set journalPubYear from pubDate String in YYYY-MM-DD or YYYY format
     */
    public void setJournalPubYear(String pubDate) {
        if (pubDate == null || pubDate.isEmpty()) {
            this.journalPubYear = null;
            return;
        }

        Pattern pubYearMonthDayPattern = Pattern.compile("^(\\d{4})-\\d{2}-\\d{2}$");
        Matcher pubYearMonthDayMatcher = pubYearMonthDayPattern.matcher(pubDate);
        if (pubYearMonthDayMatcher.matches()) {
            this.journalPubYear = Integer.valueOf(pubYearMonthDayMatcher.group(1));
            return;
        }

        Pattern pubYearPattern = Pattern.compile("^(\\d{4})$");
        Matcher pubYearMatcher = pubYearPattern.matcher(pubDate);
        if (pubYearMatcher.matches()) {
            this.journalPubYear = Integer.valueOf(pubYearMatcher.group(1));
            return;
        }

        String errorMsg = String.format("Error extracting year from PubDate: %s", pubDate);
        logger.error(errorMsg);
        throw new RuntimeException(errorMsg);
    }

    // Article authors

    public void setArticleAuthorList(JSONArray authorList) {
        this.articleAuthorList.clear();
        for (int i = 0; i < authorList.length(); i++) {
            JSONObject authorJson = authorList.getJSONObject(i);
            ArticleAuthor author = new ArticleAuthor(
                authorJson.getInt("sequence"),
                authorJson.optString("given"),
                authorJson.optString("family"),
                authorJson.optString("suffix"),
                authorJson.optString("orcid")
            );
            this.articleAuthorList.add(author);
        }
    }

    public void clearArticleAuthorList() {
        this.articleAuthorList.clear();
    }

    public void addArticleAuthor(ArticleAuthor author) {
        this.articleAuthorList.add(author);
    }

    public ArrayList<ArticleAuthor> getArticleAuthorList() {
        return this.articleAuthorList;
    }

    public String getShortArticleAuthorList() {
        List<String> shortAuthorList = new ArrayList<>();

        if (this.articleAuthorList == null || this.articleAuthorList.isEmpty()) {
            return null;
        }

        for (ArticleAuthor author : this.articleAuthorList) {
            String given = author.getGiven();
            String family = author.getFamily();

            String authorStr = "";
            if (given != null && !given.isEmpty()) {
                authorStr += givenNameToInitials(given);
            }
            if (family != null && !family.isEmpty()) {
                authorStr += " " + family;
            }
            if (!authorStr.isEmpty()) {
                shortAuthorList.add(authorStr);
            }
        }

        int nAuthors = shortAuthorList.size();

        if (nAuthors == 1) {
            return shortAuthorList.get(0);
        } else {
            String joined = String.join(", ", shortAuthorList.subList(0, nAuthors - 1));
            return joined + " & " + shortAuthorList.get(nAuthors - 1);
        }
    }

    public static String givenNameToInitials(String givenName) {
        if (givenName == null) {
            return null;
        }
        String[] parts = givenName.split(" ");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(part.charAt(0)).append(".");
            }
        }
        return result.toString();
    }

    public String getJournalIssue() {
        return journalIssue;
    }

    public String getJournalVolume() {
        return journalVolume;
    }

    public String getArticlePages() {
        return articlePages;
    }

    public void setJournalIssue(String journalIssue) {
        this.journalIssue = journalIssue;
    }

    public void setJournalVolume(String journalVolume) {
        this.journalVolume = journalVolume;
    }

    public void setArticlePages(String articlePages) {
        this.articlePages = articlePages;
    }

    public String toString() {
        return String.format("JournalCitation: id=%d, packageId=%s, articleDoi=%s, articleUrl=%s, articleTitle=%s, " +
                "journalTitle=%s, relationType=%s, journalPubYear=%d, shortArticleAuthorList=%s, journalIssue=%s, " +
                "journalVolume=%s, articlePages=%s",
                this.journalCitationId, this.packageId, this.articleDoi, this.articleUrl, this.articleTitle,
                this.journalTitle, this.relationType, this.journalPubYear, getShortArticleAuthorList(),
                this.journalIssue, this.journalVolume, this.articlePages
            );
    }
}
