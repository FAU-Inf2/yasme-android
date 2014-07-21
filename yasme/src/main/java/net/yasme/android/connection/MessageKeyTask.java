package net.yasme.android.connection;

import android.util.Log;

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
import java.util.Date;

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
            this.uri = new URIBuilder(baseURI).setPath(ConnectionTask.APIVERSION + "/msgkey").build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    public MessageKey saveKey(ArrayList<User> recipients, Chat chat,
                              String key, String iv, byte encType, String sign) throws RestServiceException {

        try {
            Log.d(this.getClass().getSimpleName(),"[???] Key wird an Server gesendet für insgesamt " + recipients.size() + " Users");

            int i = 0;

            //messageKey Array
            //MessageKey[] messageKeys = new MessageKey[recipients.size()];
            ArrayList<MessageKey> messageKeys = new ArrayList<MessageKey>();

            for (User user : recipients) {
                //TODO: Change
                Log.d(this.getClass().getSimpleName(),"[???] Get Devices for User " + user.getId());
                //for (Device recipientDevice : user.getDevices()) {
                for (Device recipientDevice : DeviceTask.getInstance().getAllDevices(user.getId())) {
                    Log.d(this.getClass().getSimpleName(),"[???] Send Key for Device" + recipientDevice.getId());
                    //encrypt the key with RSA
                    /*
                    MessageSignatur rsa = new MessageSignatur(context, creatorDevice);
                    PublicKey pubKey = rsa.getPubKeyFromUser(recipient);
                    String keyEncrypted = rsa.encrypt(key, pubKey);
                    */

                    // Do not store the key on the server for the creating device
                    if (recipientDevice.getId() == Long.parseLong(deviceId)) {
                       continue;
                    }

                    messageKeys.add(new MessageKey(0, new Device(Long.parseLong(deviceId)),
                            new Device(recipientDevice.getId()), chat, key, iv, encType, sign));

                    Log.d(this.getClass().getSimpleName(),"[???] Key von " + deviceId + " für Device generiert: " + recipientDevice.getId());
                }
            }

            Log.d(this.getClass().getSimpleName(),"[???] Keys werden gesendet");
            HttpResponse httpResponse = executeRequest(Request.POST, "", messageKeys);

            Log.d(this.getClass().getSimpleName(),"[???] Antwort auswerten");

            String json = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent())).readLine();
            /**DEBUG**/
            //Log.d(this.getClass().getSimpleName(),"getKeyRequest successful: " + json);
            /**DEBUG**/
            Log.d(this.getClass().getSimpleName(),"[???] Antwort: " + json);

            JSONObject obj = new JSONObject(json);

            long keyId = obj.getLong("id");
            Date created = new Date(obj.getLong("created"));
            Log.d(getClass().getSimpleName(), "Key created: " + created.toString());

            //return keyId and timestamp from serverresponse
            //TODO: Dummy_IV
            MessageKey result = new MessageKey(keyId, new Device(Long.parseLong(deviceId)), new Device(0), chat, key, iv, encType, sign);
            result.setCreated(created);

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

