package edu.lternet.pasta.portal;

import edu.lternet.pasta.client.PastaAuthenticationException;
import edu.lternet.pasta.client.PastaConfigurationException;
import org.apache.log4j.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import edu.lternet.pasta.client.DataPackageManagerClient;

public class ThumbnailManagerServlet extends DataPortalServlet  {

    private static final Logger logger = Logger.getLogger(ThumbnailManagerServlet.class);
    private static final String forward = "./thumbnailManager.jsp";

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Pass request on to "doPost".
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession httpSession = request.getSession();
        String uid = (String) httpSession.getAttribute("uid");

        if (uid == null || uid.isEmpty()) {
            uid = "EDI-078e6e3cee4f7f2812f150701da9351acb51e089";
        }

        String packageId = request.getParameter("packageid");
        String resources = "";
        try {
            DataPackageManagerClient dpmc = new DataPackageManagerClient(uid);
            if (packageId != null && !packageId.isEmpty()) {
                String[] parts = packageId.split("\\.");
                String scope = parts[0];
                String identifier = parts[1];
                String revision = parts[2];
                resources = dpmc.readDataPackage(scope, Integer.valueOf(identifier), revision);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        request.setAttribute("packageId", packageId);
        request.setAttribute("resources", resources);

        RequestDispatcher requestDispatcher = request.getRequestDispatcher(forward);
        requestDispatcher.forward(request, response);

    }

    public void destroy() {
        super.destroy();
    }

}
