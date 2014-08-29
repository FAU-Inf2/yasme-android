package de.fau.cs.mad.yasme.android.connection;

import de.fau.cs.mad.yasme.android.controller.Log;

import de.fau.cs.mad.yasme.android.entities.MessageKey;
import de.fau.cs.mad.yasme.android.exception.IncompleteKeyException;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;
import de.fau.cs.mad.yasme.android.exception.Error;

import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Created by Martin Sturm <martin@sturms.name> on 08.08.2014.
 * Created by Marco Eberl <mfrankie89@aol.de>
 */

public class MessageKeyTask extends ConnectionTask {

    private static MessageKeyTask instance;


    public static MessageKeyTask getInstance() {
        if (instance == null) {
            synchronized (MessageKeyTask.class) {
                if (null == instance) {
                    instance = new MessageKeyTask();
                }
            }
        }
        return instance;
    }

    private MessageKeyTask() {

        try {
            this.uri = new URIBuilder(baseURI).setPath(ConnectionTask.APIVERSION + "/msgkey").build();
        } catch (URISyntaxException e) {
            Log.e(this.getClass().getSimpleName(),e.getMessage());
        }
    }

    public MessageKey saveKeys(ArrayList<MessageKey> messageKeys) throws IncompleteKeyException {
        try {
            Log.d(this.getClass().getSimpleName(), "Keys sent");
            HttpResponse httpResponse = executeRequest(Request.POST, "", messageKeys);

            Log.d(this.getClass().getSimpleName(), "Analyze Response");
            MessageKey messageKey = new ObjectMapper().readValue(new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent())).readLine(), MessageKey.class);

            return messageKey;
        } catch (RestServiceException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            if (e.getCode() == Error.INCOMPLETE_REQUEST.getNumber()) {
                throw new IncompleteKeyException();
            }
        } catch (IOException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
        }
        return null;
    }

    public boolean deleteKey(long keyId) throws RestServiceException {
        executeRequest(Request.DELETE, String.valueOf(keyId));
        return true;
    }
}

