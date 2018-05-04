<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet"%>
<%@ page import="edu.lternet.pasta.portal.ReserveIdentifierServlet"%>
<%@ page import="edu.lternet.pasta.client.ReservationsManager"%>


<%
	final String pageTitle = "Identifier Reservation";
  final String titleText = DataPortalServlet.getTitleText(pageTitle);
  String path = request.getContextPath();
  String basePath = request.getScheme() + "://" + request.getServerName()
      + ":" + request.getServerPort() + path + "/";

  HttpSession httpSession = request.getSession();

  String deleteMessage = (String) request.getAttribute("deleteMessage");
  String deleteMessageHTML = "";
  String displayDivOpen = "<div>";
  String displayDivClose = "</div>";
  String messageType = (String) request.getAttribute("messageType");
  String messageClass = DataPortalServlet.messageClassFromMessageType(messageType);
  String reservationsTableHTML = "";
  String reservationsOptionsHTML = "";
  String reservationsDeleteOptionsHTML = "";
  String reservationMessage = (String) request.getAttribute("reservationMessage");
  String type = (String) request.getAttribute("type");

  String uid = (String) httpSession.getAttribute("uid");

  if (uid == null || uid.isEmpty()) {
    request.setAttribute("from", "./reservations.jsp");
    String loginWarning = DataPortalServlet.getLoginWarning();
    request.setAttribute("message", loginWarning);
    RequestDispatcher requestDispatcher = request
        .getRequestDispatcher("./login.jsp");
    requestDispatcher.forward(request, response);
  }
  else {
  	ReservationsManager reservationsManager = new ReservationsManager(uid);
  	int numberOfReservations = reservationsManager.numberOfReservations();
  	
  	if (numberOfReservations == 0) {
    	displayDivOpen = "<div class='display-none'>";
  	}
  	
  	reservationsTableHTML = reservationsManager.reservationsTableHTML();
  	reservationsOptionsHTML = reservationsManager.reservationsOptionsHTML();
    reservationsDeleteOptionsHTML = reservationsManager.reservationsDeleteOptionsHTML();

  	if (type == null) {
    	type = "";
  	} 
  	else {
    	type = "class=\"" + type + "\"";
  	}
  
    if (deleteMessage == null) { deleteMessage = ""; }
    if (!deleteMessage.isEmpty()) {
        deleteMessageHTML = String.format("<span class='%s'>%s</span>", 
                                          messageClass, deleteMessage);
    }

    if (reservationMessage  == null) { reservationMessage = ""; }
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
								<h1>Data Package Identifier Reservations</h1>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						
						<div class="row-fluid">
							<div class="span12">
								<!-- Content -->
                <h2>Reserve Identifiers</h2>
					<p>Reserve the next available identifier values for new data packages that you intend to <b>insert</b> into the EDI Repository (currently restricted to the <strong>edi</strong> scope):</p>
						<div class="section">
						  <form id="reserveidentifier" action="reserveidentifier" method="post" name="reserveidentifier">
								<table>
                                    <tr>
                                        <td>
                                            <label class="labelBold">How many identifiers in the edi scope would you like to reserve?:</label>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="spacersm"></td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <select class="select-width-auto" name="numberOfIdentifiers">
                                                <%= reservationsOptionsHTML %>
                                            </select>
                                        </td>
                                    </tr>
									<tr>
										<td>
											<input class="btn btn-info btn-default" name="reserve" type="submit" value="Reserve Next Available Identifier(s)" />
										</td>
									</tr>
								</table>
                                <input name="scope" type="hidden" value="edi" />
							</form>
						</div>
					<%= reservationMessage %>
				        <hr/>

     <%= displayDivOpen %>
      <h2>Current data package identifier reservations for <%= uid %></h2>
        <table>
          <tbody>
            <tr>
              <th class="nis">Data Package Identifier</th>
              <th class="nis">Reserved By</th>
              <th class="nis">Date Reserved</th>
            </tr>
            <%= reservationsTableHTML %>
          </tbody>
        </table>
        
    <hr/>
    
        <h2>Delete</h2>
        <p>Release your reservation on a data package identifier you previously reserved but no longer plan to use.</p>
        <form id="reserveidentifierdelete" action="reserveidentifierdelete" method="POST" name="reserveidentifierdelete">
            <table>
                <tr>
                    <td>
                        <label class="labelBold">Reservation ID</label>
                    </td>
                </tr>
                <tr>
                    <td>
                        <select class="select-width-auto" name="docid">
                            <%= reservationsDeleteOptionsHTML %>
                        </select>                                   
                    </td>
                </tr>
                <tr>
                    <td>
                        <input class="btn btn-info btn-default" name="delete" type="submit" value="Delete" />
                    </td>
                </tr>
            </table>
            <%= deleteMessageHTML %>
        </form>
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
