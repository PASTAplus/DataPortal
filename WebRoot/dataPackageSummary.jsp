<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="edu.lternet.pasta.portal.ConfigurationListener" %>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet" %>
<%@ page import="edu.lternet.pasta.client.DataPackageManagerClient" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%
  final String googleMapsKey = (String) ConfigurationListener.getOptions().getProperty("maps.google.key");
  final String pageTitle = "Data Package Summary";
  final String titleText = DataPortalServlet.getTitleText(pageTitle);

  String wasDeletedHTML = (String) request.getAttribute("wasDeletedHTML");
  String titleHTML = (String) request.getAttribute("dataPackageTitleHTML");
  String viewFullMetadataHTML = (String) request.getAttribute("viewFullMetadataHTML");
  String moreRecentRevisionHTML = (String) request.getAttribute("moreRecentRevisionHTML");
  String creatorsHTML = (String) request.getAttribute("dataPackageCreatorsHTML");
  String abstractHTML = (String) request.getAttribute("abstractHTML");
  String intellectualRightsHTML = (String) request.getAttribute("intellectualRightsHTML");
  String licensedHTML = (String) request.getAttribute("licensedHTML");
  String publicationDateHTML = (String) request.getAttribute("dataPackagePublicationDateHTML");
  String packageIdHTML = (String) request.getAttribute("dataPackageIdHTML");
  String resourcesHTML = (String) request.getAttribute("dataPackageResourcesHTML");
  String citationHTML = (String) request.getAttribute("citationHTML");
  String citationLinkHTML = (String) request.getAttribute("citationLinkHTML");
  String digitalObjectIdentifier = (String) request.getAttribute("digitalObjectIdentifier");
  String dataCiteDOI = (String) request.getAttribute("dataCiteDOI");
  String pastaDataObjectIdentifier = (String) request.getAttribute("pastaDataObjectIdentifier");
  String provenanceHTML = (String) request.getAttribute("provenanceHTML");
  String journalCitationsHTML = (String) request.getAttribute("journalCitationsHTML");
  String codeGenerationHTML = (String) request.getAttribute("codeGenerationHTML");
  String spatialCoverageHTML = (String) request.getAttribute("spatialCoverageHTML");
  String googleMapHTML = (String) request.getAttribute("googleMapHTML");
  String savedDataHTML = (String) request.getAttribute("savedDataHTML");
  String seoHTML = (String) request.getAttribute("seoHTML");
  String jsonCoordinates = (String) request.getAttribute("jsonCoordinates");
  Boolean expandCoordinates = (Boolean) request.getAttribute("expandCoordinates");
  Double northCoord = (Double) request.getAttribute("northCoord");
  Double southCoord = (Double) request.getAttribute("southCoord");
  Double eastCoord = (Double) request.getAttribute("eastCoord");
  Double westCoord = (Double) request.getAttribute("westCoord");

  String uid = (String) session.getAttribute("uid");
  boolean showAbstract = !(abstractHTML == null || abstractHTML.isEmpty());
  boolean showIntellectualRights = !(intellectualRightsHTML == null || intellectualRightsHTML.isEmpty());
  boolean showLicensed = !(licensedHTML == null || licensedHTML.isEmpty());
  boolean showPubDate = !(publicationDateHTML == null || publicationDateHTML.isEmpty());
  boolean showSpatial = !(spatialCoverageHTML == null || spatialCoverageHTML.isEmpty());
  boolean showCodeGeneration = !(codeGenerationHTML == null || codeGenerationHTML.isEmpty());
  boolean showSavedData = !(savedDataHTML == null || savedDataHTML.isEmpty());
  boolean showJournalCitations = !(journalCitationsHTML == null || journalCitationsHTML.isEmpty());
  boolean showSEO = !(seoHTML == null || seoHTML.isEmpty());

  String showCoordinates = "true";
  if ((expandCoordinates != null) && !expandCoordinates) {
    showCoordinates = "false";
  }
  String showWasDeleted = "true";
  if ((wasDeletedHTML == null) || (wasDeletedHTML.equals(""))) {
    showWasDeleted = "false";
  }

  HttpSession httpSession = request.getSession();
  if ((uid == null) || (uid.equals(""))) {
    uid = "public";
  }

  String tier = null;
  String testHTML = "";
  String showTestHTML = "false";
  DataPackageManagerClient dpmc = new DataPackageManagerClient(uid);
  String pastaHost = dpmc.getPastaHost();

  if (pastaHost.startsWith("pasta-d") || pastaHost.startsWith("localhost")) {
    tier = "development";
  }
  else if (pastaHost.startsWith("pasta-s")) {
    tier = "staging";
  }
  else {
    tier = "production";
  }

  if (!tier.equals("production")) {
    showTestHTML = "true";
    String fontColor = "darkorange";
    testHTML = String.format(
        "<font color='%s'>This data package was submitted to a %s environment for testing " + "purposes only. Use of these data for anything other than testing is strongly discouraged." + "</font>",
        fontColor, tier);
  }

  String showNewestRevision = "false";
  String newestRevisionHTML = "";
  if (moreRecentRevisionHTML != "") {
    showNewestRevision = "true";
    String fontColor = "darkorange";
    newestRevisionHTML = String.format("<font color='%s'>This data package is not the most recent revision " + "of a series. %s</font>", fontColor, moreRecentRevisionHTML);
  }

  StringBuffer googleAnalyticsScriptBuffer = new StringBuffer("");

  //tier = "localtest";  // Uncomment this for local testing when not on production

