package net.yasme.android.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import net.yasme.android.entities.Device;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by florianwinklmeier on 16.06.14.
 */

public class SearchTask extends ConnectionTask {

    private static SearchTask instance;

    public static SearchTask getInstance() {
        if (instance == null) {
            instance = new SearchTask();
        }
        return instance;
    }

    private SearchTask() {

        try {
            this.uri = new URIBuilder(baseURI).setPath("/search").build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public User userByNumber(String phoneNumber) throws RestServiceException {

        String path = "/userByNumber/" + phoneNumber;
        HttpResponse httpResponse = executeRequest(Request.GET, path);

        User user = null;
        try {
            user = new ObjectMapper().readValue(new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent())).readLine(), User.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return user;
    }

    public User userByMail(String email) throws RestServiceException {

        String path = "/userByMail/" + email;
        HttpResponse httpResponse = executeRequest(Request.GET, path);

        User user = null;
        try {
            user = new ObjectMapper().readValue(new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent())).readLine(), User.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return user;
    }

    public User userByLike(String term) throws RestServiceException {

        String path = "/userByLike/" + term;
        HttpResponse httpResponse = executeRequest(Request.GET, path);

        User user = null;
        try {
            user = new ObjectMapper().readValue(new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent())).readLine(), User.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return user;
    }


    public ArrayList<User> getAllUsers(long userId, String accessToken) throws
            RestServiceException {

        ArrayList<User> users = new ArrayList<User>();

        try {
            HttpResponse httpResponse = executeRequest(Request.GET, "/allUsers");

            JSONArray jsonArray = new JSONArray(new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent())).readLine());

            for (int i = 0; i < jsonArray.length(); i++)
                users.add(new ObjectMapper().readValue((jsonArray.getJSONObject(i)).
                        toString(), User.class));

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return users;
    }

    public ArrayList<Device> getAllDevices() throws
            RestServiceException {

        ArrayList<Device> devices = new ArrayList<Device>();

        try {
            String path = "/allDevices/" + userId;
            HttpResponse httpResponse = executeRequest(Request.GET, path);

            JSONArray jsonArray = new JSONArray(new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent())).readLine());

            for (int i = 0; i < jsonArray.length(); i++)
                devices.add(new ObjectMapper().readValue((jsonArray.getJSONObject(i)).
                        toString(), Device.class));

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return devices;
    }
}

