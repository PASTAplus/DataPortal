package edu.lternet.pasta.portal;

import edu.lternet.pasta.token.TokenManager;
import org.apache.commons.configuration.Configuration;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

@WebFilter("/TokenRefreshFilter")
public class TokenRefreshFilter implements Filter {
    private static final Logger logger = Logger.getLogger(TokenRefreshFilter.class);

    String tokenRefreshUrl = null;
    ServletContext context = null;

    public void init(FilterConfig fConfig) throws ServletException {
        Configuration options = ConfigurationListener.getOptions();
        String authProtocol = options.getString("auth.protocol"); // https
        String authHostname = options.getString("auth.hostname"); // auth.edirepository.org
        Integer authPort = options.getInteger("auth.port", 443); // 443
        this.tokenRefreshUrl = String.format("%s://%s:%d/auth/refresh", authProtocol, authHostname, authPort);
        context = fConfig.getServletContext();
        logger.info("TokenRefreshFilter initialized");
        logger.info("Token refresh URL: " + this.tokenRefreshUrl);
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpSession httpSession = ((HttpServletRequest) request).getSession();
        String uid = (String) httpSession.getAttribute("uid");
        if (uid == null || uid.isEmpty()) {
            uid = "public";
            // logger.debug("Skipped token refresh for public user");
        }

        if (!uid.equals("public")) {
            try {
                refreshToken(uid);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // Pass the request along the filter chain
        chain.doFilter(request, response);
    }

    private void refreshToken(String uid) throws Exception {
        String extToken = getExtToken(uid).get("auth-token");
        if (extToken == null) {
            logger.error("Token not found for user: " + uid);
            return;
        }
        String refreshedExtToken = fetchRefreshedToken(extToken);
        if (refreshedExtToken != null) {
            setExtToken(refreshedExtToken);
            logger.info("Refreshed token for user: " + uid);
        }
    }

    public HashMap<String, String> getExtToken(String uid) throws ClassNotFoundException {
        try {
            return TokenManager.getTokenSet(uid);
        } catch (SQLException e) {
            return null;
        }
    }

    public void setExtToken(String extToken) throws SQLException, ClassNotFoundException {
        HashMap<String, String> tokenSet = new HashMap<String, String>(2);
        tokenSet.put("auth-token", extToken);
        tokenSet.put("edi-token", "");
        TokenManager tokenManager = new TokenManager(tokenSet);
        tokenManager.storeToken();
    }

    public String fetchRefreshedToken(String token) throws Exception {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(this.tokenRefreshUrl);
        httpPost.setEntity(new StringEntity(token, "utf-8"));
        try {
            HttpResponse httpResponse = httpClient.execute(httpPost);
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                logger.error("Error fetching refreshed token: " + statusLine.toString());
                return null;
            }
            HttpEntity httpEntity = httpResponse.getEntity();
            return EntityUtils.toString(httpEntity, "utf-8").trim();
        } catch (Exception e) {
            logger.error("Error fetching refreshed token: " + e.getMessage());
        } finally {
            httpClient.close();
        }

        return null;
    }
}
