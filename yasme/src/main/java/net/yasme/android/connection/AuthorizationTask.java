package net.yasme.android.connection;

import net.yasme.android.connection.ssl.HttpClient;
import net.yasme.android.entities.User;
import net.yasme.android.exception.Error;
import net.yasme.android.exception.RestServiceException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by florianwinklmeier on 04.06.14.
 */

public class AuthorizationTask extends ConnectionTask {

    private static AuthorizationTask instance;

    public static AuthorizationTask getInstance() {
        if (instance == null) {
            instance = new AuthorizationTask();
        }
        return instance;
    }

    private AuthorizationTask() {

        try {
            this.uri = new URIBuilder(baseURI).setPath("/sign").build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public String[] loginUser(User user) throws RestServiceException {

        try {
            HttpResponse httpResponse = executeEntityRequest(Request.POST, "in", user);

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
                    throw new RestServiceException(Error.LOGIN_FAILED);
                default:
                    System.out.println("[DEBUG] Login Error");
                    throw new RestServiceException(Error.LOGIN_FAILED);
            }

        } catch (ClientProtocolException e) {
            System.out.println("[DEBUG] Login ClientProtocolException");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("[DEBUG] Login RestServiceException");
            throw new RestServiceException(Error.CONNECTION_ERROR);
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
                    throw new RestServiceException(Error.ERROR);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }
}
