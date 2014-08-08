package net.yasme.android.connection;

import android.util.Log;

import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.DatabaseManager;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
            e.printStackTrace();
        }
    }

    public static AuthorizationTask getInstance() {
        if (instance == null) {
            synchronized(AuthorizationTask.class) {
                if (null == instance) {
                    instance = new AuthorizationTask();
                }
            }
        }
        return instance;
    }

    public String[] loginUser(User user) throws RestServiceException {
				Log.d(this.getClass().getSimpleName(),"Logging in..."); //TODO RM
        HttpResponse httpResponse = executeRequest(Request.POST, "in", user);

        Header userID = httpResponse.getFirstHeader("userId");
        Header token = httpResponse.getFirstHeader("Authorization");

        DatabaseManager.INSTANCE.setUserId(Long.parseLong(userID.getValue()));
        DatabaseManager.INSTANCE.setAccessToken(token.getValue());

        Log.d(this.getClass().getSimpleName(),"Login successful!");
        return new String[]{userID.getValue(), token.getValue()};
    }

    public void logoutUser() throws RestServiceException {
        executeRequest(Request.POST, "out");
        Log.d(this.getClass().getSimpleName(),"Signed out successful");
    }

//    public String outdated() throws RestServiceException {
//        HttpResponse response = executeRequest(Request.GET, "sendInfoToClient");
//        String answer = "";
//        try {
//            answer = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
//                    .readLine();
//        } catch (IOException e) {
//            Log.d(this.getClass().getSimpleName(), e.getMessage());
//        }
//        Log.d(this.getClass().getSimpleName(), "is device outdated");
//        return answer;
//    }
}
