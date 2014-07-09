package net.yasme.android.connection;

import android.content.Context;
import android.util.Log;

import net.yasme.android.asyncTasks.DeleteMessageKeyTask;
import net.yasme.android.encryption.MessageEncryption;
import net.yasme.android.entities.Message;
import net.yasme.android.entities.User;
import net.yasme.android.exception.Error;
import net.yasme.android.exception.RestServiceException;

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

public class MessageTask extends ConnectionTask {

    private static MessageTask instance;

    public static MessageTask getInstance() {
        if (instance == null) {
            instance = new MessageTask();
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

    public void sendMessage(Message message) throws RestServiceException {
        executeRequest(Request.POST, "", message);
        System.out.println("[DEBUG] Message stored!");
    }

    public ArrayList<Message> getMessage(long lastMessageId)
            throws RestServiceException {

        ArrayList<Message> messages = new ArrayList<Message>();

        try {
            HttpResponse httpResponse = executeRequest(Request.GET, Long.toString(lastMessageId));
            String json = (new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent(), "UTF-8"))).readLine();

            JSONArray jsonArray = new JSONArray(json);

            Log.d(this.getClass().getSimpleName(),
                    "[DEBUG] getMessageRequest successful: " + jsonArray.length() + " new messages");// + json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                JSONObject sender = obj.getJSONObject("sender");
                JSONObject chat = obj.getJSONObject("chat");

                Log.d(this.getClass().getSimpleName(), "Message: " + obj.toString());

                long chatId = chat.getLong("id");
                long keyId = obj.getLong("messageKeyId");

                //System.out.println("Sender: " + sender.toString());

                        /* extracting Keys and save it */
                JSONObject key;
                try {
                    key = obj.getJSONObject("messageKey");
                } catch (Exception e) {
                    key = null;
                }

                if (key != null) {
                    String messageKey = key.getString("messageKey");
                    String iv = key.getString("initVector");
                    //decrypt the key with RSA
                    //TODO: statt userId deviceId uebergeben
                            /*
                            MessageSignatur rsa = new MessageSignatur(context, userId);
                            String messageKey = rsa.decrypt(messageKeyEncrypted);
                            */


                    long timestamp = key.getLong("timestamp");

                    //MessageEncryption keyStorage = new MessageEncryption(context, chatId);

                    //keyStorage.saveKey(obj.getLong("messageKeyId"), messageKey, iv, timestamp);
                    // TODO: storeKeyToDatabase
                            /*DEBUG*/
                    System.out.println("[???] Key " + keyId + " aus den Nachrichten extrahiert und gespeichert");
                            /*DEBUG END*/
                    new DeleteMessageKeyTask().execute(keyId);
                    //keyStorage.deleteKeyFromServer(keyId);
                } else {
                    System.out.println("[???] Es wurde kein Key in der Message gefunden");
                }

                Message msg = new Message(new User(sender.getString("name"),
                        sender.getLong("id")), obj.getString("message"), chatId, keyId);
                messages.add(msg);
                Log.d(this.getClass().getSimpleName(), "Message added: " + msg.getMessage());
            }

        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println("Number new Messages: " + messages.size());
        return messages;
    }
}
