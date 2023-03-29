<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet" %>
<%@ page import="edu.lternet.pasta.client.JournalCitationsClient" %>
<%@ page import="edu.lternet.pasta.portal.LoginServlet" %>
<%@ page import="edu.lternet.pasta.portal.Tooltip" %>

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
    request.setAttribute("from", "./journalCitations.jsp");
    String loginWarning = DataPortalServlet.getLoginWarning();
    request.setAttribute("message", loginWarning);
    RequestDispatcher requestDispatcher = request.getRequestDispatcher("./login.jsp");
    requestDispatcher.forward(request, response);
  }
  else {
    uname = LoginServlet.uidFromDistinguishedName(uid);
    JournalCitationsClient jcc = new JournalCitationsClient(uid);
    journalCitationsHTML = jcc.citationsTableHTML();
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
  <!-- script for this page -->
  <script src="js/journal-citations.js" type="text/javascript"></script>
</head>

<body>

<jsp:include page="header.jsp"/>

<div class="container">
  <div class="row distance_1">
    <div class="col">
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

<script>
  let packageId = "<%= packageId %>";
</script>

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
          <div class="form-group" hidden>
            <!-- <div class="form-group"> -->
            <label for="citation-id" class="col-form-label">Citation ID:</label>
            <input class="form-control" id="citation-id" type="number" disabled="disabled">
          </div>
          <div class="form-group">
            <label for="package-id" class="col-form-label">Package ID <em>(Required)</em></label>
            <input class="form-control" id="package-id" required="required" size="50" type="text" value="<%= packageId %>"/>
          </div>
          <div class="form-group">
            <label for="relation-type" class="col-form-label">Relation Type <em>(See below)</em></label>
            <select class="form-control" id="relation-type">
              <option value="IsCitedBy">IsCitedBy</option>
              <option value="IsDescribedBy">IsDescribedBy</option>
              <option value="IsReferencedBy">IsReferencedBy</option>
            </select>
          </div>
          <div class="form-group">
            <label for="article-doi" class="col-form-label">Article DOI <span style="color:red;">*</span></label>
            <input class="form-control" id="article-doi" size="50" type="text"/>
          </div>
          <div class="form-group">
            <label for="article-url" class="col-form-label">Article URL <span style="color:red;">*</span></label>
            <input class="form-control" id="article-url" size="50" type="url"/>
          </div>
          <div class="form-group">
            <label for="article-title" class="col-form-label">Article Title <em>(Optional)</em></label>
            <input class="form-control" id="article-title" size="50" type="text"/>
          </div>
          <div class="form-group">
            <label for="journal-title" class="col-form-label">Journal Title <em>(Optional)</em></label>
            <input class="form-control" id="journal-title" size="50" type="text"/>
          </div>
        </form>
        <div>
          <span class="text-muted"><em><span style="color:red;">*</span> At least one of these fields are required</em></span>
        </div>
        <div>
          <br>
          The Relation Type describes the relationship between this data package and the journal manuscript:<br>
          <br>
          <b>IsCitedBy</b> - this data package is formally cited in the manuscript<br>
          <b>IsDescribedBy</b> - this data package is explicitly described within the manuscript<br>
          <b>IsReferencedBy</b> - this data package is implicitly described within the manuscript<br>
        </div>
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
