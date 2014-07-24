package net.yasme.android.encryption;

import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import net.yasme.android.R;
import net.yasme.android.connection.MessageKeyTask;
import net.yasme.android.controller.Toaster;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Message;
import net.yasme.android.entities.MessageKey;
import net.yasme.android.entities.User;
import net.yasme.android.storage.DatabaseManager;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MessageEncryption {
    private Map<Long,MessageKey> currentKeys = new HashMap<Long,MessageKey>();
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
                //return generateKey();
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

        return sendKey(key,iv);
    }

    public MessageKey getKey(long keyId) {
        if (currentKeys.containsKey(keyId)) {
            return currentKeys.get(keyId);
        } else {
            return getKeyFromLocalStorage(keyId);
        }
    }

    public MessageKey getKeyFromLocalStorage(long keyId) {
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
            message.setMessage("Key could not be generated");
            return message;
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
            message.setMessage("Key " + message.getMessageKeyId() + " not found");
            return message;
        }
        AESEncryption aes = new AESEncryption(messageKey);
        message.setMessage(aes.decrypt(message.getMessage()));

        //is the message successfully authenticated?
        message.setAuthenticity(messageKey.getAuthenticity());

        return message;
    }

    public MessageKey sendKey(String key, String iv) {
        ArrayList<User> recipients = chat.getParticipants();

        //TODO: If sendkey nicht erfolgreich, dann Devices pro User updaten und nochmal versuchen!!!
        try {
            Log.d(this.getClass().getSimpleName(),"Try to send MessageKey");
            String sign = "test";
            //TODO: encType je nach Verschluesselung anpassen
            byte encType = 0;

            // send Key to all Recipients
            Log.d(this.getClass().getSimpleName(),"Send key");
            MessageKeyTask messageKeyTask = MessageKeyTask.getInstance();
            MessageKey messageKey = messageKeyTask.saveKey(recipients, chat,
                    key, iv, encType, sign);
            Log.d(this.getClass().getSimpleName(),"Key sent");

            if (messageKey != null) {
                Log.d(this.getClass().getSimpleName(),"[???] Key wurde an Server gesendet, ID: "+ messageKey.getId());
                db.getMessageKeyDAO().addIfNotExists(messageKey);
                Log.d(this.getClass().getSimpleName(),"[???] Key wurde lokal gespeichert, ID: "+ messageKey.getId());
                currentKeys.put(messageKey.getId(),messageKey);
                return messageKey;
            }else {
                Log.d(this.getClass().getSimpleName(),"[???] Fehler beim Senden des Keys an den Server");
                return null;
            }
        } catch (Exception e) {
            Log.d(this.getClass().getSimpleName(),"Fail to send key: "+e.getMessage());
            return null;
        }
    }
}

