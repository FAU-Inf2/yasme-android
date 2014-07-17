package net.yasme.android.encryption;

import android.util.Base64;
import android.util.Log;

import net.yasme.android.asyncTasks.server.SendMessageKeyTask;
import net.yasme.android.connection.MessageKeyTask;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Device;
import net.yasme.android.entities.Message;
import net.yasme.android.entities.MessageKey;
import net.yasme.android.entities.User;
//import net.yasme.android.storage.CurrentKey;
import net.yasme.android.storage.DatabaseManager;

import android.util.Base64;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

//um den Schluessel zum Verschluesseln abzurufen muss bekannt sein, mit welcher KeyId der Chat verschluesselt wird
//hier wird vorausgesetzt, dass zum Verschluesseln nur ein Key vorhanden is
//ACHTUNG: Zum Entschluesseln koennen aber mehrere vorhanden sein
//Mapping: Chats --> KeyID
//Tabelle: KeyID --> Key

//CurrentKey_[CHAT-ID]						Keys_[CHAT-ID]
//Type	    |	Key-ID					Key-ID	|	Key
//---------------------					--------------------------------
//keyId		|	1							1	|	KEY, IV
//Timestamp	| xxxxx							2	| 	KEY, IV


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
            MessageKey key = db.getMessageKeyDAO().getCurrentKeyByChat(chat.getId());
            if (key != null) {
                //return key;
                return generateKey();
            } else {
                return generateKey();
            }
        }
        catch (Exception e){
            Log.d(this.getClass().getSimpleName(),e.getMessage());
            return null;
        }
    }

    public MessageKey generateKey() {
        Log.d(this.getClass().getSimpleName(),"[???] Generate Key");

        // Generate key
        AESEncryption aes = new AESEncryption();
        String key = aes.getKeyinBase64();
        String iv = aes.getIVinBase64();

        ArrayList<User> recipients = chat.getParticipants();

        //TODO: If sendkey nicht erfolgreich, dann Devices pro User updaten und nochmal versuchen!!!
        if (recipients.size() > 0) {
            //send Key to server
            Log.d(this.getClass().getSimpleName(),"[???] Send Key to server ...");
            MessageKey resultMessageKey = sendKey(recipients,key,iv);
            Log.d(this.getClass().getSimpleName(),"[???] ... done");

            //if server has successfully saved the key
            if (resultMessageKey != null) {
                Long keyId = resultMessageKey.getId();
                long timestamp = resultMessageKey.getTimestamp();
                Log.d(this.getClass().getSimpleName(),"[???] Key wurde an Server gesendet, ID: "+ keyId);
                db.getMessageKeyDAO().addIfNotExists(resultMessageKey);
                Log.d(this.getClass().getSimpleName(),"[???] Key wurde lokal gespeichert, ID: "+ keyId);
                currentKeys.put(keyId,resultMessageKey);
                return resultMessageKey;
            }else {
                Log.d(this.getClass().getSimpleName(),"[???] Fehler beim Senden des Keys an den Server");
                return null;
            }
        } else {
            Log.d(this.getClass().getSimpleName(),"[???] No recipients in chat could be found. Key was not sent to server!");
            return null;
        }
    }

    // encrypt
    public Message encrypt(Message message) {
        MessageKey messageKey = getCurrentKey();
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
            message.setMessage("Key " + message.getId() + " not found");
            return message;
        }
        AESEncryption aes = new AESEncryption(messageKey);
        message.setMessage(aes.decrypt(message.getMessage()));
        return message;
    }

    public MessageKey sendKey(ArrayList<User> recipients, String key, String iv) {
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
            return messageKey;
        } catch (Exception e) {
            Log.d(this.getClass().getSimpleName(),"Fail to send key: "+e.getMessage());
            return null;
        }
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

}

