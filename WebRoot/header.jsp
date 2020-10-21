<%@ page import="edu.lternet.pasta.portal.LoginServlet" %>
<%@ page import="edu.lternet.pasta.portal.Tooltip" %>
<%@ page import="edu.lternet.pasta.client.DataPackageManagerClient" %>

<!-- Header -->
<%

	HttpSession httpSession = request.getSession();
	String uid = (String) httpSession.getAttribute("uid");
	String cname = (String) httpSession.getAttribute("cname");
	String identity = null;
	String uname = null;
	String welcomeBack = null;
	
	if ((uid == null) || (uid.equals(""))) {
		identity = "<a href='./login.jsp'>Login</a>";
		uname = "";
		welcomeBack = "";
		uid = "public";
	} 
	else {
        identity = "<a id=\"login\" href=\"./logout\">Log Out</a>";
		uname = cname;
		welcomeBack = "Welcome Back";
	}
	
	DataPackageManagerClient dpmc = new DataPackageManagerClient(uid);
	String pastaHost = dpmc.getPastaHost();
	String tierHTML = "EDI Data Portal";
	if (pastaHost.startsWith("pasta-d") || 
	    pastaHost.startsWith("localhost")
	   ) {
	  tierHTML = "<font color='darkorange'>EDI Development Environment</font>";
	}
	else if (pastaHost.startsWith("pasta-s")) {
	  tierHTML = "<font color='darkorange'>EDI Staging Environment</font>";
	}

  final String currentClass = " class='current-menu-item current_page_item'";
  String dataClass = "";
  String helpClass = "";
  String homeClass = "";
  String loginClass = "";
  String toolsClass = "";
  String requestURI = request.getRequestURI();
  String pageName = "";
  
  if (requestURI.contains(".")) {
      pageName = requestURI.substring(requestURI.lastIndexOf("/") + 1, 
                                        requestURI.lastIndexOf(".")
                                       );
  }
  else {
      pageName = requestURI.substring(requestURI.lastIndexOf("/") + 1);
  }
  
  if (pageName.equals("browse") ||
           pageName.equals("packageIdentifier") ||
           pageName.equals("advancedSearch") ||
           pageName.equals("savedData") ||
           pageName.equals("dataPackageBrowser")
          ) {
    dataClass = currentClass;
  }
  else if (pageName.equals("help") ||
           pageName.equals("resources") ||
           pageName.equals("about") ||
           pageName.equals("contact")
          ) {
    helpClass = currentClass;
  }
  else if (pageName.equals("home")) {
    homeClass = currentClass;
  }
  else if (
           pageName.equals("previewMetadata") ||
           pageName.equals("dataPackageEvaluate") ||
           pageName.equals("harvester") ||
           pageName.equals("harvestReport") ||
           //pageName.equals("dataPackageDelete") ||
           pageName.equals("eventSubscribe") ||
           pageName.equals("reservations") ||
           pageName.equals("provenanceGenerator") ||
           pageName.equals("journalCitations") ||
           pageName.equals("dataPackageAudit") ||
           pageName.equals("auditReport")
          ) {
    toolsClass = currentClass;
  }
  else if (pageName.equals("login")) {
    loginClass = currentClass;
  }
%>
 
<header role="banner">
<div class="row-fluid ">
	<div class="span12 page_top_header base_color_background">
	</div>
