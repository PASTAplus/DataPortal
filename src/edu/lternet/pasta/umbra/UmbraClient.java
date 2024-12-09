package edu.lternet.pasta.umbra;

import edu.lternet.pasta.client.PastaConfigurationException;
import edu.lternet.pasta.portal.ConfigurationListener;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.owasp.encoder.Encode;
import java.io.IOException;


/**
 * The UmbraClient class provides an interface to the EDI Umbra REST API service.
 *
 * @author Mark Servilla
 */
public class UmbraClient {

    private static final Logger logger = Logger.getLogger(edu.lternet.pasta.umbra.UmbraClient.class);

    protected String umbraCreatorsUrlHead = null;

    public UmbraClient() throws PastaConfigurationException {

        PropertiesConfiguration options = ConfigurationListener.getOptions();

        if (options == null) {
          throw new PastaConfigurationException();
        }

        this.umbraCreatorsUrlHead = options.getString("umbra.creators.urlHead");

    }

    /**
     * Returns a String array of standardized names in "surname, givename" format.
     *
     * @return the String array of names
     * @throws Exception
     */
    public String[] getNames() throws UmbraClientException, IOException {

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String url = this.umbraCreatorsUrlHead + "/names";
		HttpGet httpGet = new HttpGet(url);
		String[] names = null;

		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                HttpEntity httpEntity = httpResponse.getEntity();
                String entityString = EntityUtils.toString(httpEntity);
                JSONArray jsonArray = new JSONArray(entityString);
                int jsonArrayLength = jsonArray.length();
                names = new String[jsonArrayLength];
                for (int i = 0; i < jsonArray.length(); i++) {
                    names[i] = jsonArray.getString(i);
                }
            }
            else {
                String gripe = String.format("Error occurred when retrieving names - host: %s, status: %s",
                        url, statusCode);
                throw new UmbraClientException(gripe);
            }
		} finally {
			httpClient.close();
		}

        return names;

    }

    /**
     * Returns list of name variants for the give name.
     *
     * @param name
     * @return list of name variants
     * @throws UmbraClientException
     * @throws IOException
     */

    public String[] getNameVariants(String name) throws UmbraClientException, IOException {
        String[] names = null;

        if (name != null && !name.isEmpty()) {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            String url = this.umbraCreatorsUrlHead + "/name_variants/" + Encode.forUriComponent(name);
            HttpGet httpGet = new HttpGet(url);
            try {
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    HttpEntity httpEntity = httpResponse.getEntity();
                    String entityString = EntityUtils.toString(httpEntity);
                    JSONArray jsonArray = new JSONArray(entityString);
                    int jsonArrayLength = jsonArray.length();
                    names = new String[jsonArrayLength];
                    for (int i = 0; i < jsonArray.length(); i++) {
                        names[i] = jsonArray.getString(i);
                    }
                }
                else {
                    String gripe = String.format("Error occurred when retrieving names - host: %s, status: %s",
                            url, statusCode);
                    throw new UmbraClientException(gripe);
                }
            } finally {
                httpClient.close();
            }
        }

        return names;

    }
}
