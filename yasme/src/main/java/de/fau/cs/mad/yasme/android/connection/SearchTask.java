package de.fau.cs.mad.yasme.android.connection;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.entities.Device;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.exception.Error;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;

/**
 * Created by Florian Winklmeier <f.winklmeier@t-online.de> on 16.06.14.
 */

public class SearchTask extends ConnectionTask {

    private static SearchTask instance;

    public static SearchTask getInstance() {
        if (instance == null) {
            synchronized (SearchTask.class) {
                if (null == instance) {
                    instance = new SearchTask();
                }
            }
        }
        return instance;
    }

    private SearchTask() {

        try {
            this.uri = new URIBuilder(baseURI).setPath(ConnectionTask.APIVERSION + "/search").build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public User userByNumber(String phoneNumber) throws RestServiceException {

        String path = "userByNumber/" + phoneNumber;
        HttpResponse httpResponse = executeRequest(Request.GET, path);

        try {
            return new ObjectMapper().readValue(new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent())).readLine(), User.class);
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
    }

    public User userByMail(String email) throws RestServiceException {

        String path = "userByMail/" + email;
        HttpResponse httpResponse = executeRequest(Request.GET, path);

        try {
            return new ObjectMapper().readValue(new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent())).readLine(), User.class);
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
    }

    public User userById(String id) throws RestServiceException {

        String path = "userById/" + id; //TODO maybe change path
        HttpResponse httpResponse = executeRequest(Request.GET, path);

        try {
            return new ObjectMapper().readValue(new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent())).readLine(), User.class);
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
    }

    public List<User> userByLike(String term) throws RestServiceException {

        ArrayList<User> users = new ArrayList<User>();

        String path = "userByLike/" + term;
        HttpResponse httpResponse = executeRequest(Request.GET, path);

        try {
            JSONArray jsonArray = new JSONArray(new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent())).readLine());

            for (int i = 0; i < jsonArray.length(); i++) {
                users.add(new ObjectMapper().readValue((jsonArray.getJSONObject(i)).
                        toString(), User.class));
            }
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (JSONException je) {
            Log.e(this.getClass().getSimpleName(), je.getMessage());
        }
        return users;
    }


    public ArrayList<User> getAllUsers() throws
            RestServiceException {

        ArrayList<User> users = new ArrayList<User>();

        try {
            HttpResponse httpResponse = executeRequest(Request.GET, "allUsers");

            JSONArray jsonArray = new JSONArray(new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent())).readLine());

            for (int i = 0; i < jsonArray.length(); i++)
                users.add(new ObjectMapper().readValue((jsonArray.getJSONObject(i)).
                        toString(), User.class));

        } catch (JSONException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
        return users;
    }

    public ArrayList<Device> getAllDevices() throws
            RestServiceException {

        ArrayList<Device> devices = new ArrayList<Device>();

        try {
            long userId = DatabaseManager.INSTANCE.getUserId();
            String path = "allDevices/" + userId;
            HttpResponse httpResponse = executeRequest(Request.GET, path);

            JSONArray jsonArray = new JSONArray(new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent())).readLine());

            for (int i = 0; i < jsonArray.length(); i++)
                devices.add(new ObjectMapper().readValue((jsonArray.getJSONObject(i)).
                        toString(), Device.class));

        } catch (JSONException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
        return devices;
    }
}

