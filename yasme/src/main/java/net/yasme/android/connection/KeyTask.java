package net.yasme.android.connection;

import android.content.Context;

import net.yasme.android.connection.ssl.HttpClient;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Device;
import net.yasme.android.entities.MessageKey;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.exception.Error;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class KeyTask extends ConnectionTask {

    private static KeyTask instance;
    private Context context; //necessary for getting Key from Local Storage


    public static KeyTask getInstance(Context context) {
        if (instance == null) {
            instance = new KeyTask(context);
        }
        return instance;
    }

    private KeyTask(Context context) {

        try {
            this.uri = new URIBuilder(baseURI).setPath("/msgkey").build();
            this.context = context;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    public MessageKey saveKey(long creatorDevice, ArrayList<Long> recipients, Chat chat, String key, byte encType, String sign) throws RestServiceException {

        try {
            System.out.println("[???] Key wird an Server gesendet für insgesamt " + recipients.size() + " Users");

            int i = 0;

            //messageKey Array
            MessageKey[] messageKeys = new MessageKey[recipients.size()];

            for (long recipient : recipients) {

                //encrypt the key with RSA
                /*
                MessageSignatur rsa = new MessageSignatur(context, creatorDevice);
                PublicKey pubKey = rsa.getPubKeyFromUser(recipient);
                String keyEncrypted = rsa.encrypt(key, pubKey);
                */
                //TODO: Dummy_IV
                messageKeys[i++] = new MessageKey(0, new Device(creatorDevice),
                        new Device(recipient), chat, key, "DummyIV", encType, sign);

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
            MessageKey result = new MessageKey(keyId, new Device(creatorDevice), new Device(0), chat, key, "DummyIV", encType, sign);
            result.setTimestamp(timestamp);

            return result;

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteKey(long chatId, long keyId, long DeviceId) throws RestServiceException {

        executeRequest(Request.DELETE, keyId + "/" + chatId);
        return true;
    }
}