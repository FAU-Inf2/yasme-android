package net.yasme.android.encryption;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import net.yasme.android.R;
import net.yasme.android.connection.ConnectionTask;
import net.yasme.android.connection.KeyTask;
import net.yasme.android.entities.MessageKey;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Base64;

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

//TODO: Bevor Nachrichten vom Server geholt werden, muessen neue Keys vom Server geholt werden und diese Tabellen aktualisiert werden
//TODO: Wenn Schluessel empfangen wird, dann Befehl senden, dass Schluessel auf Server geloescht wird

public class MessageEncryption {

    long keyId; // contains the latest keyid for encryption

    long chatId;
    long creatorDevice;
    long recipientDevice;
    Context context;

    private String CURRENTKEY = "CurrentKey"; // tablename for "currentKey-Storage" per Chat
    private String KEYSTORAGE = "KeyStorage"; //tablename for "KeyStorage" per Chat

    private AESEncryption aes;
    private KeyTask keytask;


    //Constructor for saving Key from server (Generating a key is not necessary)
    public MessageEncryption(Context context, long chatid){
        this.context = context;
        this.chatId = chatid;
        this.CURRENTKEY = this.CURRENTKEY + "_" + Long.toString(chatId);
        this.KEYSTORAGE = this.KEYSTORAGE + "_" + Long.toString(chatId);
    }

    // TODO: aus long chatid muss Chat chat werden
    // Constructor fuer Chat-Verschluesselung--> holt bzw. generiert Key, falls noetig
    public MessageEncryption(Context context, long chatid, long creator) {
        new MessageEncryption(context, chatid);

        SharedPreferences currentKeyPref = context.getSharedPreferences(
                CURRENTKEY, Context.MODE_PRIVATE);

        // if no old key for this chat, then generate a new one, beginning with
        // ID "1"
        if (!currentKeyPref.contains("keyId")) {

            aes = new AESEncryption("geheim");

            // TODO Schluessel fuer jeden Empaenger an den Server senden
            // get recipientDevice from chatid
            // for every recipientDevice

            if (!ConnectionTask.isInitialized()) {
                ConnectionTask.initParams(context.getResources().getString(R.string.server_scheme),context.getResources().getString(R.string.server_host),context.getResources().getString(R.string.server_port));
            }

            //String serverUrl = context.getResources().getString(R.string.server_scheme) + context.getResources().getString(R.string.server_host) + ":" + context.getResources().getString(R.string.server_port);
            sendKey(recipientDevice);

            //TODO: KeyId vom Server abspeichern und Timestamp
            keyId = 1L;
            long timestamp = 1;
            saveKey(keyId, aes.getKeyinBase64(), aes.getIVinBase64(), timestamp);

            // ###DEBUG
            System.out.println("[???]: KeyID " + keyId + " fuer Chat " + chatId
                    + " wurde erstellt und gespeichert und an Server gesendet");
            // ###
        }

        // if old key is already available
        else {

            // get needed Key from LocalStorage
            updateKey();
        }

        // TODO:
        // What happens, if the needed key is not available
        // is this a real scenario?
    }



    //update Key for Encryption
    public void updateKey(){

        // check, which Key is need to encrypt
        checkCurrentKeyId();

        // get Key from storage
        byte[][] keydata = getKeyfromLocalStorage(chatId, keyId);
        // if Key is available
        if (keydata != null) {
            byte[] key = keydata[0];
            byte[] iv = keydata[1];

            aes = new AESEncryption(key, iv);
            // ###DEBUG
            System.out.println("[???]: Key " + keyId + " fuer Chat "
                    + chatId + " wurde geladen");
            // /###
        }
    }

    // check, which Key is need to encrypt
    public void checkCurrentKeyId(){
        SharedPreferences currentKeyPref = context.getSharedPreferences(CURRENTKEY, Context.MODE_PRIVATE);

        long keyidfromstorage = currentKeyPref.getLong("keyId", 0);
        keyId = keyidfromstorage;
    }

    // encrypt
    public String encrypt(String text) {
        return aes.encrypt(text);
    }

    // decrypt
    public String decrypt(String encrypted, long keyid) {
        System.out.println("[???] Decrypt with:");
        System.out.println("[???] aktuelle KEYID:" + this.keyId);
        System.out.println("[???] benoetigte KEYID:" + keyid);

        if (this.keyId == keyid) {
            return aes.decrypt(encrypted, aes.getKey(), aes.getIV());
        }

        // another key is needed
        else {
            // get Key from storage
            byte[][] keydata = getKeyfromLocalStorage(chatId, keyid);
            // if Key is available
            if (keydata != null) {
                byte[] keyBytes = keydata[0];
                byte[] ivBytes = keydata[1];

                // convert key needed for decryption
                SecretKey key = new SecretKeySpec(keyBytes, "AES");
                IvParameterSpec iv = new IvParameterSpec(ivBytes);

                System.out.println("[???]: alter Key wurde zum Entschluesseln geladen");
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
    public boolean sendKey(long recipient) {

        this.recipientDevice = recipient;
        new SendKeyTask().execute();
        return true;
    }

    // save needed key for chatid, and save key for keyid
    public void saveKey(long keyid, String key, String iv, long timestamp) {
        SharedPreferences keysPref = context.getSharedPreferences(KEYSTORAGE, Context.MODE_PRIVATE);

        SharedPreferences currentKeyPref = context.getSharedPreferences(
                CURRENTKEY, Context.MODE_PRIVATE);

        SharedPreferences.Editor keysEditor = keysPref.edit();
        SharedPreferences.Editor currentKeyEditor = currentKeyPref.edit();
        // safe Key+IV, which belongs to Key-Id
        keysEditor.putString(Long.toString(keyid), key + "," + iv);

        //delete old key, which was needed for encryption
        //TODO: ueberpruefen, ob der abzuspeichernde Key neuer oder älter ist
        if (currentKeyPref.contains("keyId")) {
            currentKeyEditor.remove("keyId");
            currentKeyEditor.remove("timestamp");
        }

        // safe new Key-Id for this Chat-ID
        currentKeyEditor.putLong("keyId", keyid);
        currentKeyEditor.putLong("timestamp",timestamp);

        keysEditor.commit();
        currentKeyEditor.commit();

    }

    public byte[][] getKeyfromLocalStorage(long chatid, long keyid) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                KEYSTORAGE, Context.MODE_PRIVATE);
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

    }



    // Async-Task for sending Key to Server
    class SendKeyTask extends AsyncTask<String, Void, Boolean> {
        String key;

        protected Boolean doInBackground(String... params) {

            try {

                key = aes.getKeyinBase64() + "," + aes.getIVinBase64();
                byte encType = 1;

                // setup MessageKey-Object
                MessageKey keydata = new MessageKey(keyId, creatorDevice,
                        recipientDevice, chatId, key, encType, "test");

                // send MessageKey-Object
                keytask = KeyTask.getInstance();

                //TODO: AccessToken mit übergeben
                keytask.saveKey(keydata,"0");

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {

        }
    }
}

