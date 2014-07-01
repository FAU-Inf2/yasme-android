package net.yasme.android.connection;

import android.graphics.drawable.Drawable;

import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.exception.Error;

import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

public class UserTask extends ConnectionTask {

    private static UserTask instance;

    public static UserTask getInstance() {
        if (instance == null) {
            instance = new UserTask();
        }
        return instance;
    }

    private UserTask() {
        try {
            this.uri = new URIBuilder(baseURI).setPath("/usr").build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public Long registerUser(User user) throws RestServiceException {

        try {
            HttpResponse httpResponse = executeRequest(Request.POST, "", user);
            return Long.parseLong(new BufferedReader(new InputStreamReader(httpResponse
                    .getEntity().getContent(), "UTF-8")).readLine());
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
    }

    public void changeUserData(User user) throws RestServiceException {
        executeRequest(Request.PUT, "", user);
        System.out.println("[DEBUG] User data changed");
    }

    public User getUserData() throws RestServiceException {

        try {
            HttpResponse httpResponse = executeRequest(Request.GET, "");
            return new ObjectMapper().readValue(((new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent(), "UTF-8"))).readLine()), User.class);
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
    }

    //TODO: implement method
    public void uploadProfilePicture(Drawable drawable) throws RestServiceException {

        executeRequest(Request.POST, "profile");

    }

    public Drawable getOwnProfilePicture() throws RestServiceException {

        try {
            InputStream stream = (executeRequest(Request.GET, "profile")).getEntity().getContent();
            return Drawable.createFromStream(stream, null);

        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
    }

    public Drawable getProfilePicture(long userId) throws RestServiceException {
        try {
            String path = "profile/" + userId;
            InputStream stream = (executeRequest(Request.GET, path)).getEntity().getContent();
            return Drawable.createFromStream(stream, null);

        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
    }
}
