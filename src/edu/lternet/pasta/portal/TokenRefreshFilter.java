package edu.lternet.pasta.portal;

import edu.lternet.pasta.client.PastaClient;
import edu.lternet.pasta.token.TokenManager;

import org.apache.commons.configuration.Configuration;
import org.apache.http.*;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

@WebFilter("/TokenRefreshFilter")
public class TokenRefreshFilter implements Filter {
    private static final Logger logger = Logger.getLogger(TokenRefreshFilter.class);

    String tokenRefreshUrl = null;
    ServletContext context;

    public void init(FilterConfig fConfig) throws ServletException {
        Configuration options = ConfigurationListener.getOptions();
        String authProtocol = options.getString("auth.protocol"); // https
        String authHostname = options.getString("auth.hostname"); // auth.edirepository.org
        Integer authPort = options.getInteger("auth.port", 443); // 443
        this.tokenRefreshUrl = String.format("%s://%s:%d/auth/refresh", authProtocol, authHostname, authPort);
        context = fConfig.getServletContext();
        context.log("TokenRefreshFilter initialized");
        context.log("Token refresh URL: " + this.tokenRefreshUrl);
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpSession httpSession = ((HttpServletRequest) request).getSession();
        String uid = (String) httpSession.getAttribute("uid");
        if (uid == null || uid.isEmpty()) {
            uid = "public";
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
        String extToken = getExtToken(uid);
        String refreshedExtToken = fetchRefreshedToken(extToken);
        if (refreshedExtToken != null) {
            setExtToken(refreshedExtToken);
            context.log("Refreshed token for user: " + uid);
        }
    }

    public String getExtToken(String uid) throws SQLException, ClassNotFoundException {
        return TokenManager.getExtToken(uid);
    }

    public void setExtToken(String extToken) throws SQLException, ClassNotFoundException {
        TokenManager tokenManager = new TokenManager(extToken);
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
                return null;
            }
            HttpEntity httpEntity = httpResponse.getEntity();
            return EntityUtils.toString(httpEntity, "utf-8").trim();
        } catch (Exception e) {
            String errorMsg = String.format("Error fetching refreshed token: %s", e.getMessage());
            context.log(errorMsg);
        } finally {
            httpClient.close();
        }

        return null;
    }
}
