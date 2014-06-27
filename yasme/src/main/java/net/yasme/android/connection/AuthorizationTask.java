package net.yasme.android.connection;

import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
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

        HttpResponse httpResponse = executeRequest(Request.POST, "in", user);

        Header userID = httpResponse.getFirstHeader("userId");
        Header token = httpResponse.getFirstHeader("Authorization");

        System.out.println("[DEBUG] Login successful!");
        return new String[]{userID.getValue(), token.getValue()};
    }

    public void logoutUser(long userId, String accessToken)
            throws RestServiceException {
        executeRequest(Request.POST, "out");
        System.out.println("[DEBUG] Signed out successful");
    }
}
