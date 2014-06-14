package net.yasme.android.connection;

import net.yasme.android.connection.ssl.HttpClient;
import net.yasme.android.entities.User;
import net.yasme.android.exception.Error;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.exception.UserError;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class UserTask extends  ConnectionTask {

    private static UserTask instance;
    private URI uri;

    public static UserTask getInstance() {
        if (instance == null) {
            instance = new UserTask();
        }
        return instance;
    }

    private UserTask() {

        //TODO: URI dynamisch auslesen
        try {
            this.uri = new URIBuilder().setScheme(serverScheme).
                    setHost(serverHost).setPort(serverPort).setPath("/usr").build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public Long registerUser(User user) throws RestServiceException {

        URI requestURI = uri;
        try {
            CloseableHttpClient httpClient = HttpClient.createSSLClient();
            HttpPost httpPost = new HttpPost(requestURI);

            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

            StringEntity se = new StringEntity(ow.writeValueAsString(user));
            httpPost.setEntity(se);

            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Accept", "application/json");

            HttpResponse httpResponse = httpClient.execute(httpPost);
            System.out.println(httpResponse.getStatusLine().getStatusCode());

            switch (httpResponse.getStatusLine().getStatusCode()) {
                case 201:
                    return Long.parseLong(new BufferedReader(new InputStreamReader(httpResponse
                            .getEntity().getContent(), "UTF-8")).readLine());
                case 401:
                    System.out.println("[DEBUG] Unauthorized");
                    throw new RestServiceException(Error.UNAUTHORIZED);
                case 500:
                    throw new RestServiceException(UserError.REGISTRATION_FAILED);
                default:
                    throw new RestServiceException(UserError.ERROR);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }


    public boolean changeUserData(long userId, User user, String accessToken) throws RestServiceException {

        URI requestURL = uri;

        try {

            CloseableHttpClient httpClient = HttpClient.createSSLClient();
            HttpPut httpPut = new HttpPut(requestURL);

            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            httpPut.setEntity(new StringEntity(ow.writeValueAsString(user)));

            httpPut.setHeader("accept", "application/json");
            httpPut.setHeader("Content-type", "application/json");

            httpPut.setHeader("userId", Long.toString(userId));
            httpPut.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpPut);

            switch (httpResponse.getStatusLine().getStatusCode()) {

                case 200:
                    System.out.println("[DEBUG] User data changed");
                    return true;
                case 401:
                    System.out.println("[DEBUG] Unauthorized");
                    throw new RestServiceException(Error.UNAUTHORIZED);
                case 500:
                    throw new RestServiceException(UserError.USER_NOT_FOUND);
                default:
                    throw new RestServiceException(UserError.ERROR);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
             throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    public User getUserData(long userId, String accessToken) throws RestServiceException {

        URI requestURL = uri;

        try {
            CloseableHttpClient httpClient = HttpClient.createSSLClient();

            HttpGet httpGet = new HttpGet(requestURL);
            httpGet.setHeader("accept", "application/json");

            httpGet.setHeader("userId", Long.toString(userId));
            httpGet.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpGet);

            switch (httpResponse.getStatusLine().getStatusCode()) {

                case 200:
                    return new ObjectMapper().readValue(((new BufferedReader(new InputStreamReader(
                            httpResponse.getEntity().getContent(), "UTF-8"))).readLine()), User.class);
                case 401:
                    System.out.println("[DEBUG] Unauthorized");
                    throw new RestServiceException(Error.UNAUTHORIZED);
                case 404:
                    throw new RestServiceException(UserError.USER_NOT_FOUND);
                default:
                    throw new RestServiceException(UserError.ERROR);
            }

        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
