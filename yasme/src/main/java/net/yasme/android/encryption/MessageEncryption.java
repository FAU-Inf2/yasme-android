package net.yasme.android.encryption;

import android.util.Base64;
import android.util.Log;

import net.yasme.android.asyncTasks.server.SendMessageKeyTask;
import net.yasme.android.entities.Chat;
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

    long keyId = -1; // contains the latest keyid for encryption (get it from server, after sending the key)

    long chatId;
    Chat chat;
    long creatorDevice; //TODO: make DEVICE
    ArrayList<User> recipients = new ArrayList<User>(); //Send generated Key to this recipients
    //Context context;
    //String accessToken;

    //private String CURRENTKEY = "CurrentKey"; // tablename for "currentKey-Storage" per Chat
    //private String KEYSTORAGE = "KeyStorage"; //tablename for "KeyStorage" per Chat

    private AESEncryption aes;

    private DatabaseManager db = DatabaseManager.INSTANCE;


    /*
    //Constructor for saving Key from server (Generating a key is not necessary)
    public MessageEncryption(long tets, Context context, long chatid){
        this.context = context;
        this.chatId = chatid;
        this.CURRENTKEY = this.CURRENTKEY + "_" + Long.toString(chatId);
        this.KEYSTORAGE = this.KEYSTORAGE + "_" + Long.toString(chatId);
    }
    */

    // Constructor fuer Chat-Verschluesselung--> holt bzw. generiert Key, falls noetig
    public MessageEncryption(Chat chat, long creator) {

        //this(context, chat.getId());
        this.chat = chat;
        //this.accessToken = accessToken;
        this.creatorDevice = creator;

        //SharedPreferences currentKeyPref = context.getSharedPreferences(
        //        CURRENTKEY, Context.MODE_PRIVATE);

        // if no old key for this chat, then generate a new one, beginning with
        List<CurrentKey> currentKeys = db.getCurrentKeyDAO().getCurrentKeysByChat(chatId);
        if (currentKeys != null  && currentKeys.size() > 0
                && currentKeys.get(0).getMessageKey().getId() < 1) {
            Log.d(this.getClass().getSimpleName(),"[???] Generate Key");
            aes = new AESEncryption();

            // TODO pro User alle Devices suchen und in recipients speichern
            //suche alle Empfaenger des Schluessels
            if (chat.getParticipants() != null) {
                for (User user : chat.getParticipants()) {
                    long userId = user.getId();
                    //nicht an sich selbst schicken
                    //TODO TEST
                    if (userId != creator) {
                        recipients.add(user);
                    }

                }
            }
            //TODO: If-Anweisung entfernen wenn participants in chat implementiert wurde
            //TODO: If sendkey nicht erfolgreich, dann Devices pro User updaten und nochmal versuchen!!!
            if (recipients.size() > 0) {
                //send Key to server
                MessageKey resultMessageKey = sendKey();

                //if server has successfully saved the key
                if (resultMessageKey != null) {
                    keyId = resultMessageKey.getId();
                    long timestamp = resultMessageKey.getTimestamp();
                    Log.d(this.getClass().getSimpleName(),"[???] Key wurde an Server gesendet, ID: "+keyId);
                    //saveKey(keyId, aes.getKeyinBase64(), aes.getIVinBase64(), timestamp);
                    //TODO: saveKeyToDatabase
                    Log.d(this.getClass().getSimpleName(),"[???] Key wurde lokal gespeichert, ID: "+keyId);
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

        // if old key is already available
        else {
            // get needed Key from LocalStorage
            updateKey();
            // ###DEBUG
            Log.d(this.getClass().getSimpleName(),"[???]: Key " + keyId + " fuer Chat " + chatId + " wurde geladen");
            // /###
        }

    }



    //update Key for Encryption
    public void updateKey() {
        try {
            // check, which Key is need to encrypt
            checkCurrentKeyId();

            // get Key from storage
            MessageKey key = getKeyFromLocalStorage(chatId, keyId);
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

    // check, which Key is need to encrypt
    public void checkCurrentKeyId() {
        keyId = db.getCurrentKeyDAO().getCurrentKeysByChat(chatId).get(0).getMessageKey().getId();
        /*SharedPreferences currentKeyPref = context.getSharedPreferences(CURRENTKEY, Context.MODE_PRIVATE);

        long keyidfromstorage = currentKeyPref.getLong("keyId", 0);
        keyId = keyidfromstorage;
        */
    }

    // encrypt
    public String encrypt(String text) {
        return aes.encrypt(text);
    }

    // decrypt
    public String decrypt(String encrypted, long keyid) {
        Log.d(this.getClass().getSimpleName(),"[???] Decrypt with:");
        Log.d(this.getClass().getSimpleName(),"[???] aktuelle KEYID:" + this.keyId);
        Log.d(this.getClass().getSimpleName(),"[???] benoetigte KEYID:" + keyid);

        if (this.keyId == keyid) {
            return aes.decrypt(encrypted, aes.getKey(), aes.getIV());
        }

        // another key is needed
        else {
            // get Key from storage
            MessageKey messageKey = getKeyFromLocalStorage(chatId, keyId);
            // if Key is available
            if (messageKey != null) {
                String keyBase64 = messageKey.getMessageKey();
                String ivBase64 = messageKey.getInitVector();

                byte[] keyBytes = Base64.decode(keyBase64.getBytes(), Base64.DEFAULT);
                byte[] ivBytes = Base64.decode(ivBase64.getBytes(), Base64.DEFAULT);

                // convert key needed for decryption
                SecretKey key = new SecretKeySpec(keyBytes, "AES");
                IvParameterSpec iv = new IvParameterSpec(ivBytes);

                Log.d(this.getClass().getSimpleName(),"[???]: alter Key wurde zum Entschluesseln geladen");
                String decrypted = aes.decrypt(encrypted, key, iv);

                return decrypted;
            }
            return "Key for Decryption could not be found";
        }

    }

    public long getKeyId() {
        return this.keyId;
    }

    // send Key to server
    public MessageKey sendKey() {
        try {
            SendMessageKeyTask task = new SendMessageKeyTask(aes, recipients, chat);
            task.execute();
            MessageKey result = task.get();
            return result;
        } catch (Exception e) {
            Log.d(this.getClass().getSimpleName(),e.getMessage());
        }
        return null;
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

