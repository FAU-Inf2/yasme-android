package net.yasme.android.connection;

import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Device;
import net.yasme.android.entities.MessageKey;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.exception.Error;

import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class MessageKeyTask extends ConnectionTask {

    private static MessageKeyTask instance;


    public static MessageKeyTask getInstance() {
        if (instance == null) {
            instance = new MessageKeyTask();
        }
        return instance;
    }

    private MessageKeyTask() {

        try {
            this.uri = new URIBuilder(baseURI).setPath("/msgkey").build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    public MessageKey saveKey(ArrayList<User> recipients, Chat chat,
                              String key, String iv, byte encType, String sign) throws RestServiceException {

        try {
            System.out.println("[???] Key wird an Server gesendet für insgesamt " + recipients.size() + " Users");

            int i = 0;

            //messageKey Array
            MessageKey[] messageKeys = new MessageKey[recipients.size()];

            for (User recipient : recipients) {

                //encrypt the key with RSA
                /*
                MessageSignatur rsa = new MessageSignatur(context, creatorDevice);
                PublicKey pubKey = rsa.getPubKeyFromUser(recipient);
                String keyEncrypted = rsa.encrypt(key, pubKey);
                */
                //TODO: Dummy_IV
                messageKeys[i++] = new MessageKey(0, new Device(Long.parseLong(deviceId)),
                        new Device(recipient.getId()), chat, key, iv, encType, sign);

                System.out.println("[???] Key gesendet für User " + recipient);
            }

            HttpResponse httpResponse = executeRequest(Request.POST, "", messageKeys);


            String json = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent())).readLine();
            /**DEBUG**/
            //System.out.println("[DEBUG] getKeyRequest successful: " + json);
            /**DEBUG**/

            JSONObject obj = new JSONObject(json);

            long keyId = obj.getLong("id");
            long timestamp = obj.getLong("timestamp");

            //return keyId and timestamp from serverresponse
            //TODO: Dummy_IV
            MessageKey result = new MessageKey(keyId, new Device(Long.parseLong(deviceId)), new Device(0), chat, key, "DummyIV", encType, sign);
            result.setTimestamp(timestamp);

            return result;

        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteKey(long keyId) throws RestServiceException {
        executeRequest(Request.DELETE, String.valueOf(keyId));
        return true;
    }
}