package net.yasme.android.connection;

import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.exception.UserError;
import org.apache.http.Header;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UserTask {

    private String url;

    public UserTask(String url) {
        this.url = url;
    }

    /**
     * registerUser() get as return value an ID which should be saved on the
     * client to use it for all user requests
     *
     * @param user
     * @return userID, which should be stored on the device
     */
    public String registerUser(User user) throws RestServiceException {

        String requestURL = url.concat("/usr");

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
                    return (new BufferedReader(new InputStreamReader(httpResponse
                            .getEntity().getContent(), "UTF-8"))).readLine();
                case 500:
                    throw new RestServiceException(UserError.REGISTRATION_FAILED);
                default:
                    throw new RestServiceException(UserError.ERROR);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(UserError.REGISTRATION_FAILED);
        }
        return null;
    }

    public String[] loginUser(User user) throws RestServiceException {

        String requestURL = url.concat("/sign/in");

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
                            + userID.getValue());

                    return new String[]{userID.getValue(), token.getValue()};
                case 401:
                    throw new RestServiceException(UserError.LOGIN_FAILED);
                default:
                    throw new RestServiceException(UserError.ERROR);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(UserError.LOGIN_FAILED);
        }

        return null;
    }

    public boolean changeUserData(long id, User user) throws RestServiceException {

        String requestURL = url.concat("/usr" + id);

        try {

            HttpClient httpClient = new DefaultHttpClient();
            HttpPut httpPut = new HttpPut(requestURL);

            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

            StringEntity se = new StringEntity(ow.writeValueAsString(user));
            httpPut.setEntity(se);

            httpPut.addHeader("accept", "application/json");
            httpPut.setHeader("Content-type", "application/json");

            HttpResponse httpResponse = httpClient.execute(httpPut);

            switch (httpResponse.getStatusLine().getStatusCode()) {

                case 200:
                    return true;
                case 500:
                    throw new RestServiceException(UserError.USER_NOT_FOUND);
                default:
                    throw new RestServiceException(UserError.ERROR);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(UserError.ERROR);
        }
        return false;
    }

    public User getUserData(long userId) throws RestServiceException {

        String requestURL = url.concat("/usr/" + userId);

        User user = null;

        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(requestURL);
            httpGet.addHeader("accept", "application/json");

            HttpResponse httpResponse = httpClient.execute(httpGet);

            switch (httpResponse.getStatusLine().getStatusCode()) {

                case 200:
                    JSONObject json = new JSONObject(((new BufferedReader(new InputStreamReader(
                            httpResponse.getEntity().getContent(), "UTF-8"))).readLine()));

                    return new User(null, json.getString("name"),
                            json.getString("email"));
                case 404:
                    throw new RestServiceException(UserError.USER_NOT_FOUND);
                default:
                    throw new RestServiceException(UserError.ERROR);
            }

        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(UserError.ERROR);
        }

        return user;
    }
}
