package net.yasme.android.encryption;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import net.yasme.android.R;
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

//ChatKeyMapping						Chat-ID: 2
//Chat-ID	|	Key-ID					Key-ID	|	Key
//---------------------					--------------------------------
//	2		|	1							1	|	KEY, IV
//											2	| 	KEY, IV

//TODO: Bevor Nachrichten vom Server geholt werden, muessen neue Keys vom Server geholt werden und diese Tabellen aktualisiert werden
//TODO: Wenn Schluessel empfangen wird, dann Befehl senden, dass Schluessel auf Server geloescht wird

public class MessageEncryption {

    long keyId; // contains the latest keyid for encryption
    
    long chatId;
    long creatorDevice;
    long recipientDevice;

    String url;
    Context context;

    private final String CHATKEYMAPPING = "ChatKeyMapping"; // tablename for
    // chatkeymapping

    private AESEncryption aes;
    private KeyTask keytask;

    // TODO: aus long chatid muss Chat chat werden
    // Constructor --> holt bzw. generiert Key, falls noetig
    public MessageEncryption(Context context, long chatid, long creator) {
        this.context = context;
        this.chatId = chatid;
        this.creatorDevice = creator;

        SharedPreferences sharedPref = context.getSharedPreferences(
                CHATKEYMAPPING, Context.MODE_PRIVATE);

        // if no old key for this chat, then generate a new one, beginning with
        // ID "1"
        if (!sharedPref.contains(Long.toString(chatId))) {
        	//TODO: Wert kommt von Server
            keyId = 1L;
            aes = new AESEncryption("geheim");
            saveKey(context, chatId, keyId);

            // TODO Schluessel fuer jeden Empaenger an den Server senden
            // get recipientDevice from chatid
            // for every recipientDevice
            sendKey(context.getResources().getString(R.string.server_url),
                    recipientDevice);

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
        byte[][] keydata = getKeyfromLocalStorage(context, chatId, keyId);
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
    	SharedPreferences sharedPref = context.getSharedPreferences(CHATKEYMAPPING, Context.MODE_PRIVATE);
    	
    	long keyidfromstorage = sharedPref.getLong(Long.toString(chatId), 0);
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
            byte[][] keydata = getKeyfromLocalStorage(context, chatId, keyid);
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
    public boolean sendKey(String url, long recipient) {
        this.url = url;
        this.recipientDevice = recipient;
        new SendKeyTask().execute();
        return true;
    }

    // save needed key for chatid, and save key for keyid
    public void saveKey(Context context, long chatid, long keyid) {
        SharedPreferences keyPref = context.getSharedPreferences(
                Long.toString(chatid), Context.MODE_PRIVATE);
        
        SharedPreferences chatPref = context.getSharedPreferences(
                CHATKEYMAPPING, Context.MODE_PRIVATE);

        SharedPreferences.Editor keyeditor = keyPref.edit();
        SharedPreferences.Editor chateditor = chatPref.edit();
        // safe Key+IV, which belongs to Key-Id
        keyeditor.putString(Long.toString(keyid),
                aes.getKey() + "," + aes.getIV());
        
        //delete old key, which was needed for encryption
        if (keyPref.contains(Long.toString(chatid))) {
            chateditor.remove(Long.toString(chatid));
        }

        // safe new Key-Id for this Chat-ID
        chateditor.putLong(Long.toString(chatid), keyid);
        
        keyeditor.commit();
        chateditor.commit();

    }

    public byte[][] getKeyfromLocalStorage(Context context, long chatid,
                                           long keyid) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                Long.toString(chatid), Context.MODE_PRIVATE);
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

                key = aes.getKey() + "," + aes.getIV();
                byte encType = 1;

                // setup MessageKey-Object
                MessageKey keydata = new MessageKey(keyId, creatorDevice,
                        recipientDevice, chatId, key, encType, "test");

                // send MessageKey-Object
                keytask = new KeyTask(url);
                keytask.saveKey(keydata);

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {

        }
    }
}

