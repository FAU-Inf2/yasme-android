package de.fau.cs.mad.yasme.android.connection;

import de.fau.cs.mad.yasme.android.controller.Log;

import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;

/**
 * Created by florianwinklmeier on 04.06.14.
 */

public class AuthorizationTask extends ConnectionTask {

    private static AuthorizationTask instance;

    private AuthorizationTask() {
        try {
            this.uri = new URIBuilder(baseURI).setPath(ConnectionTask.APIVERSION + "/sign").build();
        } catch (URISyntaxException e) {
            Log.e(this.getClass().getSimpleName(),e.getMessage());
        }
    }

    public static AuthorizationTask getInstance() {
        if (instance == null) {
            synchronized (AuthorizationTask.class) {
                if (null == instance) {
                    instance = new AuthorizationTask();
                }
            }
        }
        return instance;
    }

    public String[] loginUser(User user) throws RestServiceException {
        Log.d(this.getClass().getSimpleName(), "Logging in..."); //TODO RM
        HttpResponse httpResponse = executeRequest(Request.POST, "in", user);

        Header userID = httpResponse.getFirstHeader("userId");
        Header token = httpResponse.getFirstHeader("Authorization");

        DatabaseManager.INSTANCE.setUserId(Long.parseLong(userID.getValue()));
        DatabaseManager.INSTANCE.setAccessToken(token.getValue());

        Log.d(this.getClass().getSimpleName(), "Login successful!");
        return new String[]{userID.getValue(), token.getValue()};
    }

    public void logoutUser() throws RestServiceException {
        executeRequest(Request.POST, "out");
        Log.d(this.getClass().getSimpleName(), "Signed out successful");
    }
}
