package net.yasme.android.connection;

import android.content.Context;
import android.content.SharedPreferences;

import net.yasme.android.connection.ssl.HttpClient;
import net.yasme.android.encryption.MessageEncryption;
import net.yasme.android.entities.Message;
import net.yasme.android.entities.MessageKey;
import net.yasme.android.entities.User;
import net.yasme.android.exception.Error;
import net.yasme.android.exception.MessageError;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.exception.UserError;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class MessageTask extends  ConnectionTask {

    private static MessageTask instance;
    private URI uri;
    private Context context; //necessary for saving Key from Message

    public static MessageTask getInstance(Context context) {
        if (instance == null) {
            instance = new MessageTask(context);
        }
        return instance;
    }

    private MessageTask(Context context) {

        //TODO: URI dynamisch auslesen
        try {
            this.uri = new URIBuilder().setScheme(serverScheme).
                    setHost(serverHost).setPort(serverPort).setPath("/msg").build();
            this.context = context;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public boolean sendMessage(Message message, String accessToken) throws RestServiceException {

        try {
            URI requestURI = uri;

            CloseableHttpClient httpClient = HttpClient.createSSLClient();
            HttpPost httpPost = new HttpPost(requestURI);

            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

            System.out.println(ow.writeValueAsString(message));
            StringEntity se = new StringEntity(ow.writeValueAsString(message));
            httpPost.setEntity(se);

            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("userId", Long.toString(message.getSender().getId()));
            httpPost.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpPost);

            System.out.println(httpResponse.getStatusLine().getStatusCode());

            switch (httpResponse.getStatusLine().getStatusCode()) {
                case 201:
                    System.out.println("Message stored");
                    return true;
                case 401:
                    System.out.println("[DEBUG] Unauthorized");
                    throw new RestServiceException(Error.UNAUTHORIZED);
                case 500:
                    throw new RestServiceException(MessageError.SEND_MESSAGE_FAILED);
                default:
                    throw new RestServiceException(MessageError.ERROR);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
        return false;
    }

    public ArrayList<Message> getMessage(long lastMessageId, long userId, String accessToken)
            throws RestServiceException {

        ArrayList<Message> messages = new ArrayList<Message>();

        try {
            URI requestURI = new URIBuilder(uri).setPath(uri.getPath() + "/" + lastMessageId).build();


            CloseableHttpClient httpClient = HttpClient.createSSLClient();
            HttpGet httpGet = new HttpGet(requestURI);

            httpGet.setHeader("accept", "application/json");
            httpGet.setHeader("userId", Long.toString(userId));
            httpGet.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpGet);

            switch (httpResponse.getStatusLine().getStatusCode()) {

                case 200:
                    String json = (new BufferedReader(new InputStreamReader(
                            httpResponse.getEntity().getContent(), "UTF-8"))).readLine();

                    JSONArray jsonArray = new JSONArray(json);

                    System.out.println("[DEBUG] getMessageRequest successful: " + json);

                    if (jsonArray.length() == 0)
                        throw new RestServiceException(MessageError.GET_NO_NEW_MESSAGE);

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject obj = jsonArray.getJSONObject(i);
                        JSONObject sender = obj.getJSONObject("sender");
                        JSONObject chat = obj.getJSONObject("chat");

                       //TODO: hole richtige Chatid
                       // long chatId = chat.getLong("chatId");
                        long chatId = 1;
                        long keyId = obj.getLong("messageKeyId");

                        System.out.println("Sender: " + sender.toString());

                        messages.add(new Message(new User(sender.getString("name"),
                                sender.getLong("id")), obj.getString("message"), chatId, keyId));

                        /* extracting Keys and save it */
                        JSONObject key;
                        try {
                            key = obj.getJSONObject("messageKey");
                        } catch (Exception e) {
                            key = null;
                        }
                        //JSONObject key = obj.getJSONObject("messageKey");


                        if (key != null){
                            String messageKey = key.getString("messageKey");
                            String[] base64arr = messageKey.split(",");
                            String keyBase64 = base64arr[0];
                            String ivBase64 = base64arr[1];

                            long timestamp = key.getLong("timestamp");

                            MessageEncryption keyStorage = new MessageEncryption(context, chatId);

                            keyStorage.saveKey(obj.getLong("messageKeyId"), keyBase64, ivBase64, timestamp);
                            /*DEBUG*/
                            System.out.println("[???] ein neuer Key wurde aus den Nachrichten extrahiert und gespeichert");
                            /*DEBUG END*/
                            //TODO: hier muss spaeter die DeviceId statt userUd uebergeben werden
                            keyStorage.deleteKeyFromServer(keyId, userId);

                        }


                    }
                    break;
                case 401:
                    System.out.println("[DEBUG] Unauthorized");
                    throw new RestServiceException(Error.UNAUTHORIZED);
                case 500:
                    throw new RestServiceException(UserError.USER_NOT_FOUND);
                default:
                    throw new RestServiceException(Error.ERROR);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        System.out.println("Number new Messages: " + messages.size());
        return messages;
    }


}
