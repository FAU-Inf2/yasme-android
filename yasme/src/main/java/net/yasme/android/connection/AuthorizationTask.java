package net.yasme.android.connection;

import net.yasme.android.connection.ssl.HttpClient;
import net.yasme.android.entities.User;
import net.yasme.android.exception.Error;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.exception.UserError;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by florianwinklmeier on 04.06.14.
 */

public class AuthorizationTask extends  ConnectionTask{

    private static AuthorizationTask instance;
    private URI uri;

    public static AuthorizationTask getInstance() {
        if (instance == null) {
            instance = new AuthorizationTask();
        }
        return instance;
    }

    private AuthorizationTask() {

        try {
            this.uri = new URIBuilder().setScheme(serverScheme).
                    setHost(serverHost).setPort(serverPort).setPath("/sign").build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public String[] loginUser(User user) throws RestServiceException {

        try {
            URI requestURI = new URIBuilder(uri).setPath(uri.getPath() + "/in").build();

            CloseableHttpClient httpClient = HttpClient.createSSLClient();
            HttpPost httpPost = new HttpPost(requestURI);

            ObjectWriter ow = new ObjectMapper().writer()
                    .withDefaultPrettyPrinter();

            StringEntity se = new StringEntity(ow.writeValueAsString(user));
            httpPost.setEntity(se);

            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Accept", "application/json");

            HttpResponse httpResponse = httpClient.execute(httpPost);

            System.out.println(httpResponse.getStatusLine().getStatusCode());
            switch (httpResponse.getStatusLine().getStatusCode()) {

                case 200:
                    Header userID = httpResponse.getFirstHeader("userId");
                    Header token = httpResponse.getFirstHeader("Authorization");

                    // DEBUG:
                    System.out.println("[DEBUG] Login successful - UserId: "
                            + userID.getValue());
                    return new String[]{userID.getValue(), token.getValue()};

                case 401:
                    System.out.println("[DEBUG] Unauthorized");
                    throw new RestServiceException(Error.UNAUTHORIZED);

                default:
                    throw new RestServiceException(UserError.ERROR);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean logoutUser(long userId, String accessToken)
            throws RestServiceException {

        try {
            URI requestURI = new URIBuilder(uri).setPath(uri.getPath() + "/out").build();

            CloseableHttpClient httpClient = HttpClient.createSSLClient();
            HttpPost httpPost = new HttpPost(requestURI);

            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("userId", Long.toString(userId));
            httpPost.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpPost);

            switch (httpResponse.getStatusLine().getStatusCode()) {
                case 200:
                    System.out.println("[DEBUG] Signed out successful");
                    return true;
                case 401:
                    System.out.println("[DEBUG] Unauthorized");
                    throw new RestServiceException(Error.UNAUTHORIZED);
                default:
                    throw new RestServiceException(UserError.ERROR);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return false;
    }
}
