package net.yasme.android.encryption;

import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import net.yasme.android.R;
import net.yasme.android.connection.ChatTask;
import net.yasme.android.connection.ConnectionTask;
import net.yasme.android.connection.MessageKeyTask;
import net.yasme.android.controller.Toaster;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Device;
import net.yasme.android.entities.Message;
import net.yasme.android.entities.MessageKey;
import net.yasme.android.entities.User;
import net.yasme.android.exception.*;
import net.yasme.android.storage.DatabaseManager;


import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageEncryption {
    Chat chat;
    User creator;
    private DatabaseManager db = DatabaseManager.INSTANCE;

    // Constructor fuer Chat-Verschluesselung
    public MessageEncryption(Chat chat, User creator) {
        this.chat = chat;
        this.creator = creator;
    }

    public MessageKey getCurrentKey() {
        try {
            Log.d(this.getClass().getSimpleName(),"[???] Try to use a local Key");
            MessageKey key = db.getMessageKeyDAO().getCurrentKeyByChat(chat.getId());
            if (key != null) {
                Log.d(this.getClass().getSimpleName(),"[???] ... success");
                return key;
            } else {
                Log.d(this.getClass().getSimpleName(),"[???] ... failed");
                return generateKey();
            }
        }
        catch (Exception e){
            Log.d(this.getClass().getSimpleName(),"[???] ... failed with exception");
            e.printStackTrace();
            return null;
        }
    }

    public MessageKey generateKey() {
        Log.d(this.getClass().getSimpleName(),"[???] Generate Key");
        Toaster.getInstance().toast(R.string.generate_key, Toast.LENGTH_LONG);

        // Generate key
        AESEncryption aes = new AESEncryption();
        String key = aes.getKeyinBase64();
        String iv = aes.getIVinBase64();

        return sendKey(key, iv, true);
    }

    public MessageKey getKey(long keyId) {
        Log.d(getClass().getSimpleName(), "Get key from DB: " + keyId);
        return db.getMessageKeyDAO().get(keyId);
    }

    // encrypt
    public Message encrypt(Message message) {
        return encrypt(message,getCurrentKey());
    }

    public Message encryptGenerated(Message message) {
        return encrypt(message,generateKey());
    }

    private Message encrypt (Message message, MessageKey messageKey) {
        if (messageKey == null) {
            Log.e(getClass().getSimpleName(), "Message could not be encrypted");
            Toaster.getInstance().toast(R.string.key_generation_failed,Toast.LENGTH_LONG);
            return null;
        }
        AESEncryption aes = new AESEncryption(messageKey);
        message.setMessage(aes.encrypt(message.getMessage()));
        message.setMessageKeyId(messageKey.getId());
        return message;
    }

    // decrypt
    public Message decrypt(Message message) {
        MessageKey messageKey = getKey(message.getMessageKeyId());
        if (messageKey == null) {
            message.setMessage("");
            message.setErrorId(R.string.decryption_failed);
            return message;
        }
        AESEncryption aes = new AESEncryption(messageKey);
        message.setMessage(aes.decrypt(message.getMessage()));

        //is the message successfully authenticated?
        message.setAuthenticity(messageKey.getAuthenticity());
        if (message.getErrorId() == 0 && !messageKey.getAuthenticity()) {
            message.setErrorId(R.string.authentication_failed);
        }

        return message;
    }

    private List<Device> getRecipientDevices(boolean local) {
        if (local) {
            Log.d(this.getClass().getSimpleName(),"[???] Get local stored devices");
            List<Device> devices = new ArrayList<>();
            for (User user : chat.getParticipants()) {
                for (Device device : DatabaseManager.INSTANCE.getDeviceDAO().getAll(user)) {
                    Log.d(this.getClass().getSimpleName(),"[???] Local-device: " + device.getId());
                    devices.add(device);
                }
            }
            return devices;
        } else {
            try {
                Log.d(this.getClass().getSimpleName(),"[???] Get devices from Server");
                return ChatTask.getInstance().getAllDevicesForChat(chat.getId());
            } catch (Exception e) {
                return new ArrayList<>();
            }

        }
    }

    public MessageKey sendKey(String key, String iv, boolean local) {
        //TODO: If sendkey nicht erfolgreich, dann Devices pro User updaten und nochmal versuchen!!!
        try {
            long deviceId = DatabaseManager.INSTANCE.getDeviceId();
            Device sender = new Device(deviceId);
            ArrayList<MessageKey> messageKeys = new ArrayList<MessageKey>();
            List<Device> devices = getRecipientDevices(local);
            if (local && devices.size() == 0) {
                getRecipientDevices(false);
            }

            //TODO: Try with local data first
            for (Device recipientDevice : getRecipientDevices(local)) {
                Log.d(this.getClass().getSimpleName(),"[???] Send Key for Device" + recipientDevice.getId() + " with pubKey: " + recipientDevice.getPublicKey());


                // Do not store the key on the server for the creating device
                if (recipientDevice.getId() == deviceId) {
                    continue;
                }

                Log.d(this.getClass().getSimpleName(),"[????] Generate Key for Device" + recipientDevice.getId());

                MessageKey messageKey = new MessageKey(0, sender, recipientDevice, chat, key, iv);
                KeyEncryption keyEncryption = new KeyEncryption();
                MessageKey messageKeyEncrypted = keyEncryption.encrypt(messageKey);
                Log.d(this.getClass().getSimpleName(), "[???] MessageKey has successfully been encrypted.");
                MessageKey messageKeySigned = keyEncryption.sign(messageKeyEncrypted);
                Log.d(this.getClass().getSimpleName(), "[???] MessageKey has successfully been signed.");

                messageKeys.add(messageKeySigned);
                Log.d(this.getClass().getSimpleName(),"[???] Key von " + deviceId + " für Device " + recipientDevice.getId() + " generiert");
            }

            MessageKey result = MessageKeyTask.getInstance().saveKeys(messageKeys);

            if (result == null) {
                Log.d(this.getClass().getSimpleName(),"[???] Fehler beim Senden des Keys an den Server");
                return null;
            }

            MessageKey messageKey = new MessageKey(result.getId(),sender,sender,chat,key,iv);
            messageKey.setCreated(result.getCreated());

            Log.d(this.getClass().getSimpleName(),"[???] Key wurde an Server gesendet, ID: "+ messageKey.getId());
            // If you can trust yourself
            messageKey.setAuthenticity(true);
            db.getMessageKeyDAO().addIfNotExists(messageKey);
            return messageKey;

        } catch (IncompleteKeyException e) {
            if (local) {
                Log.e(this.getClass().getSimpleName(),"[???] Keyerzeugung");
                return sendKey(key,iv,false);
            } else {
                Log.e(this.getClass().getSimpleName(),"[???] Key wurde nicht an den Server gesendet");
                return null;
            }
        } catch (Exception e) {
            Log.d(this.getClass().getSimpleName(),"Fail to send key: "+e.getMessage());
            return null;
        }
    }
}

