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

<%@ page import="java.time.LocalDate" %>
<%@ page import="edu.lternet.pasta.portal.DataPackageSurvey" pageEncoding="UTF-8" %>

<%

DataPackageSurvey dps = new DataPackageSurvey();
String[] recentInserts = dps.surveyDataPackages("recentInserts", 2);
String[] recentUpdates = dps.surveyDataPackages("recentUpdates", 2);

String recentScope1 = recentInserts[0];
String recentIdentifier1 = recentInserts[1];
String recentTitle1 = recentInserts[2];
String recentDate1 = recentInserts[3];

String recentScope2 = recentInserts[4];
String recentIdentifier2 = recentInserts[5];
String recentTitle2 = recentInserts[6];
String recentDate2 = recentInserts[7];

String recentScope3 = recentUpdates[0];
String recentIdentifier3 = recentUpdates[1];
String recentTitle3 = recentUpdates[2];
String recentDate3 = recentUpdates[3];

String recentScope4 = recentUpdates[4];
String recentIdentifier4 = recentUpdates[5];
String recentTitle4 = recentUpdates[6];
String recentDate4 = recentUpdates[7];

String icon1 = "";
String icon2 = "";
String icon3 = "";
String icon4 = "";

String title1 = "";
String title2 = "";
String title3 = "";
String title4 = "";

String date1 = "";
String date2 = "";
String date3 = "";
String date4 = "";

String iconLink = "<a href='./mapbrowse?scope=%s&identifier=%s'><span class='post_icon'></span></a>";
String titleLink = "<a href='./mapbrowse?scope=%s&identifier=%s'>%s</a>%s";
String spacePadding = "";
int minLength = 70;

if (recentScope1 != null && !recentScope1.equals("")) {
  icon1 = String.format(iconLink, recentScope1,  recentIdentifier1);
  spacePadding = DataPackageSurvey.spacePadding(recentTitle1, minLength);
  title1 = String.format(titleLink, recentScope1, recentIdentifier1, recentTitle1, spacePadding);
  date1 = recentDate1;
}

if (recentScope2 != null && !recentScope2.equals("")) {
  icon2 = String.format(iconLink, recentScope2,  recentIdentifier2);
  spacePadding = DataPackageSurvey.spacePadding(recentTitle2, minLength);
  title2 = String.format(titleLink, recentScope2, recentIdentifier2, recentTitle2, spacePadding);
  date2 = recentDate2;
}

if (recentScope3 != null && !recentScope3.equals("")) {
  icon3 = String.format(iconLink, recentScope3, recentIdentifier3);
  spacePadding = DataPackageSurvey.spacePadding(recentTitle3, minLength);
  title3 = String.format(titleLink, recentScope3, recentIdentifier3, recentTitle3, spacePadding);
  date3 = recentDate3;
}

if (recentScope4 != null && !recentScope4.equals("")) {
  icon4 = String.format(iconLink, recentScope4,  recentIdentifier4);
  spacePadding = DataPackageSurvey.spacePadding(recentTitle4, minLength);
  title4 = String.format(titleLink, recentScope4,  recentIdentifier4, recentTitle4, spacePadding);
  date4 = recentDate4;
}

LocalDate ld = LocalDate.now();
int year = ld.getYear();

%>

	<!-- Divider -->
	<div class="footers row-fluid pull-left distance_1">
		<div class="row-fluid ">
			<div class="span12 page_top_header line-divider">
			</div>
		</div>

	<!-- /Footer -->
    <footer class="row-fluid ">
      <div class="row-fluid">
				<div class="span12">
					<div class="container">
						<div class="row-fluid">
							<div class="span5">
								<div class="widget widget_recent_posts">
									<div class="footer_title">
										<h2 class="widget-title">Recently Added</h2>
									</div>
									<dl>
										<dt><%= icon1 %></dt>
										<dd class="without_avatar"><%= date1 %><br/><%= title1 %></dd>
									</dl>
									<dl>
										<dt><%= icon2 %></dt>
										<dd class="without_avatar"><%= date2 %><br/><%= title2 %></dd>
									</dl>
								</div>
							</div>
							<div class="span5">
								<div class="widget widget_recent_posts">
									<div class="footer_title">
										<h2 class="widget-title">Recently Updated</h2>
									</div>
									<dl>
										<dt><%= icon3 %></dt>
										<dd class="without_avatar"><%= date3 %><%= title3 %></dd>
									</dl>
									<dl>
										<dt><%= icon4 %></dt>
										<dd class="without_avatar"><%= date4 %><%= title4 %></dd>
									</dl>
								</div>
							</div>
						</div>
					</div>
				</div>
		  </div>
    </footer>
  <!-- /Footer -->
      
    <div class="row-fluid base_color_background footer_copyright">
			<div class="span12">
				<div class="container">
					<span class="arrow row-fluid"><span class="span12"></span>
					</span>
					<div class="row-fluid">
						<div class="span12 ">
							Copyright <%= year %> <a href="http://edirepository.org/">Environmental Data Initiative</a>.
							This material is based upon work supported by the National Science Foundation under grants
                            <a href="https://www.nsf.gov/awardsearch/showAward?AWD_ID=2223103&HistoricalAwards=false">#2223103</a>
                            and <a href="https://www.nsf.gov/awardsearch/showAward?AWD_ID=2223104&HistoricalAwards=false">#2223104</a>.
                            Any opinions, findings, conclusions, or recommendations
							expressed in the material are those of the author(s) and do not necessarily reflect the views of 
							the National Science Foundation. Please
							<a href="mailto:info@edirepository.org">contact us</a>
							with questions, comments, or for technical assistance regarding this web site or the 
							Environmental Data Initiative. Please read our
							<a href="https://edirepository.org/about/edi-policy#privacy-policy">privacy policy</a>
							to know what information we collect about you and to understand your privacy rights.
							<br/><br/>
						</div>
					</div>
				</div>
			</div>
		</div>
    	<div class="row-fluid">
			<div class="span12">
				<div class="container nis-logos-div">
                    <p>EDI is a collaboration between the University of New Mexico and the University of Wisconsin – Madison, Center for Limnology:</p>
                    <a href="https://unm.edu"><img id="unm-img" class="nis-logos-img" alt="UNM logo" src="images/unm-logo.png" title="UNM logo"></a>
					<a href="https://wisc.edu/"><img id="uwm-img" class="nis-logos-img" alt="UW-M logo" src="images/uwm-logo.png" title="UW-M logo"></a>
                </div>
			</div>
		</div>
	</div>
  <!-- /Divider -->
