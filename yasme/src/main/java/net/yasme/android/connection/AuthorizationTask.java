package net.yasme.android.connection;

import android.util.Log;

import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;

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
            this.uri = new URIBuilder(baseURI).setPath(ConnectionTask.APIVERSION + "/sign").build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public String[] loginUser(User user) throws RestServiceException {
				Log.d(this.getClass().getSimpleName(),"Logging in..."); //TODO RM
        HttpResponse httpResponse = executeRequest(Request.POST, "in", user);

        Header userID = httpResponse.getFirstHeader("userId");
        Header token = httpResponse.getFirstHeader("Authorization");

        Log.d(this.getClass().getSimpleName(),"Login successful!");
        return new String[]{userID.getValue(), token.getValue()};
    }

    public void logoutUser()
            throws RestServiceException {
        executeRequest(Request.POST, "out");
        Log.d(this.getClass().getSimpleName(),"Signed out successful");
    }
}
