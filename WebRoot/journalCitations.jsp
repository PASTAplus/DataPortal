<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet" %>
<%@ page import="edu.lternet.pasta.client.JournalCitationsClient" %>
<%@ page import="edu.lternet.pasta.portal.LoginServlet" %>
<%@ page import="edu.lternet.pasta.portal.Tooltip" %>
<%@ page import="edu.lternet.pasta.client.PastaAuthenticationException" %>
<%@ page import="edu.lternet.pasta.client.PastaConfigurationException" %>

<%
  final String pageTitle = "Journal Citations";
  final String titleText = DataPortalServlet.getTitleText(pageTitle);
  String path = request.getContextPath();
  HttpSession httpSession = request.getSession();
  String journalCitationsHTML = "";
  String uname = "";
  String uid = (String) httpSession.getAttribute("uid");
  Boolean vetted = (Boolean) httpSession.getAttribute("vetted");
  if (vetted == null || !vetted) {
    String packageId = request.getParameter("packageid");
    String fromUrl = "./journalCitations.jsp";
    if (packageId != null) {
        fromUrl += "?packageid=" + packageId;
    }
    request.setAttribute("from", fromUrl);
    String loginWarning = DataPortalServlet.getLoginWarning();
    request.setAttribute("message", loginWarning);
    RequestDispatcher requestDispatcher = request.getRequestDispatcher("./login.jsp");
    requestDispatcher.forward(request, response);
  }
  else {
    uname = LoginServlet.uidFromDistinguishedName(uid);
      JournalCitationsClient jcc = null;
      try {
          jcc = new JournalCitationsClient(uid);
      } catch (PastaAuthenticationException e) {
          throw new RuntimeException(e);
      } catch (PastaConfigurationException e) {
          throw new RuntimeException(e);
      }
      try {
          journalCitationsHTML = jcc.citationsTableHTML();
      } catch (Exception e) {
          throw new RuntimeException(e);
      }
  }

  String packageId = request.getParameter("packageid");
  if (packageId == null) {
    packageId = "";
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
  <link href="https://fonts.googleapis.com/css?family=Open+Sans:400,300,600,300italic" rel="stylesheet" type="text/css">
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
  <%-- Couldn't get this working. --%>
  <%--<script src="./DataTables/dataTables.fixedColumns.min.js" type="text/javascript"></script>--%>
  <!-- script for this page -->
  <script src="js/journal-citations.js" type="text/javascript"></script>
</head>

<body>

<jsp:include page="header.jsp"/>

<!-- Make packageId available to the JS -->
<div class="parameters"
  data-package-id="<%= packageId %>"
>
</div>

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
      <h2>View and edit journal article citations recorded by <%= uname %>
      </h2>
    </div>
  </div>

  <div class="row distance_2">
    <div class="col">
      <p>
        Citations are displayed on the summary page for the data package.
      </p>
    </div>
  </div>

  <div class="row distance_2">
    <div class="col">
      <table id="citations-table">
        <thead>
        <tr>
          <th class="nis">Journal Citation ID</th>
          <th class="nis">Package ID</th>
          <th class="nis">Relation Type</th>
          <th class="nis">Journal Article DOI</th>
          <th class="nis">Journal Article URL</th>
          <th class="nis">Journal Article Title</th>
          <th class="nis">Journal Title</th>
          <th class="nis">Publication Date</th>
        </tr>
        </thead>
        <tbody>
        <%= journalCitationsHTML %>
        </tbody>
      </table>
    </div>
  </div>
</div>

<div class="row">
  <div class="col">
    <div class="container ">
      <ul class="instruction">
        <li>Click on a row to edit a citation.</li>
      </ul>
      <button id="new-button" type="button" class="btn btn-primary" data-bs-dismiss="modal">New Citation</button>
    </div>
  </div>
</div>


<jsp:include page="footer.jsp"/>

<!-- Modal dialog for creating and editing citations -->
<div class="modal fade" id="citations-modal" tabindex="-1" role="dialog" aria-labelledby="citations-modalLabel" aria-hidden="true">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="citations-modalLabel">Journal Citation
        </h5>
        <!-- Closing the modal by clicking the "X" is equivalent to clicking Cancel -->
        <button type="button" class="btn-close" data-dismiss="modal" aria-label="Close"></button>
      </div>
      <div class="modal-body">
        <form>
          <%-- Citation ID --%>
          <div class="control-group" hidden>
            <label for="citation-id" class="col-form-label">Citation ID:</label>
            <input class="form-control" id="citation-id" type="number" disabled="disabled">
          </div>
          <%-- Package ID --%>
          <div class="control-group">
            <label for="package-id" class="col-form-label">Package ID <em>(Required)</em></label>
            <input class="form-control" id="package-id" required="required" size="50" type="text"
                   value="<%= packageId %>"/>
          </div>
          <%-- Relation Type --%>
          <div class="control-group">
            <label for="relation-type" class="col-form-label">Relation Type</label>
            <select class="form-control" id="relation-type" style="width: 31em;">
              <option value="IsCitedBy">IsCitedBy - package is formally cited in the manuscript</option>
              <option value="IsDescribedBy">IsDescribedBy - package is explicitly described within the manuscript
              </option>
              <option value="IsReferencedBy">IsReferencedBy - package is implicitly described within the manuscript
              </option>
            </select>
          </div>
          <%-- Article DOI --%>
          <div class="control-group">
            <label for="article-doi" class="col-form-label">Article DOI <em>(Required unless Article URL is
              provided)</em></label>
            <div class="pasta-row">
              <input class="form-control" id="article-doi" size="50" type="text"/>
              <button id="fill-button" type="button" class="btn">Fill ↲</button>
            </div>
          </div>
          <%-- Article URL --%>
          <div class="control-group">
            <label for="article-url" class="col-form-label">Article URL <em>(Required unless Article DOI is
              provided)</em></label>
            <div class="pasta-row">
              <input class="form-control" id="article-url" size="50" type="url"/>
              <button id="open-button" type="button" class="btn">Open</button>
            </div>
          </div>
          <%-- Article Title --%>
          <div class="control-group">
            <label for="article-title" class="col-form-label">Article Title <em>(Optional)</em></label>
            <input class="form-control" id="article-title" size="50" type="text"/>
          </div>
          <%-- Journal Title --%>
          <div class="control-group">
            <label for="journal-title" class="col-form-label">Journal Title <em>(Optional)</em></label>
            <input class="form-control" id="journal-title" size="50" type="text"/>
          </div>
          <%-- Publication Date --%>
          <div class="control-group">
            <label for="journal-pub-year" class="col-form-label">Publication Date <em>(Year) (Optional)</em></label>
            <input type="number" class="form-control" id="journal-pub-year"/>
          </div>
          <%-- Authors--%>
          <div class="control-group" disabled="disabled">
            <label for="article-author-short" class="col-form-label" disabled="disabled">Authors <em>(fill from
              Crossref)</em></label>
            <input class="form-control" id="article-author-short" size="50" type="text" disabled/>
          </div>
          <%-- Journal Issue --%>
          <div class="control-group">
            <label for="journal-issue" class="col-form-label">Journal Issue</label>
            <input class="form-control" id="journal-issue" size="50" maxlength="32" type="text"/>
          </div>
          <%-- Journal Volume --%>
          <div class="control-group">
            <label for="journal-volume" class="col-form-label">Journal Volume</label>
            <input class="form-control" id="journal-volume" size="50" maxlength="32" type="text"/>
          </div>
          <%-- Article Pages --%>
          <div class="control-group">
            <label for="article-pages" class="col-form-label">Article Pages</label>
            <input class="form-control" id="article-pages" size="50" maxlength="32" type="text"/>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <div class="flex-left">
          <div>
            <button id="delete-button" type="button" class="btn btn-danger" data-dismiss="modal">Delete</button>
          </div>
        </div>
        <div class="flex-right">
          <div>
            <button id="cancel-button" type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
          </div>
          <div>
            <button id="ok-button" type="button" class="btn btn-primary" data-dismiss="modal">OK</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>

<!-- Spinner -->

<div class="modal fade" id="spinner-modal" tabindex="-1" role="dialog">
  <div class="spinner-border">
      <!-- <span class="sr-only">Loading...</span> -->
  </div>
</div>

</body>

</html>
