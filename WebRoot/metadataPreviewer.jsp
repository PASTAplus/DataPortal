<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet" %>

<%
  final String pageTitle = "Metadata Previewer";
  final String titleText = DataPortalServlet.getTitleText(pageTitle);
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
<script src="js/jquery-1.11.0.min.js" type="text/javascript"></script>
<script src="bootstrap/js/bootstrap68b368b3.js?ver=1" type="text/javascript"></script>
<script type="text/javascript" id="MathJax-script" async
  src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js">
</script>
<!-- Mobile Device CSS -->
<link href="bootstrap/css/bootstrap.css" media="screen" rel="stylesheet" type="text/css">
<link href="bootstrap/css/bootstrap-responsive.css" media="screen" rel="stylesheet" type="text/css">

</head>
<style>
#markdown {
  font-family: Arial, Helvetica, sans-serif;
  border-collapse: collapse;
  width: 100%;
}

#markdown td, #markdown th {
  border: 1px solid #ddd;
  padding: 8px;
}

#markdown tr:nth-child(even){background-color: #f2f2f2;}

#markdown tr:hover {background-color: #ddd;}

#markdown th {
  padding-top: 12px;
  padding-bottom: 12px;
  text-align: left;
  background-color: #C4DCEE;
  color: black;
}
</style>
<body onload="convert();">

<jsp:include page="header.jsp" />

<div class="row-fluid ">
		<div class="container">
			<div class="row-fluid distance_1">
				<div class="box_shadow box_layout">
					<div class="row-fluid">
						<div class="row-fluid">
							<div class="span12">
							  <div class="recent_title">
								  <h2>Metadata Previewer</h2>
							  </div>
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<div class="span12">
		            <div class="content">

        <p>Preview a rendered version of EML in HTML format by browsing to your
           EML file and then selecting <b>Preview</b>.</p>

        <div class="section">
          <form id="metadataPreviewer" name="metadataPreviewer" method="post"
            action="./metadataPreviewer" enctype="multipart/form-data"
          >
            <label  class="labelBold">EML File:</label>
            <input type="file" name="emlfile" accept="application/xml" required="required" />
            <div>
            <input class="btn btn-info btn-default" type="submit" name="upload" value="Preview" />
            <input  class="btn btn-info btn-default"type="reset" name="reset" value="Clear" />
            </div>
          </form>
        </div>

             </div>

		       </div>
		<!-- end of content -->
							  
						  </div>
					</div>
				</div>
			</div>
		</div>
	</div>

		<jsp:include page="footer.jsp" />
		
</div>

<script src="https://cdnjs.cloudflare.com/ajax/libs/showdown/1.0.1/showdown.min.js"></script>

<script type="text/javascript">
	jQuery("#showAll").click(function() {
		jQuery(".collapsible").show();
	});
    
	jQuery("#hideAll").click(function() {
		jQuery(".collapsible").hide();
	});

	jQuery(".toggleButton").click(function() {
		jQuery(this).next(".collapsible").slideToggle("fast");
	});
    
	jQuery(".collapsible").hide();
	
	jQuery("#toggleSummary").next(".collapsible").show();

    function convert() {

        var converter = new showdown.Converter();
        converter.setOption("tables", true)
        const markdowns = document.querySelectorAll("[id=markdown]");

        for (var i = 0; i < markdowns.length; i++) {
            markdown = markdowns[i];
            text = markdown.textContent;
            html = converter.makeHtml(text);
            markdown.innerHTML = html
        }
    }
</script>



</body>

</html>

