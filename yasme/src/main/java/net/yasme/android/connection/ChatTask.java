package net.yasme.android.connection;

import android.util.Log;

import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Device;
import net.yasme.android.exception.Error;
import net.yasme.android.exception.RestServiceException;

import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.codehaus.jackson.map.ObjectMapper;
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
 * Created by florianwinklmeier on 03.06.14.
 */

public class ChatTask extends ConnectionTask {

    private static ChatTask instance;

    public static ChatTask getInstance() {
        if (instance == null) {
            instance = new ChatTask();
        }
        return instance;
    }

    private ChatTask() {

        try {
            this.uri = new URIBuilder(baseURI).setPath(ConnectionTask.APIVERSION + "/chat").build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public List<Chat> getAllChatsForUser() throws RestServiceException {

        List<Chat> chats = new ArrayList<Chat>();
        try {
            HttpResponse httpResponse = executeRequest(Request.GET, "");
            JSONArray jsonArray = new JSONArray(new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent())).readLine());

            //for (int i = 0; i < jsonArray.length(); i++) {
            //    Log.d(this.getClass().getSimpleName(), jsonArray.optString(i));
            //}
            //Log.e(this.getClass().getSimpleName(), "arraylength " + jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                Chat chat = new ObjectMapper().readValue((jsonArray.getJSONObject(i)).
                        toString(), Chat.class);
                chats.add(chat);
                //Log.e(this.getClass().getSimpleName(), "chat " + i + " " + chat.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }

        return devices;
    }

    public void deleteChat(long chatId) throws RestServiceException {

        // remember: only the owner can delete the chat
        executeRequest(Request.DELETE, Long.toString(chatId));
        Log.d(this.getClass().getSimpleName(),"Chat deleted");
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
            return  new ObjectMapper().readValue(new BufferedReader(new InputStreamReader(httpResponse.getEntity()
                    .getContent())).readLine(), Chat.class);
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
    }

    public void addParticipantToChat(long participantId, long chatId)
            throws RestServiceException {
        String path = "par/" + participantId + "/" + chatId;
        executeRequest(Request.PUT, path);
        Log.d(this.getClass().getSimpleName(),"User added to Chat!");
    }

    public void changeOwnerOfChat(long chatId, long newOwnerId) throws RestServiceException {
        String path = chatId + "/owner/" + newOwnerId;
        executeRequest(Request.PUT, path);
        Log.d(this.getClass().getSimpleName(),"Owner changed");
    }

    public void removePartipantFromChat(long participantId, long chatId)
            throws RestServiceException {
        String path = "par/" + participantId + "/" + chatId;
        executeRequest(Request.DELETE, path);
        Log.d(this.getClass().getSimpleName(),"User removed from Chat!");
    }

    public void removeOneSelfFromChat(long chatId)
            throws RestServiceException {
        String path = chatId + "/par/self";
        executeRequest(Request.DELETE, path);
        Log.d(this.getClass().getSimpleName(),"I´m out of Chat No. " + chatId);
    }

    public void updateStatus(Chat chat) throws RestServiceException {
        String path = chat.getId() + "/properties";
        executeRequest(Request.PUT, path, chat);
        Log.d(this.getClass().getSimpleName(),"Status of chat updated");
    }

    //TODO: implement lastSeen Rest Call
}
