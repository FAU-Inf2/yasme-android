package net.yasme.android.connection;

import net.yasme.android.entities.User;
import net.yasme.android.exception.Error;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.exception.UserError;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UserTask {

    private String url;

    public UserTask(String url) {
        this.url = url.concat("/usr");
    }

    /**
     * registerUser() get as return value an ID which should be saved on the
     * client to use it for all user requests
     *
     * @param user
     * @return userID, which should be stored on the device
     */
    public Long registerUser(User user) throws RestServiceException {

        String requestURL = url;

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
                case 201:
                    return Long.parseLong(new BufferedReader(new InputStreamReader(httpResponse
                            .getEntity().getContent(), "UTF-8")).readLine());
                case 500:
                    throw new RestServiceException(UserError.REGISTRATION_FAILED);
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

    public boolean changeUserData(long userId, User user, String accessToken) throws RestServiceException {

        String requestURL = url;

        try {
            HttpClient httpClient = new DefaultHttpClient();
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
                case 500:
                    throw new RestServiceException(UserError.USER_NOT_FOUND);
                default:
                    throw new RestServiceException(UserError.ERROR);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
        return false;
    }

    public User getUserData(long userId, String accessToken) throws RestServiceException {

        String requestURL = url;

        try {
            HttpClient httpClient = new DefaultHttpClient();

            HttpGet httpGet = new HttpGet(requestURL);
            httpGet.setHeader("accept", "application/json");

            httpGet.setHeader("userId", Long.toString(userId));
            httpGet.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpGet);

            switch (httpResponse.getStatusLine().getStatusCode()) {

                case 200:
                    return new ObjectMapper().readValue(((new BufferedReader(new InputStreamReader(
                            httpResponse.getEntity().getContent(), "UTF-8"))).readLine()), User.class);
                case 404:
                    throw new RestServiceException(UserError.USER_NOT_FOUND);
                default:
                    throw new RestServiceException(UserError.ERROR);
            }

        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
        return null;
    }
}
