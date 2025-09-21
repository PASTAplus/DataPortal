package edu.lternet.pasta.portal;


import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.lternet.pasta.client.DataPackageManagerClient;
import edu.lternet.pasta.client.PastaAuthenticationException;
import edu.lternet.pasta.client.PastaConfigurationException;
import edu.lternet.pasta.common.IllegalEmlPackageIdException;


public class ThumbnailManagerServlet extends DataPortalServlet  {

    private static final Logger logger = Logger.getLogger(ThumbnailManagerServlet.class);
    private static final String forward = "./thumbnailManager.jsp";

    private String uid;
    private String packageId;
    private static String ediPublicId;

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Pass request on to "doPost".
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession httpSession = request.getSession();
        uid = (String) httpSession.getAttribute("uid");
        if (uid == null || uid.isEmpty()) {
            uid = ediPublicId;
        }

        packageId = request.getParameter("packageid");

        String scope = "";
        String identifier = "";
        String revision = "";
        if (packageId != null && !packageId.isEmpty()) {
            String[] parts = packageId.split("\\.");
            if (parts.length != 3) {
                String msg = String.format("Invalid package id '%s'.", packageId);
                throw new IllegalEmlPackageIdException(msg, packageId);
            }
            scope = parts[0];
            identifier = parts[1];
            revision = parts[2];
        }

        StringBuilder resources = new StringBuilder();
        resources.append("<ul>\n");

        DataPackageManagerClient dpmClient = null;
        try {
            dpmClient = new DataPackageManagerClient(uid);
        } catch (PastaAuthenticationException | PastaConfigurationException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }

        if (dpmClient.hasThumbnail(scope, identifier, revision, null)) {
            String style = "style=\"" +
                    "max-height: 200px; " +
                    "max-width: 400px; " +
                    "height: auto; " +
                    "width: auto; " +
                    "border: 2px solid #5990bd; " +
                    "\"";
            String imageUrl = String.format("%s/thumbnail/eml/%s/%s/%s", dpmClient.getBaseUrl(), scope, identifier, revision);
            String alt = String.format("%s-thumbnail", packageId);
            String thumbnail = String.format(
                    "<a href=\"%s\" class=\"lightbox-trigger\"><img %s src=\"%s\" alt=\"%s\"/></a>\n",
                    imageUrl,
                    style,
                    imageUrl,
                    alt
            );
            resources.append(String.format("<li>%s %s</li>", packageId, thumbnail));
        }
        else {
            resources.append(String.format("<li>%s</li>", packageId));
        }

        Map<String, String> entityMap = null;
        try {
            entityMap = entityMap(scope, identifier, revision);
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException(e);
        }

        for (Map.Entry<String, String> entity : entityMap.entrySet()) {
            String resourceKey = entity.getKey();
            String resourceValue = entity.getValue();
            if (dpmClient.hasThumbnail(scope, identifier, revision, resourceKey)) {
                String style = "style=\"" +
                        "max-height: 50px; " +
                        "max-width: 50px; " +
                        "height: auto; " +
                        "width: auto; " +
                        "\"";
                String imageUrl = String.format("%s/thumbnail/eml/%s/%s/%s/%s", dpmClient.getBaseUrl(), scope, identifier, revision, resourceKey);
                String alt = String.format("%s-thumbnail", packageId);
                String thumbnail = String.format(
                        "<a href=\"%s\" class=\"lightbox-trigger\"><img %s src=\"%s\" alt=\"%s\"/></a>\n",
                        imageUrl,
                        style,
                        imageUrl,
                        alt
                );
                resources.append(String.format("<li>%s %s</li>", resourceValue, thumbnail));
            }
            else {
                resources.append(String.format("<li>%s</li>\n", resourceValue));
            }
        }

        resources.append("</ul>\n");

        request.setAttribute("packageId", packageId);
        request.setAttribute("resources", resources.toString());

        RequestDispatcher requestDispatcher = request.getRequestDispatcher(forward);
        requestDispatcher.forward(request, response);

    }

    public void destroy() {
        super.destroy();
    }

    public void init() throws ServletException {
        PropertiesConfiguration options = ConfigurationListener.getOptions();
        ediPublicId = options.getString("edi.public.id");
    }

    private Map<String, String> entityMap(
            String scope,
            String identifier,
            String revision
    ) throws Exception {

        DataPackageManagerClient dpmClient = new DataPackageManagerClient(uid);
        String dataEntities = dpmClient.listDataEntities(scope, Integer.valueOf(identifier), revision);

        String resourceKey;
        Map<String, String> entities = new LinkedHashMap<>();
        for (String dataEntity : dataEntities.split("\n")) {
            resourceKey = dataEntity;
            String dataEntityName = dpmClient.readDataEntityName(scope, Integer.valueOf(identifier), revision, dataEntity);
            entities.put(resourceKey, dataEntityName);
        }
        return entities;
    }

}
