<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet"%>
<%@ page import="edu.lternet.pasta.client.JournalCitationsClient"%>

<%
    final String pageTitle = "Journal Citations";
  final String titleText = DataPortalServlet.getTitleText(pageTitle);
  String path = request.getContextPath();
  String basePath = request.getScheme() + "://" + request.getServerName()
      + ":" + request.getServerPort() + path + "/";

  HttpSession httpSession = request.getSession();

  String displayDivOpen = "<div>";
  String displayDivClose = "</div>";
  String journalCitationsHTML = "";
  String journalCitationOptionsHTML = "";
  String subscribeMessage = (String) request.getAttribute("subscribemessage");
  String deleteMessage = (String) request.getAttribute("deletemessage");
  String testMessage = (String) request.getAttribute("testmessage");
  String type = (String) request.getAttribute("type");

  String uid = (String) httpSession.getAttribute("uid");

  if (uid == null || uid.isEmpty()) {
    request.setAttribute("from", "./journalCitations.jsp");
    String loginWarning = DataPortalServlet.getLoginWarning();
    request.setAttribute("message", loginWarning);
    RequestDispatcher requestDispatcher = request
        .getRequestDispatcher("./login.jsp");
    requestDispatcher.forward(request, response);
  }
  else {
  	JournalCitationsClient jcc = new JournalCitationsClient(uid);
  	journalCitationsHTML = jcc.citationsTableHTML();
  	journalCitationOptionsHTML = jcc.citationsOptionsHTML();

  	if (type == null) {
    	type = "";
  	} 
  	else {
    	type = "class=\"" + type + "\"";
  	}
  
  	if (subscribeMessage  == null) { subscribeMessage = ""; }
  	if (testMessage  == null) { testMessage = ""; }
  	if (deleteMessage  == null) { deleteMessage = ""; }
  }
%>

<!DOCTYPE html>
<html lang="en">

<head>
<title><%= titleText %></title>

<meta charset="UTF-8" />
<meta content="width=device-width, initial-scale=1, maximum-scale=1" name="viewport">

<link rel="shortcut icon" href="./images/favicon.ico" type="image/x-icon" />

<!-- Google Fonts CSS -->
<link href="https://fonts.googleapis.com/css?family=Open+Sans:400,300,600,300italic" rel="stylesheet" type="text/css">

<!-- Page Layout CSS MUST LOAD BEFORE bootstap.css -->
<link href="css/style_slate.css" media="all" rel="stylesheet" type="text/css">

<!-- JS -->
<script src="js/jqueryba3a.js?ver=1.7.2" type="text/javascript"></script>
<script src="bootstrap/js/bootstrap68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/jquery.easing.1.368b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/jquery.flexslider-min68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/themeple68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/jquery.pixel68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/jquery.mobilemenu68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/isotope68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/mediaelement-and-player.min68b368b3.js?ver=1" type="text/javascript"></script>

<!-- Mobile Device CSS -->
<link href="bootstrap/css/bootstrap.css" media="screen" rel="stylesheet" type="text/css">
<link href="bootstrap/css/bootstrap-responsive.css" media="screen" rel="stylesheet" type="text/css">

</head>

<body>

<jsp:include page="header.jsp" />

<div class="row-fluid ">
	<div>
		<div class="container">
			<div class="row-fluid distance_1">
				<div class="box_shadow box_layout">
					<div class="row-fluid">
						<div class="span12">
							<div class="recent_title">
								<h1><%= pageTitle %></h1>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						
						<div class="row-fluid">
							<div class="span12">
								<!-- Content -->
                <h2>Enter a journal citation for data package</h2>
                
								<p>You may enter the DOI or URL of a journal article that cites a data package by supplying the information in the fields below.
								   At minimum, you will need to enter the package ID of the data package and the DOI or the URL of the journal article that cites it.
								   This information will be recorded in the EDI Data Repository and displayed on the summary page for the
								   data package.</p>

								<div class="section">
									<form id="journalcitationadd" action="journalcitationadd" method="post" name="journalcitationadd">
										<table>
											<tr>
												<td>
												  <label class="labelBold" for="packageid">Package Id</label>
												</td>
                                                <td>&nbsp;</td>
												<td>
												  <label class="labelBold" for="articledoi">Article DOI</label>
												</td>
											</tr>
                                            <tr>
                                                <td>
                                                  <input name="packageid" required="required" size="50" type="text" />
                                                </td>
                                                <td>&nbsp;</td>
                                                <td>
                                                  <input name="articledoi" size="50" type="text"/>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td>
                                                  <label class="labelBold" for="articleurl">Article URL</label>
                                                </td>
                                                 <td>&nbsp;</td>
                                                <td>
                                                  <label class="labelBold" for="articletitle">Article Title</label>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td>
                                                  <input name="articleurl" size="50" type="text" />
                                                </td>
                                                <td>&nbsp;</td>
                                                <td>
                                                  <input name="articletitle" size="50" type="text"/>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td>
                                                  <label class="labelBold" for="journalname">Journal Name</label>
                                                </td>
                                                <td>&nbsp;</td>
                                                <td>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td>
                                                <input name="journalname" size="50" type="text" />
                                                </td>
                                                <td>&nbsp;</td>
                                                <td>
                                                </td>
                                            </tr>
											<tr>
												<td>
													<input class="btn btn-info btn-default" name="add" type="submit" value="Add Journal Citation" />
													<input class="btn btn-info btn-default" name="reset" type="reset" value="Clear" />
												</td>
											</tr>
										</table>
									</form>
								</div>
					<%= subscribeMessage %>
				        <hr/>

     <%= displayDivOpen %>
      <h2>Current journal article citations recorded by <%= uid %></h2>
        <table>
          <tbody>
            <tr>
              <th class="nis">Journal Citation Id</th>
              <th class="nis">Package Id</th>
              <th class="nis">Journal Article DOI</th>
              <th class="nis">Journal Article URL</th>
              <th class="nis">Journal Article Title</th>
              <th class="nis">Journal Name</th>
            </tr>
            <%= journalCitationsHTML %>
          </tbody>
        </table>
							
								<h2>Delete</h2>
								<p>Delete a journal citation entry you previously entered using the journal citation id.</p>
									<form id="journalcitationdelete" action="journalcitationdelete" method="POST" name="journalcitationdelete">
										<table>
											<tr>
												<td>
												<label class="labelBold">Journal Citation Id</label>
												</td>
											</tr>
											<tr>
												<td>
                                                    <select class="select-width-auto" name="journalcitationid">
                                                    <%= journalCitationOptionsHTML %>
                                                    </select>									
												</td>
											</tr>
											<tr>
												<td>
												<input class="btn btn-info btn-default" name="delete" type="submit" value="Delete" />
												</td>
											</tr>
										</table>
									</form>
									<%= deleteMessage %>
					  <%= displayDivClose %>
								<!-- /Content -->
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>

		<jsp:include page="footer.jsp" />

</div>

</body>

</html>
