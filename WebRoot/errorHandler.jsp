<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet" %>
<%@ page import="edu.lternet.pasta.portal.ExceptionFormatter" %>
<%@ page import="org.json.JSONObject" %>
<%@ page isErrorPage="true"%>

<%
  final String pageTitle = "Error Handler";
  final String titleText = DataPortalServlet.getTitleText(pageTitle);

  String exceptionMessage = String.valueOf(pageContext.getException().getMessage());
  ExceptionFormatter exceptionFormatter = new ExceptionFormatter();
  String message = exceptionFormatter.getMessage(exceptionMessage);
  String exceptionJSON = exceptionFormatter.getJSONString(exceptionMessage);
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
								<!-- <h2>Title</h2> -->
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<div class="span12">							
								<!-- Content -->
    				      <p>We apologize, but an unexpected error occured in the EDI Data Portal or the Data Repository:</p>
                          <p class="nis-error"><%= message %></p>
                          <p>
                              For further assistance, please
                              <a href="javascript:void(0);" onclick="copyError()"><u><b>copy</b></u></a>
                              the error message shown above, along with any other information that might help
                              us to assist you more promptly, and send it to our
                              <a href="mailto:support@edirepository.org?Subject=EDI%20Data%20Portal%20error" target="_top">
                              <u>support team</u></a> at the Environmental Data Initiative.</p>
                         </div>
								<!-- /Content -->
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>

<jsp:include page="footer.jsp" />

<script type="text/javascript">
    function copyError()
    {
        var copyErrorText = "<%= exceptionJSON %>";
        navigator.clipboard.writeText(copyErrorText)
            .then(() => {
                alert("Error copied successfully!");
            })
            .catch(err => {
                console.error("Failed to copy error: ", err);
                alert("Failed to copy error. Please copy and paste the error message.");
            });
    }
</script>

</body>

</html>
