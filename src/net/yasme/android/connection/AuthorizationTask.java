package net.yasme.android.connection;

import net.yasme.android.entities.User;
import net.yasme.android.exception.Error;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.exception.UserError;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import java.io.IOException;

/**
 * Created by florianwinklmeier on 04.06.14.
 */
public class AuthorizationTask {

    private String url;

    public AuthorizationTask(String url) {
        this.url = url.concat("/sign");
    }

    public String[] loginUser(User user) throws RestServiceException {

        String requestURL = url.concat("/in");

        try {

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(requestURL);

            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

            StringEntity se = new StringEntity(ow.writeValueAsString(user));
            httpPost.setEntity(se);

            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Accept", "application/json");

            HttpResponse httpResponse = httpClient.execute(httpPost);

            switch (httpResponse.getStatusLine().getStatusCode()) {
                case 200:
                    Header userID = httpResponse.getFirstHeader("userId");
                    Header token = httpResponse.getFirstHeader("Authorization");

                    // DEBUG:
                    System.out.println("Login successful. Your UserID is "
                            + userID.getValue()+".");

                    return new String[]{userID.getValue(), token.getValue()};
                case 401:
                    throw new RestServiceException(UserError.LOGIN_FAILED);
                default:
                    throw new RestServiceException(UserError.ERROR);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }

        return null;
    }

    public boolean logoutUser(long userId, long accessToken) throws RestServiceException {

        String requestURL = url.concat("/out");

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(requestURL);

            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("userId", Long.toString(userId));
            httpPost.setHeader("Authorization", Long.toString(accessToken));

            HttpResponse httpResponse = httpClient.execute(httpPost);

            switch (httpResponse.getStatusLine().getStatusCode()) {
                case 200:
                    // DEBUG:
                    System.out.println("Signed out successful!");
                    return true;
                default:
                    throw new RestServiceException(UserError.LOGOUT_FAILED);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
        return false;
    }
}
