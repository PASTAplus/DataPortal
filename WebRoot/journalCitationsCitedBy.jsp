<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet" %>
<%@ page import="edu.lternet.pasta.client.JournalCitationsClient" %>
<%--<%@ page import="edu.lternet.pasta.portal.LoginServlet" %>--%>
<%--<%@ page import="edu.lternet.pasta.portal.Tooltip" %>--%>
<%@ page import="edu.lternet.pasta.client.PastaAuthenticationException" %>
<%@ page import="edu.lternet.pasta.client.PastaConfigurationException" %>
<%--<%@ page import="org.apache.commons.io.IOUtils" %>--%>
<%--<%@ page import="javax.xml.parsers.DocumentBuilder" %>--%>
<%--<%@ page import="javax.xml.parsers.DocumentBuilderFactory" %>--%>
<%--<%@ page import="org.apache.xpath.CachedXPathAPI" %>--%>
<%@ page import="org.w3c.dom.Document" %>
<%@ page import="javax.xml.parsers.ParserConfigurationException" %>
<%@ page import="edu.lternet.pasta.portal.JournalCitationsUtil" %>
<%@ page import="org.xml.sax.SAXException" %>
<%@ page import="org.w3c.dom.NodeList" %>
<%@ page import="org.w3c.dom.Element" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%
    final String pageTitle = "Data Packages Cited by Journal Article";
    final String titleText = DataPortalServlet.getTitleText(pageTitle);

    String packageIdXml;
    JournalCitationsClient jcc;

    // http://localhost:8080/nis/journalCitationsCitedBy?journalDoi=10.1111/gcb.15867

    String journalDoi = request.getParameter("journalDoi");
    if (journalDoi == null) {
        throw new IllegalArgumentException("journalDoi query parameter is required");
    }

    // Remove URL wrapping from DOI if present
    journalDoi = journalDoi.replaceFirst("^https?://doi.org/", "");

    try {
        jcc = new JournalCitationsClient("public");
    } catch (PastaAuthenticationException | PastaConfigurationException e) {
        throw new RuntimeException(e);
    }

    try {
        packageIdXml = jcc.listDataPackagesCitedBy(journalDoi);
    } catch (Exception e) {
        throw new RuntimeException(e);
    }

    Document doc;
    try {
        doc = JournalCitationsUtil.parseXML(packageIdXml);
    } catch (ParserConfigurationException | SAXException e) {
        throw new RuntimeException(e);
    }
%>


<!DOCTYPE html>
<html lang="en">

<head>
    <title><%= titleText %>
    </title>
    <meta charset="UTF-8"/>

    <!-- CSS -->
    <link rel="shortcut icon" href="./images/favicon.ico" type="image/x-icon"/>
    <!-- Google Fonts CSS -->
    <link href="https://fonts.googleapis.com/css?family=Open+Sans:400,300,600,300italic" rel="stylesheet"
          type="text/css">
    <!-- Page Layout CSS MUST LOAD BEFORE bootstap.css -->
    <link href="css/style_slate.css" media="all" rel="stylesheet" type="text/css">
    <!-- Mobile Device CSS -->
    <link href="bootstrap/css/bootstrap-responsive.css" rel="stylesheet" type="text/css">
    <link href="bootstrap/css/bootstrap.css" rel="stylesheet" type="text/css">
    <!-- PASTA style tweaks, should be after all other Bootstrap CSS -->
    <link href="./css/pasta-bootstrap-tweaks.css" rel="stylesheet" type="text/css">
    <!-- DataTables -->
    <link href="./DataTables/datatables.min.css" rel="stylesheet" type="text/css"/>

    <!-- JS -->
    <script src="js/jquery-3.6.3.min.js"></script>
    <!-- <script src="js/jqueryba3a.js?ver=1.7.2" type="text/javascript"></script> -->
    <script src="bootstrap/js/bootstrap68b368b3.js?ver=1" type="text/javascript"></script>
    <script src="js/jquery.easing.1.368b368b3.js?ver=1" type="text/javascript"></script>
    <script src="js/jquery.flexslider-min68b368b3.js?ver=1" type="text/javascript"></script>
    <!-- <script src="js/themeple68b368b3.js?ver=1" type="text/javascript"></script> -->
    <script src="js/jquery.pixel68b368b3.js?ver=1" type="text/javascript"></script>
    <script src="js/jquery.mobilemenu68b368b3.js?ver=1" type="text/javascript"></script>
    <script src="js/mediaelement-and-player.min68b368b3.js?ver=1" type="text/javascript"></script>
    <!-- DataTables, depends on jQuery  -->
    <script src="./DataTables/datatables.min.js" type="text/javascript"></script>
    <!-- script for this page -->
    <script src="js/journal-citations-cited-by.js" type="text/javascript"></script>
</head>

<body>

<jsp:include page="header.jsp"/>

<div class="container">
    <div class="row distance_1">
        <div class="col">

            <%@ include file="statusNotices.jsp" %>

            <div class="recent_title">
                <h1><%= pageTitle %>
                </h1>
            </div>
            <span class="separator_border"></span>
        </div>
    </div>

    <div class="row distance_2">
        <div class="col">
            <h2>Journal Article DOI: <b><%= journalDoi %>
            </b>
            </h2>
        </div>
    </div>

    <div class="row distance_2">
        <div class="col">
            <table id="cited-by-table">
                <thead>
                <tr>
                    <th class="nis">Journal Citation ID</th>
                    <th class="nis">Package ID</th>
                    <th class="nis">Title</th>
                    <th class="nis">Creators</th>
                    <th class="nis">Publication Date</th>
                </tr>
                </thead>
                <tbody>
                <%
                    NodeList citationNodes = doc.getElementsByTagName("journalCitation");
                    for (int i = 0; i < citationNodes.getLength(); i++) {
                        Element citationEl = (Element) citationNodes.item(i);
                        String articleCitationId = citationEl.getElementsByTagName("articleCitationId").item(0).getTextContent();
                        String packageId = citationEl.getElementsByTagName("packageId").item(0).getTextContent();
                        Map<String, Object> metaMap = JournalCitationsUtil.getMetaFromRidare(packageId);
                %>
                <tr>
                    <td class="nis"><c:out value="<%= articleCitationId %>"/></td>
                    <td class="nis">
                        <a href="mapbrowse?packageid=<c:out value='<%= packageId %>'/>" class="searchsubcat">
                            <c:out value='<%= packageId %>'/>
                        </a>
                    </td>
                    <td class="nis"><c:out value='<%= metaMap.get("title") %>'/></td>
                    <td class="nis"><c:out value='<%= String.join("<br>", (List<String>) metaMap.get("creators")) %>'/></td>
                    <td class="nis"><c:out value='<%= metaMap.get("pubDate") %>'/></td>
                </tr>
                <%
                    }
                %></tbody>
            </table>
        </div>
    </div>

    <%--    <jsp:include page="footer.jsp"/>--%>

</body>

</html>
