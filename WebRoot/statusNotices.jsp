<!--
~ Copyright 2011-2014 the University of New Mexico.
~
~ This work was supported by National Science Foundation Cooperative
~ Agreements #DEB-0832652 and #DEB-0936498.
~
~ Licensed under the Apache License, Version 2.0 (the "License");
~ you may not use this file except in compliance with the License.
~ You may obtain a copy of the License at
~ http://www.apache.org/licenses/LICENSE-2.0.
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
~ either express or implied. See the License for the specific
~ language governing permissions and limitations under the License.
-->

<%--
Display 3rd-party service status notices.
--%>

<%@ page import="java.util.*" pageEncoding="UTF-8" %>
<%@ page import="edu.lternet.pasta.portal.ConfigurationListener" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.Statement" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="javax.servlet.jsp.jstl.sql.Result" %>
<%@ page import="javax.servlet.jsp.jstl.sql.ResultSupport" %>
<%@ page import="edu.lternet.pasta.portal.database.DatabaseClient" %>
<%@ page import="org.apache.commons.configuration.Configuration" %>
<%@ page import="java.sql.SQLException" %>

<%
    Configuration options = ConfigurationListener.getOptions();
    String dbDriver = options.getString("db.Driver");
    String dbUrl = options.getString("db.URL");
    String dbUser = options.getString("db.User");
    String dbPassword = options.getString("db.Password");
    DatabaseClient databaseClient = new DatabaseClient(dbDriver, dbUrl, dbUser, dbPassword);
    Connection conn = databaseClient.getConnection();
    Result statusNotices;
    try {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
                "select * " +
                        "from authtoken.rss_feed " +
                        "where resolved is false " +
                        "and updated > now() - interval '8 hours' " +
                        "order by site, updated, published"
        );
        statusNotices = ResultSupport.toResult(rs);
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
%>

<% if (statusNotices.getRowCount() > 0) { %>

<link href="./css/pasta-status-notices.css" rel="stylesheet" type="text/css">

<div class="alert-block">
    <div class="alert alert-warning status-notices">
        <p>
            The following 3rd-party service<%= statusNotices.getRowCount() > 1 ? "s " : " " %>
            may be experiencing technical issues which can impact the Data Portal:
            <% for (SortedMap entry : statusNotices.getRows()) { %>
            <a href="<%= entry.get("url") %>" target="_blank" rel="noopener noreferrer" class="alert-link">
                <%= entry.get("site") %>
            </a>
            <% } %>
        </p>
    </div>
</div>

<% } %>
