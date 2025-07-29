<%@ page import="edu.lternet.pasta.portal.ConfigurationListener"%>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet" %>
<%@ page import="edu.lternet.pasta.client.DataPackageManagerClient" %>
<%@ page import="edu.lternet.pasta.client.PastaAuthenticationException" %>
<%@ page import="edu.lternet.pasta.client.PastaConfigurationException" %>
<%@ page import="org.apache.commons.configuration.PropertiesConfiguration" %>

<%!
    // Generate the URL that the final redirect will go to. This is the page
    // that the user ends up on after logging in.
    public String getTargetUrl() {
        PropertiesConfiguration options = ConfigurationListener.getOptions();
        int port = options.getInt("dataportal.port");
        return String.format(
                "%s://%s%s/%s/login",
                options.getString("dataportal.protocol"),
                options.getString("dataportal.hostname"),
                (port == 80 || port == 443) ? "" : ":" + port,
                options.getString("dataportal.context")
        );
    }

    // Generate the URL to the EDI auth service.
    public String getAuthLoginUrl() {
        PropertiesConfiguration options = ConfigurationListener.getOptions();
        int port = options.getInt("auth.port");
        return String.format(
                "%s://%s%s/auth/login",
                options.getString("auth.protocol"),
                options.getString("auth.hostname"),
                (port == 80 || port == 443) ? "" : ":" + port
        );
    }
%>
<%
    final String pageTitle = "Login";
    final String titleText = DataPortalServlet.getTitleText(pageTitle);
    HttpSession httpSession = request.getSession();

    String message = (String) httpSession.getAttribute("message");
    httpSession.removeAttribute("message");

    String from = (String) request.getAttribute("from");

    if (from != null && !from.isEmpty()) {
        httpSession.setAttribute("from", from);
    }

    String uid = (String) session.getAttribute("uid");
    if (uid == null || uid.isEmpty()) {
        uid = "public";
    }

    DataPackageManagerClient dpmc = null;
    String pastaHost = null;
    try {
        dpmc = new DataPackageManagerClient(uid);
        pastaHost = dpmc.getPastaHost();
    } catch (PastaAuthenticationException | PastaConfigurationException e) {
        e.printStackTrace();
        return;
    }

    String authLoginUrl = getAuthLoginUrl();
    String targetUrl = getTargetUrl();
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
    <link href="https://fonts.googleapis.com/css?family=Open+Sans:400,300,600,300italic" rel="stylesheet"
          type="text/css">

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

    <!-- PASTA style tweaks, should be after all other Bootstrap CSS -->
    <link href="./css/pasta-bootstrap-tweaks.css" rel="stylesheet" type="text/css">
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
                            <div class="recent_title">
                                <h2>Login</h2>
                            </div>
                            <span class="row-fluid separator_border"></span>
                            <h3>
                                Use your EDI account to upload data (contact
                                <a href="mailto:support@edirepository.org">
                                    support@edirepository.org</a> to create an account
                                or <a href="https://dashboard.edirepository.org/dashboard/auth/reset_password_init">click
                                here</a> to reset your password):
                            </h3>
                        </div>
                        <div class="row-fluid">
                            <div class="span12">
                                <!-- Content -->
                                <% if (message != null) { %>
                                <p>
                                    <div class="alert alert-block alert-error"><%=message%></div>
                                </p>
                                <% } %>
                                <form id="login" name="loginform" method="post" action="./login" target="_top">
                                    <div class="display-table">
                                        <div class="table-row">
                                            <div class="table-cell">
                                                <label class="labelBold text-align-right">User Name:</label>
                                            </div>
                                            <div class="table-cell">
                                                <input type="text" name="uid" required="required" autocomplete="on"
                                                       autofocus/>
                                            </div>
                                        </div>
                                        <div class="table-row">
                                            <div class="table-cell">
                                                <label class="labelBold text-align-right">Password:</label>
                                            </div>
                                            <div class="table-cell">
                                                <input type="password" name="password" required="required"/>
                                            </div>
                                        </div>
                                        <%--                                <div class="table-row">--%>
                                        <%--                                    <div class="table-cell">--%>
                                        <%--                                      <label class="labelBold text-align-right">Affiliation:</label>--%>
                                        <%--                                    </div>--%>
                                        <%--                                    <div class="table-cell">--%>
                                        <%--                                        <select id="affiliation-select" class="select-width-auto" name="affiliation">--%>
                                        <%--                                            <option value="EDI" selected="selected">EDI</option>--%>
                                        <%--                                            <option value="LTER">LTER</option>--%>
                                        <%--                                        </select>--%>
                                        <%--                                    </div>--%>
                                        <%--                                </div>--%>
                                        <div class="table-row">
                                            <div class="table-cell">
                                            </div>
                                            <div class="table-cell">
                                                <input class="btn btn-info btn-default" name="login" type="submit"
                                                       value="Login"/>
                                                <input class="btn btn-info btn-default" name="reset" type="reset"
                                                       value="Clear"/>
                                            </div>
                                        </div>
                                    </div>
                                </form>
                                <h3>Or use an alternate identity provider to access data requiring user
                                    authentication:</h3>
                                <p>
                                    <a href="<%= authLoginUrl %>/google?target=<%= targetUrl %>">
                                        <img src="./images/btn_google_signin_light_normal_web.png"
                                             alt="Log in with Google"/></a>
                                    &nbsp;&nbsp;
                                    <a href="<%= authLoginUrl %>/github?target=<%= targetUrl %>">
                                        <img src="./images/btn_github_signin_light_normal_web.png"
                                             alt="Log in with GitHub"/></a>
                                    &nbsp;&nbsp;
                                    <a href="<%= authLoginUrl %>/microsoft?target=<%= targetUrl %>">
                                        <img src="./images/btn_microsoft_signin_light_normal_web.png"
                                             alt="Log in with Microsoft"/></a>
                                    &nbsp;&nbsp;
                                    <a href="<%= authLoginUrl %>/orcid?target=<%= targetUrl %>">
                                        <img src="./images/btn_orcid_signin_light_normal_web.png"
                                             alt="Log in with ORCID"/></a>
                                </p>
                                <br/><br/><br/>
                                <p>Please read our
                                    <a class="searchsubcat"
                                       href="https://edirepository.org/about/edi-policy#privacy-policy">privacy
                                        policy</a>
                                    to know what information we collect about you and to understand your privacy rights.
                                </p>
                                <!-- /Content -->
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