// Google Analytics began causing extreme latency with new version on 1 July 2023 so comment out until resolved
  // We want the Google Analytics script on the production or localhost testing tiers only
//  if (tier.equals("production") || pastaHost.startsWith("localhost")) {
//    googleAnalyticsScriptBuffer.append("<!-- Global site tag (gtag.js) - Google Analytics -->\n");
//    googleAnalyticsScriptBuffer.append("<script async src=\"https://www.googletagmanager.com/gtag/js?id=G-YBTPJW4NGB\"></script>\n");
//    googleAnalyticsScriptBuffer.append("<script>\n");
//    googleAnalyticsScriptBuffer.append("window.dataLayer = window.dataLayer || [];\n");
//    googleAnalyticsScriptBuffer.append("function gtag(){dataLayer.push(arguments);}\n");
//    googleAnalyticsScriptBuffer.append("gtag('js', new Date());\n");
//    googleAnalyticsScriptBuffer.append("gtag('config', 'G-YBTPJW4NGB');\n");
//    googleAnalyticsScriptBuffer.append("</script>\n");
//  }

  String googleAnalyticsScript = googleAnalyticsScriptBuffer.toString();

%>

<!DOCTYPE html>
<html lang="en">

<head>

  <%= googleAnalyticsScript %>

  <title><%= titleText %>
  </title>

  <meta charset="UTF-8"/>
  <meta content="width=device-width, initial-scale=1, maximum-scale=1" name="viewport">

  <link rel="shortcut icon" href="./images/favicon.ico" type="image/x-icon"/>

  <!-- Google Fonts CSS -->
  <link href="https://fonts.googleapis.com/css?family=Open+Sans:400,300,600,300italic" rel="stylesheet" type="text/css">

  <!-- jqWidgets CSS for jqxTree widget -->
  <link rel="stylesheet" href="./js/jqwidgets/styles/jqx.base.css" type="text/css"/>
  <link rel="stylesheet" href="./js/jqwidgets/styles/jqx.bootstrap.css" type="text/css"/>
  <link rel="stylesheet" href="./js/jqwidgets/styles/jqx.energyblue.css" type="text/css"/>

  <!-- Page Layout CSS MUST LOAD BEFORE bootstap.css -->
  <link href="css/style_slate.css" media="all" rel="stylesheet" type="text/css">

  <!-- Mobile Device CSS -->
  <link href="bootstrap/css/bootstrap.css" media="screen" rel="stylesheet" type="text/css">
  <link href="bootstrap/css/bootstrap-responsive.css" media="screen" rel="stylesheet" type="text/css">


  <link rel="stylesheet" href="./css/more.css" type="text/css"/>


  <!-- JS
  <script src="js/jqueryba3a.js?ver=1.7.2" type="text/javascript"></script>
  <script src="bootstrap/js/bootstrap68b368b3.js?ver=1" type="text/javascript"></script>
  <script src="js/jquery.easing.1.368b368b3.js?ver=1" type="text/javascript"></script>
  <script src="js/jquery.flexslider-min68b368b3.js?ver=1" type="text/javascript"></script>
  <script src="js/themeple68b368b3.js?ver=1" type="text/javascript"></script>
  <script src="js/jquery.pixel68b368b3.js?ver=1" type="text/javascript"></script>
  <script src="js/jquery.mobilemenu68b368b3.js?ver=1" type="text/javascript"></script>
  <script src="js/isotope68b368b3.js?ver=1" type="text/javascript"></script>
  <script src="js/mediaelement-and-player.min68b368b3.js?ver=1" type="text/javascript"></script>-->
  <script src="js/jquery-1.11.0.min.js" type="text/javascript"></script>
  <script src="js/data-shelf-ajax.js" type="text/javascript"></script>

  <script src="https://maps.googleapis.com/maps/api/js?key=<%= googleMapsKey %>" type="text/javascript"></script>
  <script src="./js/map_functions.js" type="text/javascript"></script>
  <script type="text/javascript" src="https://google-maps-utility-library-v3.googlecode.com/svn/trunk/keydragzoom/src/keydragzoom.js" type="text/javascript"></script>

  <c:set var="showSpatial" value="<%= showSpatial %>"/>
  <c:choose>
    <c:when test="${showSpatial}">
      <script type="text/javascript">
      window.onload = function () {
        var coordinatesArray = <%= jsonCoordinates %>;
        var north = <%= northCoord %>;
        var south = <%= southCoord %>;
        var east = <%= eastCoord %>;
        var west = <%= westCoord %>;
        initialize_summary_map(coordinatesArray, north, south, east, west);
      };
      </script>
    </c:when>
  </c:choose>

  <c:set var="showSEO" value="<%= showSEO %>"/>
  <c:choose>
    <c:when test="${showSEO}">
      <%= seoHTML %>
    </c:when>
  </c:choose>

  <% if (!tier.equals("production")) { %>
  <style>
      .watermark {
          background-image: url(/nis/images/watermark.png);
      }
  </style>
  <% } %>

  <style>
    .inline-svg {
      height: 1em;
      vertical-align: middle;
    }
  </style>

  <script>
  document.addEventListener('click', function (ev) {
    const linkEl = ev.target.closest('.pasta-link');
    if (linkEl) {
      window.open(linkEl.dataset.dexBaseUrl + "/" + linkEl.dataset.entityUrl, "_blank");
    }
  });
  </script>

