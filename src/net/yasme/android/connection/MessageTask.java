package net.yasme.android.connection;

import net.yasme.android.entities.Message;
import net.yasme.android.entities.User;
import net.yasme.android.exception.Error;
import net.yasme.android.exception.MessageError;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.exception.UserError;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MessageTask {

    private String url;

    public MessageTask(String url) {
        this.url = url.concat("/msg");
    }

    public boolean sendMessage(Message message, String accessToken) throws RestServiceException {

        String requestURL = url;

        try {

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(requestURL);

            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

            StringEntity se = new StringEntity(ow.writeValueAsString(message));
            httpPost.setEntity(se);

            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("userId", Long.toString(message.getSender().getId()));
            httpPost.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpPost);

            switch (httpResponse.getStatusLine().getStatusCode()) {
                case 201:
                    System.out.println("Message stored");
                    return true;
                case 401:
                    System.out.println("Unauthorized");
                    throw new RestServiceException(MessageError.SEND_MESSAGE_FAILED);
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

        String requestURL = url.concat("/" + lastMessageId);
        ArrayList<Message> messages = new ArrayList<Message>();

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(requestURL);

            httpGet.setHeader("accept", "application/json");
            httpGet.setHeader("userId", Long.toString(userId));
            httpGet.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpGet);

            switch (httpResponse.getStatusLine().getStatusCode()) {

                case 200:
                    String json = (new BufferedReader(new InputStreamReader(
                            httpResponse.getEntity().getContent(), "UTF-8"))).readLine();

                    JSONArray jsonArray = new JSONArray(json);

                    System.out.println("[DEBUG] getMessageRequest successful");

                    if (jsonArray.length() == 0)
                        throw new RestServiceException(MessageError.GET_NO_NEW_MESSAGE);

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject obj = jsonArray.getJSONObject(i);
                        JSONObject sender = obj.getJSONObject("sender");

                        messages.add(new Message(new User(sender.getString("name"),
                                sender.getLong("id")), obj.getString("message"), 1, obj.getLong("keyID")));
                    }
                    break;
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
        }

        System.out.println("Number new Messages: " + messages.size());
        return messages;
    }
}
