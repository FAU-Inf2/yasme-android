package de.fau.cs.mad.yasme.android.connection;

import de.fau.cs.mad.yasme.android.controller.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.fau.cs.mad.yasme.android.entities.ServerInfo;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;

import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

/**
 * Created by Martin Sturm <martin.sturm@fau.de> on 08.08.2014.
 */
public class InfoTask extends ConnectionTask {
    private static InfoTask instance;

    private InfoTask() {
        try {
            this.uri = new URIBuilder(baseURI).setPath(ConnectionTask.APIVERSION + "/info").build();
        } catch (URISyntaxException e) {
            Log.e(this.getClass().getSimpleName(),e.getMessage());
        }
    }

    public static InfoTask getInstance() {
        if (instance == null) {
            synchronized (AuthorizationTask.class) {
                if (null == instance) {
                    instance = new InfoTask();
                }
            }
        }
        return instance;
    }


    public ServerInfo getInfo() throws RestServiceException {
        try {
            HttpResponse httpResponse = executeRequest(ConnectionTask.Request.GET, "android/" + language);
            ServerInfo serverInfo = new ObjectMapper().readValue(((new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent(), "UTF-8"))).readLine()), ServerInfo.class);
            return serverInfo;

        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }
}
