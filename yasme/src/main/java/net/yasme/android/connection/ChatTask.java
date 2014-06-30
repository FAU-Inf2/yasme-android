package net.yasme.android.connection;

import net.yasme.android.entities.Chat;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.exception.Error;

import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;

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
            this.uri = new URIBuilder(baseURI).setPath("/chat").build();
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

            for (int i = 0; i < jsonArray.length(); i++) {
                Chat chat = new ObjectMapper().readValue((jsonArray.getJSONObject(i)).
                        toString(), Chat.class);
                chats.add(chat);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }

        return chats;
    }

    public void deleteChat(long chatId) throws RestServiceException {

        // remember: only the owner can delete the chat
        executeRequest(Request.DELETE, Long.toString(chatId));
        System.out.println("Chat deleted");
    }

    public Long createChatWithPar(Chat chat) throws RestServiceException {

        try {
            HttpResponse httpResponse = executeRequest(Request.POST, "", chat);
            return Long.parseLong((new BufferedReader(
                    new InputStreamReader(httpResponse.getEntity()
                            .getContent(), "UTF-8")
            )).readLine());
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
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

    public void addParticipantToChat(long participantId, long chatId)
            throws RestServiceException {
        String path = "par/" + participantId + "/" + chatId;
        executeRequest(Request.PUT, path);
        System.out.println("User added to Chat!");
    }

    public void changeOwnerOfChat(long chatId, long newOwnerId) throws RestServiceException {
        String path = chatId + "/owner/" + newOwnerId;
        executeRequest(Request.PUT, path);
        System.out.println("Owner changed");
    }

    public void removePartipantFromChat(long participantId, long chatId)
            throws RestServiceException {
        String path = "par/" + participantId + "/" + chatId;
        executeRequest(Request.DELETE, path);
        System.out.println("User removed from Chat!");
    }

    public void removeOneSelfFromChat(long chatId)
            throws RestServiceException {
        String path = chatId + "/par/self";
        executeRequest(Request.DELETE, path);
        System.out.println("I´m out of Chat No. " + chatId);
    }

    public void updateStatus(Chat chat) throws RestServiceException {
        String path = chat.getId() + "/properties";
        executeRequest(Request.PUT, path, chat);
        System.out.println("Status of chat updated");
    }

    //TODO: implement lastSeen Rest Call
}
