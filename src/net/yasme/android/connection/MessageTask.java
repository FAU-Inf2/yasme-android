package net.yasme.android.connection;

import net.yasme.android.entities.Message;
import net.yasme.android.entities.User;
import net.yasme.android.exception.MessageError;
import net.yasme.android.exception.RestServiceException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
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

    public boolean sendMessage(Message message) throws RestServiceException {

        String requestURL = url;

        try {

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(requestURL);

            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

            StringEntity se = new StringEntity(ow.writeValueAsString(message));
            httpPost.setEntity(se);

            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Accept", "application/json");

            HttpResponse httpResponse = httpClient.execute(httpPost);

            switch (httpResponse.getStatusLine().getStatusCode()) {
                case 201:
                    System.out.println("Message stored");
                    return true;
                case 500:
                    throw new RestServiceException(MessageError.SEND_MESSAGE_FAILED);
                default:
                    throw new RestServiceException(MessageError.ERROR);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(MessageError.SEND_MESSAGE_FAILED);
        }

        return false;
    }

    public ArrayList<Message> getMessage(long lastMessageId, long userId)
            throws RestServiceException {

        String requestURL = url.concat("/" + lastMessageId + "/" + userId);
        ArrayList<Message> messages = new ArrayList<Message>();

        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(requestURL);

            httpGet.addHeader("accept", "application/json");
            HttpResponse httpResponse = httpClient.execute(httpGet);

            switch (httpResponse.getStatusLine().getStatusCode()) {

                default:
                    String json = (new BufferedReader(new InputStreamReader(
                            httpResponse.getEntity().getContent(), "UTF-8"))).readLine();

                    /*
                    System.out.println(json);

                    ObjectMapper mapper = new ObjectMapper();

                    List<Message> messages = mapper.readValue(json, mapper.getTypeFactory().
                            constructCollectionType(ArrayList.class, Message.class));

                    return (ArrayList) messages;


                System.out.println("[???]: JSON to Client: " + json);

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        httpResponse.getEntity().getContent()));

                String json = reader.readLine();

                /****************** Debug*Output ********************************/

                    // messages.add(new Message(1, 1, json));
                    // messages.add(new Message(1, 1, responseString));
                    // messages.add(new Message(1, 1, "Debug: HalloTest"));

                    /****************** Debug*END ***********************************/

                    JSONArray jArray = new JSONArray(json);

                    if (jArray.isNull(0))
                        throw new RestServiceException(MessageError.GET_NO_NEW_MESSAGE);

                    for (int i = 0; i < jArray.length(); i++) {

                        JSONObject obj = jArray.getJSONObject(i);
                        JSONObject sender = obj.getJSONObject("sender");

                        messages.add(new Message(new User(sender.getString("name"),
                                sender.getLong("id")), obj.getString("message"), 1, obj.getLong("keyID")));
                    }


            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.out.println("Number new Messages: " + messages.size());
        return messages;
    }
}
