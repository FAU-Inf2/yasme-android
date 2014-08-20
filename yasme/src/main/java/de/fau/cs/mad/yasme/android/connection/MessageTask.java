package de.fau.cs.mad.yasme.android.connection;

import de.fau.cs.mad.yasme.android.controller.Log;

import de.fau.cs.mad.yasme.android.entities.Message;
import de.fau.cs.mad.yasme.android.exception.Error;
import de.fau.cs.mad.yasme.android.exception.KeyOutdatedException;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.dao.ChatDAO;
import de.fau.cs.mad.yasme.android.storage.dao.MessageKeyDAO;
import de.fau.cs.mad.yasme.android.storage.dao.UserDAO;

import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MessageTask extends ConnectionTask {

    private static MessageTask instance;
    private MessageKeyDAO keyDAO = DatabaseManager.INSTANCE.getMessageKeyDAO();
    private ChatDAO chatDAO = DatabaseManager.INSTANCE.getChatDAO();
    private UserDAO userDAO = DatabaseManager.INSTANCE.getUserDAO();

    public static MessageTask getInstance() {
        if (instance == null) {
            synchronized (MessageTask.class) {
                if (null == instance) {
                    instance = new MessageTask();
                }
            }
        }
        return instance;
    }

    private MessageTask() {
        try {
            this.uri = new URIBuilder(baseURI).setPath(ConnectionTask.APIVERSION + "/msg").build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send the given message to the server. It's id is generated by the server and send back
     *
     * @param message to send
     * @return given message with generated id
     * @throws RestServiceException
     */
    public Message sendMessage(Message message) throws KeyOutdatedException {
        try {
            if (message == null) {
                return null;
            }

            // Send
            HttpResponse httpResponse = executeRequest(Request.POST, "", message);
            Message result = new ObjectMapper().readValue(new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent())).readLine(), Message.class);

            message.setId(result.getId());
            return message;

        } catch (RestServiceException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            if (e.getCode() == Error.OUTDATED.getNumber()) {
                throw new KeyOutdatedException();
            }
        } catch (IOException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
        }
        return null;
    }

    public List<Message> getMessages(long lastMessageId) throws RestServiceException {
        List<Message> messages = new ArrayList<Message>();

        try {
            HttpResponse httpResponse = executeRequest(Request.GET, Long.toString(lastMessageId));
            String json = new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent())).readLine();
            Log.d(getClass().getSimpleName(), "JSON: " + json);
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                Message message = new ObjectMapper().readValue((jsonArray.getJSONObject(i)).
                        toString(), Message.class);
                messages.add(message);
            }
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(this.getClass().getSimpleName(), "Number new Messages: " + messages.size());
        return messages;
    }
}
