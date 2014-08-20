package de.fau.cs.mad.yasme.android.connection;

import de.fau.cs.mad.yasme.android.controller.Log;

import de.fau.cs.mad.yasme.android.entities.DiffieHellmanPart;
import de.fau.cs.mad.yasme.android.exception.Error;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;

import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

/**
 * Created by florianwinklmeier on 16.06.14.
 */
public class DiffieHellmanPartTask extends ConnectionTask {

    private static DiffieHellmanPartTask instance;

    public static DiffieHellmanPartTask getInstance() {
        if (instance == null) {
            synchronized (DiffieHellmanPartTask.class) {
                if (null == instance) {
                    instance = new DiffieHellmanPartTask();
                }
            }
        }
        return instance;
    }

    private DiffieHellmanPartTask() {
        try {
            this.uri = new URIBuilder(baseURI).setPath(ConnectionTask.APIVERSION + "/dh").build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void storeDHPart(DiffieHellmanPart dh) throws RestServiceException {
        executeRequest(Request.POST, "", dh);
        Log.d(this.getClass().getSimpleName(), "DH received");
    }

    public DiffieHellmanPart getNextKey(long devId) throws RestServiceException {

        DiffieHellmanPart dh = null;
        HttpResponse httpResponse = executeRequest(Request.GET, Long.toString(devId));

        try {
            dh = new ObjectMapper().readValue(new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent())).readLine(), DiffieHellmanPart.class);
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }

        return dh;
    }
}
