<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet" %>

<%
    final String pageTitle = "Audit Report";
    final String titleText = DataPortalServlet.getTitleText(pageTitle);

    String path = request.getContextPath();
    String basePath =
            request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";

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
    String endDate = (String) request.getAttribute("endDate");
    String beginTime = (String) request.getAttribute("beginTime");
    String endTime = (String) request.getAttribute("endTime");

    Integer firstRowId = (Integer) request.getAttribute("firstRowId");
    Integer lastRowId = (Integer) request.getAttribute("lastRowId");

    String scope = (String) request.getAttribute("scope");
    String identifier = (String) request.getAttribute("identifier");
    String revision = (String) request.getAttribute("revision");

    String package_ = (String) request.getAttribute("package");
    String metadata = (String) request.getAttribute("metadata");
    String entity = (String) request.getAttribute("entity");
    String report = (String) request.getAttribute("report");

    String userAgent = (String) request.getAttribute("userAgent");
    String userAgentNegate = (String) request.getAttribute("userAgentNegate");

    String userDn = (String) request.getAttribute("userDn");
    String userDnNegate = (String) request.getAttribute("userDnNegate");

    Integer pageIdx = (Integer) request.getAttribute("pageIdx");
%>

<!DOCTYPE html>
<html lang="en">

<head>
    <title><%= titleText %>
    </title>

    <meta charset="UTF-8"/>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1"
          name="viewport">

    <link rel="shortcut icon" href="./images/favicon.ico" type="image/x-icon"/>

    <!-- Google Fonts CSS -->
    <link href="https://fonts.googleapis.com/css?family=Open+Sans:400,300,600,300italic"
          rel="stylesheet" type="text/css">

    <!-- Page Layout CSS MUST LOAD BEFORE bootstap.css -->
    <link href="css/style_slate.css" media="all" rel="stylesheet" type="text/css">

    <!-- JS -->
    <script src="js/jqueryba3a.js?ver=1.7.2" type="text/javascript"></script>
    <script src="bootstrap/js/bootstrap68b368b3.js?ver=1"
            type="text/javascript"></script>
    <script src="js/jquery.easing.1.368b368b3.js?ver=1" type="text/javascript"></script>
    <script src="js/jquery.flexslider-min68b368b3.js?ver=1"
            type="text/javascript"></script>
    <script src="js/themeple68b368b3.js?ver=1" type="text/javascript"></script>
    <script src="js/jquery.pixel68b368b3.js?ver=1" type="text/javascript"></script>
    <script src="js/jquery.mobilemenu68b368b3.js?ver=1" type="text/javascript"></script>
    <script src="js/isotope68b368b3.js?ver=1" type="text/javascript"></script>
    <script src="js/mediaelement-and-player.min68b368b3.js?ver=1"
            type="text/javascript"></script>

    <!-- Mobile Device CSS -->
    <link href="bootstrap/css/bootstrap.css" media="screen" rel="stylesheet"
          type="text/css">
    <link href="bootstrap/css/bootstrap-responsive.css" media="screen" rel="stylesheet"
          type="text/css">

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
                                <h2>Audit Report</h2>
                            </div>
                            <span class="row-fluid separator_border"></span>
                        </div>
                    </div>
                    <div class="pasta-button-row">
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
                            <input type='hidden' name='endDate' value='<%= endDate %>'>
                            <input type='hidden' name='beginTime' value='<%= beginTime %>'>
                            <input type='hidden' name='endTime' value='<%= endTime %>'>

                            <input type='hidden' name='scope' value='<%= scope %>'>
                            <input type='hidden' name='identifier' value='<%= identifier %>'>
                            <input type='hidden' name='revision' value='<%= revision %>'>

                            <input type='hidden' name='package' value='<%= package_ %>'>
                            <input type='hidden' name='metadata' value='<%= metadata %>'>
                            <input type='hidden' name='entity' value='<%= entity %>'>
                            <input type='hidden' name='report' value='<%= report %>'>

                            <input type='hidden' name='userAgent' value='<%= userAgent %>'>
                            <input type='hidden' name='userAgentNegate' value='<%= userAgentNegate %>'>

                            <input type='hidden' name='userDn' value='<%= userDn %>'>
                            <input type='hidden' name='userDnNegate' value='<%= userDnNegate %>'>

                            <input type='hidden' name='startRowId' value='0'>
                            <input type='hidden' name='getPrev' value='0'>
                            <input type='hidden' name='pageIdx' value='<%= 0 %>'>
                            <button type="submit" class='btn btn-info <%= pageIdx > 0 ? "" : "pasta-button-disabled" %>'>
                                First
                            </button>
                        </form>
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
                            <input type='hidden' name='endDate' value='<%= endDate %>'>
                            <input type='hidden' name='beginTime' value='<%= beginTime %>'>
                            <input type='hidden' name='endTime' value='<%= endTime %>'>

                            <input type='hidden' name='scope' value='<%= scope %>'>
                            <input type='hidden' name='identifier' value='<%= identifier %>'>
                            <input type='hidden' name='revision' value='<%= revision %>'>

                            <input type='hidden' name='package' value='<%= package_ %>'>
                            <input type='hidden' name='metadata' value='<%= metadata %>'>
                            <input type='hidden' name='entity' value='<%= entity %>'>
                            <input type='hidden' name='report' value='<%= report %>'>

                            <input type='hidden' name='userAgent' value='<%= userAgent %>'>
                            <input type='hidden' name='userAgentNegate' value='<%= userAgentNegate %>'>

                            <input type='hidden' name='userDn' value='<%= userDn %>'>
                            <input type='hidden' name='userDnNegate' value='<%= userDnNegate %>'>

                            <input type='hidden' name='startRowId' value='<%= firstRowId %>'>
                            <input type='hidden' name='getPrev' value='1'>

                            <input type='hidden' name='pageIdx' value='<%= pageIdx - 1 %>'>
                            <button type="submit" class='btn btn-info <%= (pageIdx > 0 && !(firstRowId >= lastRowId)) ? "" : "pasta-button-disabled" %>'>
                                Prev
                            </button>
                        </form>
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
                            <input type='hidden' name='endDate' value='<%= endDate %>'>
                            <input type='hidden' name='beginTime' value='<%= beginTime %>'>
                            <input type='hidden' name='endTime' value='<%= endTime %>'>
                            <input type='hidden' name='getPrev' value='0'>

                            <input type='hidden' name='scope' value='<%= scope %>'>
                            <input type='hidden' name='identifier' value='<%= identifier %>'>
                            <input type='hidden' name='revision' value='<%= revision %>'>

                            <input type='hidden' name='package' value='<%= package_ %>'>
                            <input type='hidden' name='metadata' value='<%= metadata %>'>
                            <input type='hidden' name='entity' value='<%= entity %>'>
                            <input type='hidden' name='report' value='<%= report %>'>

                            <input type='hidden' name='userAgent' value='<%= userAgent %>'>
                            <input type='hidden' name='userAgentNegate' value='<%= userAgentNegate %>'>

                            <input type='hidden' name='userDn' value='<%= userDn %>'>
                            <input type='hidden' name='userDnNegate' value='<%= userDnNegate %>'>

                            <input type='hidden' name='startRowId' value='<%= lastRowId %>'>
                            <input type='hidden' name='pageIdx' value='<%= pageIdx + 1 %>'>
                            <button type="submit" class='btn btn-info btn-default <%= firstRowId < lastRowId ? "" : "pasta-button-disabled" %>'>
                                Next
                            </button>
                        </form>
                    </div>
                    <div class="row-fluid">
                        <div class="row-fluid">
                            <div class='span12'>
                                <%= reportMessage %>
                            </div>
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
