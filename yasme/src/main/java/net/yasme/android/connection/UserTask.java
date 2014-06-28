package net.yasme.android.connection;

import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
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
            e.printStackTrace();
        }
        return null;
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
            e.printStackTrace();
        }
        return null;
    }
}
