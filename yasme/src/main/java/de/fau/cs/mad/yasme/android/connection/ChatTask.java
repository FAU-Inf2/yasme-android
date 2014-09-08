package de.fau.cs.mad.yasme.android.connection;

import de.fau.cs.mad.yasme.android.controller.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.fau.cs.mad.yasme.android.entities.Chat;
import de.fau.cs.mad.yasme.android.entities.Device;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.exception.Error;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.dao.ChatDAO;

import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Florian Winklmeier <f.winklmeier@t-online.de> on 03.06.14.
 */

public class ChatTask extends ConnectionTask {

    private static ChatTask instance;

    private ChatTask() {

        try {
            this.uri = new URIBuilder(baseURI).setPath(ConnectionTask.APIVERSION + "/chat").build();
        } catch (URISyntaxException e) {
            Log.e(this.getClass().getSimpleName(),e.getMessage());
        }
    }

    public static ChatTask getInstance() {
        if (instance == null) {
            synchronized (ChatTask.class) {
                if (null == instance) {
                    instance = new ChatTask();
                }
            }
        }
        return instance;
    }

    public List<Chat> getAllChatsForUser() throws RestServiceException {

        List<Chat> chats = new ArrayList<Chat>();
        try {
            HttpResponse httpResponse = executeRequest(Request.GET, "");

            String json = new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent())).readLine();
            JSONArray jsonArray = new JSONArray(json);
            Log.d(this.getClass().getSimpleName(), json);

            for (int i = 0; i < jsonArray.length(); i++) {
                Chat chat = new ObjectMapper().readValue((jsonArray.getJSONObject(i)).
                        toString(), Chat.class);
                chats.add(chat);
            }
        } catch (JSONException e) {
            Log.e(this.getClass().getSimpleName(),e.getMessage());
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }

        return chats;
    }

    public List<Device> getAllDevicesForChat(Long chatId) throws RestServiceException {
        String path = chatId + "/devices";
        List<Device> devices = new ArrayList<Device>();
        try {
            HttpResponse httpResponse = executeRequest(Request.GET, path);
            JSONArray jsonArray = new JSONArray(new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent())).readLine());

            for (int i = 0; i < jsonArray.length(); i++) {
                Device device = new ObjectMapper().readValue((jsonArray.getJSONObject(i)).
                        toString(), Device.class);
                devices.add(device);
            }
        } catch (JSONException e) {
            Log.e(this.getClass().getSimpleName(),e.getMessage());
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }

        return devices;
    }

    public void deleteChat(long chatId) throws RestServiceException {

        // remember: only the owner can delete the chat
        executeRequest(Request.DELETE, Long.toString(chatId));
        Log.d(this.getClass().getSimpleName(), "Chat deleted");
    }

    public Long createChatWithPar(Chat chat) throws RestServiceException {

        try {
            HttpResponse httpResponse = executeRequest(Request.POST, "", chat);

            JSONObject json = new JSONObject((new BufferedReader(
                    new InputStreamReader(httpResponse.getEntity()
                            .getContent(), "UTF-8")
            )).readLine());
            return Long.parseLong(json.getString("message"));

        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (JSONException e) {
            throw new RestServiceException(Error.ERROR);
        }
    }

    public Chat getInfoOfChat(long chatId) throws RestServiceException {

        // note: only a participant of the chat shall get the chat object
        try {
            String path = chatId + "/info";
            HttpResponse httpResponse = executeRequest(Request.GET, path);
            return new ObjectMapper().readValue(new BufferedReader(new InputStreamReader(httpResponse.getEntity()
                    .getContent())).readLine(), Chat.class);
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
    }

    public void addParticipantToChat(long participantId, long chatId) throws RestServiceException {
        String path = "par/" + participantId + "/" + chatId;
        HttpResponse httpResponse = executeRequest(Request.PUT, path);
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				if(statusCode != 200) {
					Log.e(this.getClass().getSimpleName(), "User " + participantId + "could not be added!");
					return;
				}
				ChatDAO cDao = DatabaseManager.INSTANCE.getChatDAO();
				Chat chat = cDao.get(chatId);
				for(User u : chat.getParticipants()) Log.d("XXXXXXXXXXXXXXXXXXX","User: " + u.getName());
				Log.d("XXXXXXXXXXXXXX","Now adding " + participantId);
				//chat.addParticipant(DatabaseManager.INSTANCE.getUserDAO().get(participantId));
				for(User u : chat.getParticipants()) Log.d("XXXXXXXXXXXXXXXXXXX","User: " + u.getName());
        Log.d(this.getClass().getSimpleName(), "User " + participantId + " added to Chat!");
    }

    public void changeOwnerOfChat(long chatId, long newOwnerId) throws RestServiceException {
        String path = chatId + "/owner/" + newOwnerId;
        executeRequest(Request.PUT, path);
        Log.d(this.getClass().getSimpleName(), "Owner changed");
    }

    public void removeParticipantFromChat(long participantId, long chatId)
            throws RestServiceException {
        String path = "par/" + participantId + "/" + chatId;
        executeRequest(Request.DELETE, path);
        Log.d(this.getClass().getSimpleName(), "User removed from Chat!");
    }

    public void removeOneSelfFromChat(long chatId)
            throws RestServiceException {
        String path = chatId + "/par/self";
        executeRequest(Request.DELETE, path);
        Log.d(this.getClass().getSimpleName(), "I´m out of Chat No. " + chatId);
    }

    public void updateChat(Chat chat) throws RestServiceException {
        // Copy the chat object first
        Chat clone = chat.clone();
        if (null == clone) {
            Log.e(this.getClass().getSimpleName(), "Cannot clone chat");
            return;
        }

        String path = clone.getId() + "/properties";
        clone.setId(-1); // Tell the server not to overwrite the properties with server data
        executeRequest(Request.PUT, path, clone);
        Log.d(this.getClass().getSimpleName(), "Chat updated");
    }
}
