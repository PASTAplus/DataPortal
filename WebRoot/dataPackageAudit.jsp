<%--suppress ALL --%>

<%--suppress XmlDefaultAttributeValue --%>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<%@ page import="edu.lternet.pasta.portal.ConfigurationListener" %>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet" %>
<%
  final String pageTitle = "Data Package Access Reports";
  final String titleText = DataPortalServlet.getTitleText(pageTitle);
  String path = request.getContextPath();
  String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";

  HttpSession httpSession = request.getSession();
  Boolean vetted = (Boolean) httpSession.getAttribute("vetted");

  if (vetted == null || !vetted) {
    request.setAttribute("from", "./dataPackageAudit.jsp");
    String loginWarning = DataPortalServlet.getLoginWarning();
    request.setAttribute("message", loginWarning);
    RequestDispatcher requestDispatcher = request.getRequestDispatcher("./login.jsp");
    requestDispatcher.forward(request, response);
  }

  String reportMessage = (String) request.getAttribute("reportMessage");
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

  <!-- For Custom Checkboxes -->
  <script src="js/jquery-1.8.3.min.js" type="text/javascript"></script>
  <script type="text/javascript">

  $(document).ready(function () {
    $(".checklist .checkbox-select").click(
        function (event) {
          event.preventDefault();
          $(this).parent().addClass("selected");
          $(this).parent().find(":checkbox").attr("checked", "checked");

        }
    );

    $(".checklist .checkbox-deselect").click(
        function (event) {
          event.preventDefault();
          $(this).parent().removeClass("selected");
          $(this).parent().find(":checkbox").removeAttr("checked");

        }
    );

  });

  </script>
  <!-- /For Custom Checkboxes -->

</head>

<body>

<jsp:include page="header.jsp"/>

<form id="dataPackageAudit" action="./dataPackageAudit" method="post" name="dataPackageAudit">

  <div class="row-fluid ">
    <div class="container">
      <div class='row-fluid'>
        <div class='span12'>
        </div>
      </div>

      <div class="row-fluid distance_1">
        <%--<div class="box_layout">--%>
        <div class="span12">
          <div class="recent_title">
            <h2>Data Package Access Report</h2>
          </div>
        </div>
        <%--</div>--%>

        <div class="row-fluid distance_1 separator_border">
          <div class="span12">
            Review a Data Package access report by entering information into one or more of the filters below.
          </div>
        </div>

        <div class="row-fluid">
          <div class="span12">
            <table>
              <tr>
                <td>
                  <label class="labelBold">Scope:</label>
                  <input autofocus required name="scope" size="15" type="text" placeholder="e.g., knb-lter-nin"/>
                </td>
                <td>
                  <label class="labelBold">Identifier:</label>
                  <input name="identifier" size="5" type="number" placeholder="e.g., 1"/>
                </td>
                <td>
                  <label class="labelBold">Revision:</label>
                  <input name="revision" size="5" type="number" placeholder="e.g., 3"/>
                </td>
              </tr>
            </table>
          </div>
        </div>

        <div class="row-fluid">
          <div class="span12">
            <label class="labelBold">Resource Type:</label>
          </div>
        </div>

        <div class="row-fluid">
          <div class="span12">
            <form>
              <label for="choices">
                <ul class="checklist">
                  <li>
                    <input name="package" type="checkbox" value="1"/>
                    <p>Package</p>
                    <a class="checkbox-select" href="#">Select</a>
                    <a class="checkbox-deselect" href="#">Cancel</a>
                  </li>
                  <li>
                    <input name="metadata" type="checkbox" value="1"/>
                    <p>Metadata</p>
                    <a class="checkbox-select" href="#">Select</a>
                    <a class="checkbox-deselect" href="#">Cancel</a>
                  </li>
                  <li>
                    <input name="entity" type="checkbox" value="1"/>
                    <p>Data</p>
                    <a class="checkbox-select" href="#">Select</a>
                    <a class="checkbox-deselect" href="#">Cancel</a>
                  </li>
                  <li>
                    <input name="report" type="checkbox" value="1"/>
                    <p>Report</p>
                    <a class="checkbox-select" href="#">Select</a>
                    <a class="checkbox-deselect" href="#">Cancel</a>
                  </li>
                </ul>
              </label>
            </form>
          </div>
        </div>

        <div class="row-fluid distance_2">
          <div class="span12">
            <table>
              <tr>
                <td>
                  <label class="labelBold">User Agent:</label>
                  <input name="userAgent" size="15" type="text"/>
                </td>
                <td class='pasta-radio-button-col'>
                  <label class="labelBold">Matching User Agents:</label>
                  <%--<div class='pasta-radio-button-col'>--%>
                  <span>
                      <input type="radio" id="id-useragent-include" name="userAgentNegate" value="0" checked>
                      <label class='pasta-inline' for="id-useragent-include">Include</label>
                    </span>
                  <br>
                  <span>
                      <input type="radio" id="id-useragent-exclude" name="userAgentNegate" value="1">
                      <label class='pasta-inline' for="id-useragent-exclude">Exclude</label>
                    </span>
                  <%--</div>--%>
                </td>
              </tr>
            </table>
          </div>
        </div>

        <div class="row-fluid">
          <div class="span12">
            <table>
              <tr>
                <td>
                  <label class="labelBold">Begin Date:</label>
                  <input name="begin" placeholder="YYYY-MM-DD" type="date"/>
                </td>
                <td>
                  <label class="labelBold">End Date:</label>
                  <input name="end" placeholder="YYYY-MM-DD" type="date"/>
                </td>
              </tr>
            </table>
          </div>
        </div>

        <div class="row-fluid">
          <div class="span12">
            <div class='pasta-button-row'>
              <input class="btn btn-info" name="download" type="submit" value="Download"/>
              <input class="btn btn-info btn-default" name="submit" type="submit" value="View"/>
              <input class="btn btn-info btn-default" name="reset" type="reset" value="Clear"/>
            </div>
          </div>
        </div>

        <div class="row-fluid">
          <div class="span12">
            <%
              if (reportMessage != null) {
                out.println(String.format("<p class=\"nis-warn\">%s</p>", reportMessage));
              }
            %>
          </div>
        </div>
      </div>
    </div>
  </div>

</form>

<jsp:include page="footer.jsp"/>

</body>

</html>
