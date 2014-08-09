package de.fau.cs.mad.yasme.android.connection;

import android.util.Log;

import de.fau.cs.mad.yasme.android.entities.MessageKey;
import de.fau.cs.mad.yasme.android.exception.IncompleteKeyException;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;
import de.fau.cs.mad.yasme.android.exception.Error;

import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class MessageKeyTask extends ConnectionTask {

    private static MessageKeyTask instance;


    public static MessageKeyTask getInstance() {
        if (instance == null) {
            synchronized (MessageKeyTask.class) {
                if (null == instance) {
                    instance = new MessageKeyTask();
                }
            }
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

    public MessageKey saveKeys(ArrayList<MessageKey> messageKeys) throws IncompleteKeyException {
        try {
            Log.d(this.getClass().getSimpleName(),"[???] Keys werden gesendet");
            HttpResponse httpResponse = executeRequest(Request.POST, "", messageKeys);

            Log.d(this.getClass().getSimpleName(),"[???] Antwort auswerten");
            MessageKey messageKey = new ObjectMapper().readValue(new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent())).readLine(), MessageKey.class);

            return messageKey;
        } catch (RestServiceException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            if (e.getCode() == Error.INCOMPLETE_REQUEST.getNumber()) {
                throw new IncompleteKeyException();
            }
        } catch (IOException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
        }
        return null;
    }

    public boolean deleteKey(long keyId) throws RestServiceException {
        executeRequest(Request.DELETE, String.valueOf(keyId));
        return true;
    }

/*
    public MessageKey saveKey(ArrayList<User> recipients, Chat chat,
                              String key, String iv, byte encType, String sign) throws RestServiceException {

        try {
            Log.d(this.getClass().getSimpleName(),"[???] Key wird an Server gesendet für insgesamt " + recipients.size() + " Users");

            int i = 0;

            //messageKey Array
            //MessageKey[] messageKeys = new MessageKey[recipients.size()];
            ArrayList<MessageKey> messageKeys = new ArrayList<MessageKey>();

            //for (User user : recipients) {
            //TODO: Change
            //Log.d(this.getClass().getSimpleName(),"[???] Get Devices for User " + user.getId());
            //for (Device recipientDevice : DeviceTask.getInstance().getAllDevices(user.getId())) {
            for (Device recipientDevice : ChatTask.getInstance().getAllDevicesForChat(chat.getId())) {
                Log.d(this.getClass().getSimpleName(),"[???] Send Key for Device" + recipientDevice.getId());

                // Do not store the key on the server for the creating device
                if (recipientDevice.getId() == Long.parseLong(deviceId)) {
                    continue;
                }

                Log.d(this.getClass().getSimpleName(),"[????] Send Key for Device" + recipientDevice.getId());

                MessageKey messageKey = new MessageKey(0, new Device(Long.parseLong(deviceId)),
                        recipientDevice, chat, key, iv, encType, sign);
                KeyEncryption keyEncryption = new KeyEncryption();
                MessageKey messageKeyEncrypted = keyEncryption.encrypt(messageKey);
                MessageKey messageKeySigned = keyEncryption.sign(messageKeyEncrypted);

               // TEST
                if (keyEncryption.verify(messageKeySigned)){
                    Log.d(this.getClass().getSimpleName(), "[????] Verification successful.");
                }else{
                    Log.d(this.getClass().getSimpleName(), "[????] Verification failed.");
                }
                // TEST

                Log.d(this.getClass().getSimpleName(), "[???] MessageKey has successfully been encrypted.");
                Log.d(this.getClass().getSimpleName(), "[???] MessageKey has successfully been signed.");

                messageKeys.add(messageKeySigned);

                Log.d(this.getClass().getSimpleName(),"[???] Key von " + deviceId + " für Device " + recipientDevice.getId() + " generiert");

            }

            Log.d(this.getClass().getSimpleName(),"[???] Keys werden gesendet");
            HttpResponse httpResponse = executeRequest(Request.POST, "", messageKeys);

            Log.d(this.getClass().getSimpleName(),"[???] Antwort auswerten");

            String json = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent())).readLine();

            //Log.d(this.getClass().getSimpleName(),"getKeyRequest successful: " + json);

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
    */
}