</head>

<body>

<jsp:include page="header.jsp"/>

<div class="row-fluid ">
  <div>
    <div class="container">
      <div class="row-fluid distance_1">
        <div class="box_shadow box_layout">
          <div class="row-fluid">
            <div class="span12">

              <%@ include file="statusNotices.jsp" %>

              <c:set var="showTestHTML" value="<%= showTestHTML %>"/>
              <c:choose>
                <c:when test="${showTestHTML}">
                  <div>
                    <h2><%= testHTML %>
                    </h2>
                  </div>
                </c:when>
              </c:choose>

              <c:set var="showNewestRevision" value="<%= showNewestRevision %>"/>
              <c:choose>
                <c:when test="${showNewestRevision}">
                  <div>
                    <h2><%= newestRevisionHTML %>
                    </h2>
                  </div>
                </c:when>
              </c:choose>

              <c:set var="showWasDeleted" value="<%= showWasDeleted %>"/>
              <c:choose>
                <c:when test="${showWasDeleted}">
                  <span class="nis-banner-msg">&nbsp;&nbsp;<%= wasDeletedHTML %>&nbsp;&nbsp;</span>
                  <span class="row-fluid separator_border"></span>
                </c:when>
              </c:choose>

              <div class="recent_title">
                <h1>Data Package Summary&nbsp;&nbsp;&nbsp;
                  <small><small>
                    <%= viewFullMetadataHTML %>
                  </small></small>
                </h1>
              </div>

              <span class="row-fluid separator_border"></span>
            </div>

            <div class="row-fluid">
              <div class="span12">
                <div class="display-table">
                  <div class="watermark">
                    <div class="table-row">
                      <div class="table-cell text-align-right">
                        <label class="labelBold">Title:</label>
                      </div>
                      <div class="table-cell">
                        <%= titleHTML %>
                      </div>
                    </div>

                    <div class="table-row">
                      <div class="table-cell text-align-right">
                        <label class="labelBold">Creators:</label>
                      </div>
                      <div class="table-cell">
                        <ul class="no-list-style">
                          <li>
                            <div class="pasta-more-text">
                              <div class="pasta-more-content">
                                <div>
                                  <%= creatorsHTML %>
                                </div>
                              </div>
                              <div class="pasta-more">
                              </div>
                            </div>
                          </li>
                        </ul>
                      </div>
                    </div>

                    <c:set var="showDate" value="<%= showPubDate %>"/>
                    <c:choose>
                      <c:when test="${showDate}">
                        <div class="table-row">
                          <div class="table-cell text-align-right">
                            <label class="labelBold">Publication Date:</label>
                          </div>
                          <div class="table-cell">
                            <%= publicationDateHTML %>
                          </div>
                        </div>
                      </c:when>
                    </c:choose>

                    <div class="table-row">
                      <div class="table-cell text-align-right">
                        <label class="labelBold">Citation:</label>
                      </div>
                      <div class="table-cell">
                        <ul class="no-list-style">
                          <li>
                            <%= citationHTML %>
                          </li>
                        </ul>
                        <ul class="no-list-style">
                          <li>
                            <button class="btn btn-info btn-default" onclick="copyCitation()">Copy Citation</button>
                          </li>
                        </ul>
                      </div>
                    </div>

                    <c:set var="showAbstract" value="<%= showAbstract %>"/>
                    <c:choose>
                      <c:when test="${showAbstract}">
                        <div class="table-row">
                          <div class="table-cell text-align-right nis-summary-label">
                            <label class="labelBold">Abstract:</label>
                          </div>
                          <div class="table-cell">
                            <ul class="no-list-style">
                              <li>
                                <div class="pasta-more-text">
                                  <div class="pasta-more-content">
                                    <div>
                                      <%= abstractHTML %>
                                    </div>
                                  </div>
                                  <div class="pasta-more">
                                  </div>
                                </div>
                              </li>
                            </ul>
                          </div>
                        </div>
                      </c:when>
                    </c:choose>


                    <c:choose>
                      <c:when test="${showSpatial}">

                        <div class="table-row">

                          <div class="table-cell text-align-right nis-summary-label">
                            <label class="labelBold">Spatial Coverage:</label>
                          </div>

                          <div class="table-cell">
                            <%= googleMapHTML %>
                          </div>

                        </div>

                        <div class="table-row">

                          <div class="table-cell text-align-right">
                            <label class="labelBold"></label>
                          </div>

                          <div class="table-cell">
                            <ul class="no-list-style" style="margin-top:2px">
                              <li>
                                <%= spatialCoverageHTML %>
                              </li>
                            </ul>
                          </div>

                        </div>

                        <div class="table-row">
                          <div class="table-cell text-align-right">
                            <label class="labelBold"></label>
                          </div>
                          <div class="table-cell"></div>
                        </div>

                      </c:when>
                    </c:choose>

                    <div class="table-row">
                      <div class="table-cell text-align-right">
                        <label class="labelBold">Package ID:</label>
                      </div>
                      <div class="table-cell">
                        <%= packageIdHTML %>
                        <c:set var="showSavedData" value="<%= showSavedData %>"/>
                        <c:if test="${showSavedData}">
                          <%= savedDataHTML %>
                        </c:if>
                      </div>
                    </div>

                    <div class="table-row">
                      <div class="table-cell text-align-right">
                        <label class="labelBold">Resources:</label>
                      </div>
                      <div class="table-cell">
                        <%= resourcesHTML %>
                      </div>
                    </div>

                    <c:set var="showIntellectualRights" value="<%= showIntellectualRights %>"/>
                    <c:choose>
                      <c:when test="${showIntellectualRights}">
                        <div class="table-row">
                          <div class="table-cell text-align-right nis-summary-label">
                            <label class="labelBold">Intellectual Rights:</label>
                          </div>
                          <div class="table-cell">
                            <ul class="no-list-style">
                              <li>
                                <div class="pasta-more-text">
                                  <div class="pasta-more-content">
                                    <div>
                                      <%= intellectualRightsHTML %>
                                    </div>
                                  </div>
                                  <div class="pasta-more">
                                  </div>
                                </div>
                              </li>
                            </ul>
                          </div>
                        </div>
                      </c:when>
                    </c:choose>

                    <c:set var="showLicensed" value="<%= showLicensed %>"/>
                    <c:choose>
                      <c:when test="${showLicensed}">
                        <div class="table-row">
                          <div class="table-cell text-align-right nis-summary-label">
                            <label class="labelBold">Data License:</label>
                          </div>
                          <div class="table-cell">
                            <ul class="no-list-style">
                              <li>
                                <div class="more"><%= licensedHTML %>
                                </div>
                              </li>
                            </ul>
                          </div>
                        </div>
                      </c:when>
                    </c:choose>

                    <div class="table-row">
                      <div class="table-cell text-align-right">
                        <label class="labelBold">Digital Object Identifier:</label>
                      </div>
                      <div class="table-cell">
                        <ul class="no-list-style">
                          <li><%= digitalObjectIdentifier %>
                          </li>
                        </ul>
                      </div>
                    </div>

                    <!--
                    <div class="table-row">
                      <div class="table-cell text-align-right">
                        <label class="labelBold">PASTA Identifier:</label>
                      </div>
                      <div class="table-cell">
                        <ul class="no-list-style">
                          <li><%= pastaDataObjectIdentifier %>
                          </li>
                        </ul>
                      </div>
                    </div>
                    -->

                    <!--
                    <div class="table-row">
                      <div class="table-cell text-align-right">
                        <label class="labelBold">Citation:</label>
                      </div>
                      <div class="table-cell">
                        <ul class="no-list-style">
                          <li><%= citationLinkHTML %></li>
                        </ul>
                      </div>
                    </div>
                    -->

                    <c:set var="showCodeGeneration" value="<%= showCodeGeneration %>"/>
                    <c:if test="${showCodeGeneration}">
                      <div class="table-row">
                        <div class="table-cell text-align-right">
                          <label class="labelBold">Code Generation:</label>
                        </div>
                        <div class="table-cell">
                          <ul class="no-list-style">
                            <li><%= codeGenerationHTML %>
                            </li>
                          </ul>
                        </div>
                      </div>
                    </c:if>

                    <div class="table-row">
                      <div class="table-cell text-align-right">
                        <label class="labelBold">Provenance:</label>
                      </div>
                      <div class="table-cell">
                        <ul class="no-list-style">
                          <li>
                            <div class="pasta-more-text">
                              <div class="pasta-more-content">
                                <div>
                                  <%= provenanceHTML %>
                                </div>
                              </div>
                              <div class="pasta-more">
                              </div>
                            </div>
                          </li>
                        </ul>
                      </div>
                    </div>

                    <c:set var="showJournalCitations" value="<%= showJournalCitations %>"/>
                    <c:choose>
                      <c:when test="${showJournalCitations}">
                        <div class="table-row">
                          <div class="table-cell text-align-right">
                            <label class="labelBold">Journal Citations:</label>
                          </div>
                          <div class="table-cell">
                            <ul class="no-list-style">
                              <li><%= journalCitationsHTML %>
                              </li>
                            </ul>
                          </div>
                        </div>
                      </c:when>
                    </c:choose>

                  </div>
                </div> <!-- end display table -->
                <script src="https://unpkg.com/vue"></script>
                <script src="https://cdn.jsdelivr.net/npm/data-metrics-badge/dist/data-metrics-badge.min.js"></script>
                <data-metrics-badge doi="<%= dataCiteDOI %>"></data-metrics-badge>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>

<jsp:include page="footer.jsp"/>

<!-- jqWidgets JavaScript for jqxTree widget -->
<script type="text/javascript" src="./js/jqwidgets-ver3.2.1/jqxcore.js"></script>
<script type="text/javascript" src="./js/jqwidgets-ver3.2.1/jqxexpander.js"></script>
<script>
// Create jqxExpander
$("#jqxExpander").jqxExpander(
    {
      width: '454px',
      theme: 'bootstrap',
      expanded: <%= showCoordinates %>
    }
);
</script>
<!-- End jqWidgets JavaScript for jqxTree widget -->

<script src="./js/more.js" type="text/javascript"></script>

<script type="text/javascript">
function copyCitation()
{
  var copyText = document.getElementById("citation");
  var selection = window.getSelection();
  var range = document.createRange();
  range.selectNodeContents(copyText);
  selection.removeAllRanges();
  selection.addRange(range);
  document.execCommand("Copy");
}
</script>

</body>

</html>
