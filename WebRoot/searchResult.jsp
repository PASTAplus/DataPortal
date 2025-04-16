<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet" %>
<%@ page import="edu.lternet.pasta.portal.search.LTERTerms" %>

<%
  final String pageTitle = "Search Results";
  final String titleText = DataPortalServlet.getTitleText(pageTitle);
  String path = request.getContextPath();
  String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";

  String termsListHTML = (String) session.getAttribute("termsListHTML");
  if (termsListHTML == null) {
    termsListHTML = "";
  }

  String queryText = (String) session.getAttribute("queryText");
  String queryURL = "";
  int textBoxSize = 100;
  String textBoxSizeStr = "100";
  if (!queryText.isEmpty()) {
    queryURL = String.format("%ssimpleSearch?%s", basePath, queryText);
    textBoxSize = Math.min(textBoxSize, queryURL.length());
    textBoxSizeStr = String.format("%d", textBoxSize);
  }

  String mapButtonHTML = (String) request.getAttribute("mapButtonHTML");
  if (mapButtonHTML == null) {
    mapButtonHTML = "";
  }

  String relevanceHTML = (String) request.getAttribute("relevanceHTML");
  if (relevanceHTML == null) {
    relevanceHTML = "";
  }

  String searchResult = (String) request.getAttribute("searchResult");

  if (searchResult == null) {
    searchResult = "";
  }

  //String jqueryString = LTERTerms.getJQueryString(); // for auto-complete using JQuery
%>

<!DOCTYPE html>
<html lang="en">

<head>
  <title><%= titleText %>
  </title>

  <meta charset="UTF-8"/>
  <meta content="width=device-width, initial-scale=1, maximum-scale=1" name="viewport">

  <link rel="shortcut icon" href="./images/favicon.ico" type="image/x-icon"/>

  <!-- Google Fonts CSS -->
  <link href="https://fonts.googleapis.com/css?family=Open+Sans:400,300,600,300italic" rel="stylesheet" type="text/css">

  <!-- Page Layout CSS MUST LOAD BEFORE bootstap.css -->
  <link href="css/style_slate.css" media="all" rel="stylesheet" type="text/css">

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
  <script src="js/clipboard.min.js"></script>

  <!-- Mobile Device CSS -->
  <link href="bootstrap/css/bootstrap.css" media="screen" rel="stylesheet" type="text/css">
  <link href="bootstrap/css/bootstrap-responsive.css" media="screen" rel="stylesheet" type="text/css">

</head>

<body>


<jsp:include page="header.jsp"/>

<style>
    .pasta-link {
        cursor: pointer;
        color: #5990bd;
    }
    .pasta-link:hover {
        text-decoration: underline;
    }
</style>

<script>
  var clipboard = new Clipboard('.btn');

  clipboard.on('success', function (e) {
    console.log(e);
  });

  clipboard.on('error', function (e) {
    console.log(e);
  });

  document.addEventListener('click', function (ev) {
    const linkEl = ev.target.closest('.pasta-link');
    if (linkEl) {
      window.location.href = "./mapbrowse?packageid=" + linkEl.dataset.packageId;
    }
  });
</script>

<div class="row-fluid ">
  <div class="container">
    <div class="row-fluid distance_1">
      <div class="box_shadow box_layout">
        <div class="row-fluid">
          <div class="span12">
            <div class="recent_title">
              <h2>Search Results</h2>
            </div>
            <span class="row-fluid separator_border"></span>
          </div>

          <div class="row-fluid">
            <div class="span12">
              <!-- Content -->

              <table>
                <tr>
                  <td>
                    <%=mapButtonHTML%>
                  </td>
                </tr>
                <tr>
                  <td>
                    <%=relevanceHTML%>
                  </td>
                </tr>
              </table>

              <table>
                <tr>
                  <td style="padding-top: 1rem">
                    <label for="queryURLButton">Query URL</label>
                    <button type="button" id="queryURLButton" class="btn btn-info btn-default" data-clipboard-action="copy" data-clipboard-target="#queryURL">Copy Query URL</button>
                  </td>
                </tr>
                <tr>
                  <td style="padding-top: .5rem">
                    <input id="queryURL" type="url" value="<%=queryURL%>" size="<%=textBoxSizeStr%>">
                  </td>
                </tr>
              </table>

              <table>
                <tr>
                  <td>
                    <form action="./downloadSearch">
                      <label for="downloadSearchButton">Download the results from this search as a CSV file</label>
                      <button id="downloadSearchButton" type="submit" name="q" class="btn btn-info btn-default" value="<%=queryText%>">Download Search Results As CSV</button>
                    </form>
                  </td>
                </tr>
              </table>

              <%=termsListHTML%>
              <%=searchResult%>

            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <jsp:include page="footer.jsp"/>
</div>

</body>

</html>
