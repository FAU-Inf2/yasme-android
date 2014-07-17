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
import net.yasme.android.storage.CurrentKey;
import net.yasme.android.storage.DatabaseManager;

import android.util.Base64;

import java.util.ArrayList;
import java.util.List;

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
    private long mKeyId;
    Chat chat;
    User creator;
    //ArrayList<User> recipients = new ArrayList<User>(); //Send generated Key to this recipients

    private AESEncryption aes;
    private DatabaseManager db = DatabaseManager.INSTANCE;

    // Constructor fuer Chat-Verschluesselung--> holt bzw. generiert Key, falls noetig
    public MessageEncryption(Chat chat, User creator) {

        this.chat = chat;
        this.creator = creator;

        // if no old key for this chat, then generate a new one, beginning with
        //List<CurrentKey> currentKeys = db.getCurrentKeyDAO().getCurrentKeysByChat(chat.getId());
        //if (currentKeys == null  || currentKeys.size() <= 0 || currentKeys.get(0).getMessageKey().getId() < 1) {
        MessageKey currentKey = db.getMessageKeyDAO().getCurrentKeyByChat(chat.getId());
        // TODO: Also check timestamp
        if (currentKey == null) {
            generateKey();
        }
        // if old key is already available
        else {
            // get needed Key from LocalStorage
            // TODO: change
            //updateKey();
            generateKey();
        }
    }



    //update Key for Encryption
    public void updateKey() {
        try {
            // check, which Key is need to encrypt
            mKeyId = getCurrentKeyId();

            // get Key from storage
            MessageKey key = getKeyFromLocalStorage(chat.getId(), mKeyId);
            // if Key is available
            if (key != null) {
                String keyBase64 = key.getMessageKey();
                String ivBase64 = key.getInitVector();

                byte[] keyBytes = Base64.decode(keyBase64.getBytes(), Base64.DEFAULT);
                byte[] ivBytes = Base64.decode(ivBase64.getBytes(), Base64.DEFAULT);

                aes = new AESEncryption(keyBytes, ivBytes);
            }
        }
        catch (Exception e){
            Log.d(this.getClass().getSimpleName(),e.getMessage());
        }
    }

    public void generateKey() {
        Log.d(this.getClass().getSimpleName(),"[???] Generate Key");
        aes = new AESEncryption();

        ArrayList<User> recipients = new ArrayList<User>();

        // TODO pro User alle Devices suchen und in recipients speichern
        //suche alle Empfaenger des Schluessels
        if (chat.getParticipants() != null) {
            for (User user : chat.getParticipants()) {
                long userId = user.getId();
                //nicht an sich selbst schicken
                //TODO TEST
                if (userId != creator.getId()) {
                    recipients.add(user);
                } else { // DEBUG TODO: REMOVE
                    //recipients.add(user);
                }
            }
        }
        //TODO: If-Anweisung entfernen wenn participants in chat implementiert wurde
        //TODO: If sendkey nicht erfolgreich, dann Devices pro User updaten und nochmal versuchen!!!
        if (recipients.size() > 0) {
            //send Key to server
            Log.d(this.getClass().getSimpleName(),"[???] Send Key to server ...");
            MessageKey resultMessageKey = sendKey(recipients);
            Log.d(this.getClass().getSimpleName(),"[???] ... done");

            //if server has successfully saved the key
            if (resultMessageKey != null) {
                mKeyId = resultMessageKey.getId();
                long timestamp = resultMessageKey.getTimestamp();
                Log.d(this.getClass().getSimpleName(),"[???] Key wurde an Server gesendet, ID: "+mKeyId);
                db.getMessageKeyDAO().addIfNotExists(resultMessageKey);
                Log.d(this.getClass().getSimpleName(),"[???] Key wurde lokal gespeichert, ID: "+mKeyId);
            }else {
                Log.d(this.getClass().getSimpleName(),"[???] Fehler beim Senden des Keys an den Server");
            }
        }
        else{
            Log.d(this.getClass().getSimpleName(),"[???] No recipients in chat could be found. Key was not sent to server!");
        }

        //if (!ConnectionTask.isInitialized()) {
        //    ConnectionTask.initParams(context.getResources().getString(R.string.server_scheme),context.getResources().getString(R.string.server_host),context.getResources().getString(R.string.server_port));
        //}
    }

    // get Id of which Key is need to encrypt
    public long getCurrentKeyId() {
        if (db.getMessageKeyDAO().getCurrentKeyByChat(chat.getId()) != null) {
            return db.getMessageKeyDAO().getCurrentKeyByChat(chat.getId()).getId();
        } else {
            return -1;
        }


        //return db.getCurrentKeyDAO().getCurrentKeysByChat(chat.getId()).get(0).getMessageKey().getId();
    }

    // encrypt
    public String encrypt(String text) {
        Log.d(this.getClass().getSimpleName(),"[???]: Nachricht wird verschlüsselt:");
        Log.d(this.getClass().getSimpleName(),"[???]: Key: " + aes.getKey());
        Log.d(this.getClass().getSimpleName(),"[???]: IV: " + aes.getIV());

        return aes.encrypt(text);
        //return text;
    }

    // decrypt
    public String decrypt(String encrypted, long keyId) {
        Log.d(this.getClass().getSimpleName(),"[???] Decrypt with:");
        Log.d(this.getClass().getSimpleName(),"[???] aktuelle KEYID:" + this.mKeyId);
        Log.d(this.getClass().getSimpleName(),"[???] benoetigte KEYID:" + keyId);

        if (this.mKeyId == keyId) {
            return aes.decrypt(encrypted, aes.getKey(), aes.getIV());
        }

        // another key is needed
        else {
            // get Key from storage
            MessageKey messageKey = getKeyFromLocalStorage(chat.getId(), keyId);
            // if Key is available
            if (messageKey != null) {
                String keyBase64 = messageKey.getMessageKey();
                String ivBase64 = messageKey.getInitVector();

                byte[] keyBytes = Base64.decode(keyBase64.getBytes(), Base64.DEFAULT);
                byte[] ivBytes = Base64.decode(ivBase64.getBytes(), Base64.DEFAULT);

                // convert key needed for decryption
                SecretKey key = new SecretKeySpec(keyBytes, "AES");
                IvParameterSpec iv = new IvParameterSpec(ivBytes);

                Log.d(this.getClass().getSimpleName(),"[???]: alter Key wurde zum Entschluesseln geladen:");
                Log.d(this.getClass().getSimpleName(),"[???]: Key: " + keyBase64);
                Log.d(this.getClass().getSimpleName(),"[???]: IV: " + ivBase64);

                String decrypted = aes.decrypt(encrypted, key, iv);

                return decrypted;
            }
            return "Key for Decryption could not be found";
        }
    }

    public long getKeyId() {
        return this.mKeyId;
    }

    // send Key to server

    public MessageKey sendKey(ArrayList<User> recipients) {
        /*
        try {
            Log.d(this.getClass().getSimpleName(),"[???] Create task");
            SendMessageKeyTask task = new SendMessageKeyTask(aes, recipients, chat);
            Log.d(this.getClass().getSimpleName(),"[???] Execute task");
            task.execute();
            Log.d(this.getClass().getSimpleName(),"[???] Wait ...");
            MessageKey result = task.get();
            return result;
        } catch (Exception e) {
            Log.d(this.getClass().getSimpleName(),e.getMessage());
        }
        return null;
        */
        //if (true) return null;

        try {
            Log.d(this.getClass().getSimpleName(),"Try to send MessageKey");
            String keyBase64 = aes.getKeyinBase64();
            String iv = aes.getIVinBase64();
            String sign = "test";
            //TODO: encType je nach Verschluesselung anpassen
            byte encType = 0;

            // send Key to all Recipients
            Log.d(this.getClass().getSimpleName(),"Send key");
            MessageKeyTask messageKeyTask = MessageKeyTask.getInstance();
            MessageKey messageKey = messageKeyTask.saveKey(recipients, chat,
                    keyBase64, iv, encType, sign);

            Log.d(this.getClass().getSimpleName(),"Key sent");
            return messageKey;
        } catch (Exception e) {
            Log.d(this.getClass().getSimpleName(),"Fail to send key: "+e.getMessage());
            return null;
        }
    }



    //delete a symmetric Key from server when the client got that key
    //public void deleteKeyFromServer(long keyId){
    //    new DeleteMessageKeyTask().execute(keyId);
    //}

    // save needed key for chatid, and save key for keyid
      /*
    public void saveKey(long keyid, String key, String iv, long timestamp) {

        SharedPreferences keysPref = context.getSharedPreferences(KEYSTORAGE, Context.MODE_PRIVATE);
        SharedPreferences currentKeyPref = context.getSharedPreferences(CURRENTKEY, Context.MODE_PRIVATE);

        SharedPreferences.Editor keysEditor = keysPref.edit();
        SharedPreferences.Editor currentKeyEditor = currentKeyPref.edit();

        // safe Key+IV, which belongs to Key-Id
        keysEditor.putString(Long.toString(keyid), key + "," + iv);

        //delete old key, which was needed for encryption, if it is older than the new one
        if (currentKeyPref.contains("keyId")) {
            long old_ts = currentKeyPref.getLong("timestamp", 0);

            //if new timestamp is after old timestamp, replace old key by new key
            if(new Timestamp(timestamp).after(new Timestamp(old_ts))){
                currentKeyEditor.remove("keyId");
                currentKeyEditor.remove("timestamp");
                // safe new Key-Id for this Chat-ID
                currentKeyEditor.putLong("keyId", keyid);
                currentKeyEditor.putLong("timestamp",timestamp);
            }

        }else{
            // safe new Key-Id for this Chat-ID
            currentKeyEditor.putLong("keyId", keyid);
            currentKeyEditor.putLong("timestamp",timestamp);
        }


        keysEditor.commit();
        currentKeyEditor.commit();


    }
  */
    public MessageKey getKeyFromLocalStorage(long chatId, long keyId) {
        Log.d(getClass().getSimpleName(), "Get key from DB: " + keyId);
        return db.getMessageKeyDAO().get(keyId);

        /*
        SharedPreferences sharedPref;
        try {
            sharedPref = context.getSharedPreferences(
                    KEYSTORAGE, Context.MODE_PRIVATE);
        } catch (Exception e) {
            return null;
        }
        if (sharedPref.contains(Long.toString(keyid))) {
            String base64 = sharedPref.getString(Long.toString(keyid), "");

            // if Key is available
            if (base64 != "") {
                String[] base64arr = base64.split(",");
                String base64key = base64arr[0];
                String base64iv = base64arr[1];

                // convert to byte
                byte[][] keydata = new byte[2][16];
                keydata[0] = Base64.decode(base64key.getBytes(), Base64.DEFAULT);
                keydata[1] = Base64.decode(base64iv.getBytes(), Base64.DEFAULT);

                // [0] --> Key in Base64, [1] --> IV in Base64
                return keydata;
            }
            // Key is not available
            return null;
        }
        // Key is not available
        return null;
        */

    }

}

