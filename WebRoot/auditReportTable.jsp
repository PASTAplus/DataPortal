<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet"%>

<%
    final String pageTitle = "Audit Report";
    final String titleText = DataPortalServlet.getTitleText(pageTitle);

	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName()
	    + ":" + request.getServerPort() + path + "/";

	HttpSession httpSession = request.getSession();
	Boolean vetted = (Boolean) httpSession.getAttribute("vetted");

	if (vetted == null || !vetted) {
		request.setAttribute("from", "./auditReportTable.jsp");
		String loginWarning = DataPortalServlet.getLoginWarning();
		request.setAttribute("message", loginWarning);
		RequestDispatcher requestDispatcher = request.getRequestDispatcher("./login.jsp");
		requestDispatcher.forward(request, response);
	}

	String reportMessage = (String) request.getAttribute("reportMessage");
	String serviceMethod = (String) request.getAttribute("serviceMethod");
	String debug = (String) request.getAttribute("debug");
	String info = (String) request.getAttribute("info");
	String warn = (String) request.getAttribute("warn");
	String error = (String) request.getAttribute("error");
	String code = (String) request.getAttribute("code");
	String userId = (String) request.getAttribute("userId");
	String affiliation = (String) request.getAttribute("affiliation");
	String beginDate = (String) request.getAttribute("beginDate");
	String beginTime = (String) request.getAttribute("beginTime");
	String endDate = (String) request.getAttribute("endDate");
	String endTime = (String) request.getAttribute("endTime");
	String startRowId = (String) request.getAttribute("startRowId");
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

<%--<jsp:include page="header.jsp" />--%>

<div class="row-fluid ">
	<div>
		<div class="container">
			<div class="row-fluid distance_1">
				<div class="box_shadow box_layout">
					<div class="row-fluid">
						<div class="span12">
							<div class="recent_title">
								<h2>Audit Report</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<div class="span12">
								<!-- Content -->
                                <form method="post">
									<input type='hidden' name='serviceMethod' value='<%= serviceMethod %>'>
									<input type='hidden' name='debug' value='<%= debug %>'>
									<input type='hidden' name='info' value='<%= info %>'>
									<input type='hidden' name='warn' value='<%= warn %>'>
									<input type='hidden' name='error' value='<%= error %>'>
									<input type='hidden' name='code' value='<%= code %>'>
									<input type='hidden' name='userId' value='<%= userId %>'>
									<input type='hidden' name='affiliation' value='<%= affiliation %>'>
									<input type='hidden' name='beginDate' value='<%= beginDate %>'>
									<input type='hidden' name='beginTime' value='<%= beginTime %>'>
									<input type='hidden' name='endDate' value='<%= endDate %>'>
									<input type='hidden' name='endTime' value='<%= endTime %>'>
									<input type='hidden' name='startRowId' value='<%= startRowId %>'>
                                    <button type="submit">Next</button>
                                </form>
			                    <%= reportMessage %>
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