</div>
<div class="container">
	<div class="row-fluid header_container">
		<div class="span6">
            <div class="display-table">
                <div class="table-row">
                    <div class="table-cell">
                        <a href="home.jsp"><img id="edi-img" class="nis-logos-img" alt="Environmental Data Initiative logo" src="images/EDI-logo-300DPI_5.png" title="EDI"></a>
                        <br/>
                    </div>
                    <div class="table-cell">
                        <span><big><strong><%= tierHTML %></strong></big></span>
                    </div>
                </div>
            </div>
	    </div>
	    
	    
			<div class="span6 menu">
			<nav role="navigation">
			<ul id="menu-nav" class="menu">
				<li<%= homeClass %>><a href="home.jsp">Home</a></li>
				<li<%= dataClass %>><a href="#">Data</a>
				<ul class="sub-menu">
					<p class="smallmenu pull-left nis-navigation-submenu">
						<img class="mini-arrow-margin" alt="" src="images/mini_arrow.png" title="EDI">
						EDI Data Policy:</p>
					<li><a href="https://environmentaldatainitiative.org/edi-data-policy-2/">Data Policy</a> </li>
					<p class="smallmenu pull-left nis-navigation-submenu">
					<img class="mini-arrow-margin" alt="" src="images/mini_arrow.png" title="EDI">
					Browse Data By:</p>
					<li><a href="browse.jsp">Keyword or Research Site</a> </li>
					<li><a href="scopebrowse">Package Identifier</a> </li>
					<p class="smallmenu pull-left nis-navigation-submenu">
					  <img class="mini-arrow-margin" alt="" src="images/mini_arrow.png" title="EDI"> 
					Search Data:</p>
					<li><a href="advancedSearch.jsp">Advanced Search</a> </li>
					<p class="smallmenu pull-left nis-navigation-submenu">
					  <img class="mini-arrow-margin" alt="" src="images/mini_arrow.png" title="EDI"> 
			        View Your Data:</p>
					<li><a href="savedDataServlet">Your Data Shelf</a> </li>
                    <li><a href="userBrowseServlet">Your Uploaded Data</a> </li>
				</ul>
				</li>
				<li<%= toolsClass %>><a href="#">Tools</a>
				  <ul class="sub-menu">
					
					<p class="smallmenu pull-left nis-navigation-submenu">
					<img alt="" src="images/mini_arrow.png" class="mini-arrow-margin" title="EDI"> 
					Data Packages:</p>
                    <li><a href="metadataPreviewer.jsp">Preview Your Metadata</a> </li>
					<!-- <li><a href="dataPackageEvaluate.jsp">Evaluate Data Packages</a></li> -->
					<li><a href="harvester.jsp">Evaluate/Upload Data Packages</a></li>
					<li><a href="harvestReport.jsp">View Evaluate/Upload Results</a></li>
					<!--  <li><a href="dataPackageDelete.jsp">Delete Data Packages</a></li> -->
					
                    <p class="smallmenu pull-left nis-navigation-submenu">
                    <img alt="" src="images/mini_arrow.png" class="mini-arrow-margin" title="EDI"> 
                    Citations and Provenance:</p>
                    <li><a href="journalCitations.jsp">Journal Citations</a></li>
                    <li><a href="provenanceGenerator.jsp">Provenance Generator</a></li>
                    
					<p class="smallmenu pull-left nis-navigation-submenu">
					<img alt="" src="images/mini_arrow.png" class="mini-arrow-margin" title="EDI"> 
					Events:</p>
					<li><a href="eventSubscribe.jsp">Event Subscriptions</a></li>
					
					<p class="smallmenu pull-left nis-navigation-submenu">
					<img alt="" src="images/mini_arrow.png" class="mini-arrow-margin" title="EDI"> 
					Reports:</p>
					<li><a href="auditReport.jsp">Audit Reports</a></li>
					<li><a href="dataPackageAudit.jsp">Data Package Access Reports</a></li>
                    
                    <p class="smallmenu pull-left nis-navigation-submenu">
                    <img alt="" src="images/mini_arrow.png" class="mini-arrow-margin" title="EDI"> 
                    Reservations:</p>
                    <li><a href="reservations.jsp">Data Package Identifier Reservations</a></li>
                    <p class="smallmenu pull-left nis-navigation-submenu">
                    <img alt="" src="images/mini_arrow.png" class="mini-arrow-margin" title="EDI">
                    Metadata:</p>
                    <li><a href="https://ezeml.edirepository.org/eml/">ezEML</a></li>
                    <li><a href="https://github.com/EDIorg/EMLassemblyline">EMLassemblyline</a></li>
				  </ul>
				</li>
				<li<%= helpClass %>><a href="#">Help</a>
				<ul class="sub-menu">
					<p class="smallmenu pull-left nis-navigation-submenu">
					<img alt="" src="images/mini_arrow.png" class="mini-arrow-margin" title="EDI"> 
					Support:</p>
				  <!-- <li><a href="help.jsp">How Do I...</a></li> -->
				  <li><a href="contact.jsp">Contact Us</a></li>
					<p class="smallmenu pull-left nis-navigation-submenu">
					<img alt="" src="images/mini_arrow.png" class="mini-arrow-margin" title="EDI"> 
					Resources:</p>
				  <li><a href="resources.jsp">Additional Resources</a></li>
					<p class="smallmenu pull-left nis-navigation-submenu">
					<img alt="" src="images/mini_arrow.png" class="mini-arrow-margin" title="EDI"> 
					About:</p>
                  <li><a href="https://environmentaldatainitiative.org/">About the EDI Project</a></li>
				  <li><a href="about.jsp">About the EDI Data Portal</a></li>
				</ul>
				</li>
				<li<%= loginClass %>><%= identity %></li>
			</ul>
			</nav>
		</div>
	</div>
</div>
<!-- /Header -->

<!-- Divider -->
<div class="row-fluid ">
	<div class="span12 page_top_header line-divider">
	</div>
</div>
<!-- /Divider -->

<!-- Search Section -->
<div class="row-fluid page_title">
	<div class="container">
		<div class="span8">
			<h2 class="title_size"><%= welcomeBack %></h2>
			<h2 class="title_desc loggedin"><%= uname %></h2>
		</div>
		<div class="span4">
			<div class="pull-right">
				<div id="search-3" class="widget title_widget widget_search">
				  <form id="searchform" action="./simpleSearch" class="form-inline" method="post" >
				    <!-- <label class="nis-search-label">Search Terms</label> -->
					<!-- <span name='<%= Tooltip.SEARCH_TERMS %>'
						  class="tooltip"> -->
						<input type="search" 
							name="terms" 
							id="lterterms" 
							class="span11 search-query"
							placeholder="enter search terms" 
							size="25" required="required">
					<!-- </span> -->
						<button class="search_icon" type="submit"></button>
						<label id="advanced-search-label" class="nis-search-label">
						  <img id="advanced-search-arrow" alt="" src="images/mini_arrow.png" title="Advanced Search">
						  <a href="advancedSearch.jsp">ADVANCED SEARCH</a>
						</label>
					</form>
					<span class="seperator extralight-border"></span></div>
			</div>
		</div>
	</div>
	<div class="row-fluid divider base_color_background">
		<div class="container">
			<span class="bottom_arrow"></span></div>
	</div>
</div>
<!-- /Search Section -->

<div class="container shadow">
	<span class="bottom_shadow_full"></span>
</div>
</header>
