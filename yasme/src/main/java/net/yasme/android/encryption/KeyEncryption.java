package net.yasme.android.encryption;

/**
 * Created by Marco Eberl on 22.07.2014.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import net.yasme.android.entities.Device;
import net.yasme.android.entities.MessageKey;
import net.yasme.android.entities.User;
import net.yasme.android.storage.DatabaseManager;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeyEncryption {

    private String RSAKEY_STORAGE = "rsaKeyStorage"; //Storage for Private and Public Keys from user
    private final String PRIVATEKEY = "privateKey";
    private final String PUBLICKEY = "publicKey";
    private final byte CREATOR = 0;
    private final byte RECIPIENT = 1;
    private RSAEncryption rsa;
    private DatabaseManager db = DatabaseManager.INSTANCE;

    public KeyEncryption() {
        this.rsa = new RSAEncryption();
    }

    public void generateRSAKeys(){
        rsa.generateKeyPair();
    }

    //save own RSAKeys in SharedPreferences
    public boolean saveRSAKeys(long deviceId){

        String RSAKEY_STORAGE_USER = RSAKEY_STORAGE + "_" + deviceId;

        try {

            Context context = DatabaseManager.INSTANCE.getContext();
            SharedPreferences privKeyStorage = context.getSharedPreferences(RSAKEY_STORAGE_USER, Context.MODE_PRIVATE);
            SharedPreferences.Editor keyeditor = privKeyStorage.edit();

            //delete existing keys
            if (privKeyStorage.getString(PRIVATEKEY, "") != ""){
                keyeditor.remove(PRIVATEKEY);
            }
            if (privKeyStorage.getString(PUBLICKEY, "") != ""){
                keyeditor.remove(PUBLICKEY);
            }

            keyeditor.putString(PRIVATEKEY, rsa.getPrivKeyinBase64());
            keyeditor.putString(PUBLICKEY, rsa.getPubKeyinBase64());

            keyeditor.commit();

            Log.d(this.getClass().getSimpleName(), "[???] RSA Keys generated and saved");


            return true;
        } catch (Exception e){
            Log.d(this.getClass().getSimpleName(), "[???] saving rsa keys failed");
            Log.d(this.getClass().getSimpleName(),"[???] "+e.getMessage());
            return false;
        }
    }

    public String getGeneratedPubKeyInBase64(){
        return rsa.getPubKeyinBase64();
    }

    //encrypt
    public MessageKey encrypt(MessageKey messageKey){

        PublicKey pubKey = getPubKeyFromUser(messageKey, RECIPIENT);

        if (pubKey != null){
            String keyEncrypted = rsa.encrypt(messageKey.getMessageKey(), pubKey);
            messageKey.setKey(keyEncrypted);
            return messageKey;
        }

        return null;
    }

    //decrypt
    public MessageKey decrypt(MessageKey messageKey){

        long selfDeviceId = messageKey.getRecipientDevice().getId();
        PrivateKey privKey = getPrivateRSAKeyFromStorage(selfDeviceId);

        if (privKey != null) {
            String key = rsa.decrypt(messageKey.getMessageKey(), privKey);
            messageKey.setKey(key);
            return messageKey;
        }

        return null;
    }

    //sign
    public MessageKey sign(MessageKey messageKey){

        long selfDeviceId = messageKey.getCreatorDevice().getId();
        PrivateKey privKey = getPrivateRSAKeyFromStorage(selfDeviceId);

        if (privKey != null) {
            String keySigned = rsa.sign(messageKey.getMessageKey(), privKey);
            messageKey.setSign(keySigned);
            return messageKey;
        }

        return null;
    }

    //verify
    public boolean verify(MessageKey messageKey){

        PublicKey pubKey = getPubKeyFromUser(messageKey, CREATOR);

        if (pubKey != null) {
            Log.d(getClass().getSimpleName(), "Verify key");
            return rsa.verify(messageKey.getSign(), messageKey.getMessageKey(), pubKey);
        }
        Log.d(getClass().getSimpleName(), "PubKey is null");
        return false;
    }

    //get own PrivateKey from LocalStorage
    public PrivateKey getPrivateRSAKeyFromStorage(long selfDeviceId){

        String RSAKEY_STORAGE_USER = RSAKEY_STORAGE + "_" + selfDeviceId;

        Context context = DatabaseManager.INSTANCE.getContext();
        SharedPreferences privKeyStorage = context.getSharedPreferences(RSAKEY_STORAGE_USER, Context.MODE_PRIVATE);
        String privKeyInBase64 = privKeyStorage.getString(PRIVATEKEY, "");

        Log.d(this.getClass().getSimpleName(),"[???] Load private Key from storage: " + RSAKEY_STORAGE_USER);
        //if Key is available
        if (privKeyInBase64 != "") {

            PrivateKey privKey = rsa.convertBase64toPrivKey(privKeyInBase64);

            if (privKey != null){
                Log.d(this.getClass().getSimpleName(),"[???] Private Key was successfully loaded from storage");
                return privKey;
            }

            Log.d(this.getClass().getSimpleName(), "[???] getting private key from storage failed");
            return null;
        }

            Log.d(this.getClass().getSimpleName(), "[???] Private Key could not be found.");
            return null;

    }

    //get a Public Key for specific user from LocalStorage
    public PublicKey getPubKeyFromUser(MessageKey messageKey, byte type) {

        String pubKeyInBase64 = null;

        //try to extract Public Key from MessageKey
        if (type == CREATOR){
                pubKeyInBase64 = messageKey.getCreatorDevice().getPublicKey();
        }
        else if (type == RECIPIENT){
                pubKeyInBase64 = messageKey.getRecipientDevice().getPublicKey();
        }
        else{
            Log.d(this.getClass().getSimpleName(), "[???] Wrong use of function: getPubKeyFromUser()");
            return null;
        }

        //convert Base64toPublicKey
        if (pubKeyInBase64 != null) {
            PublicKey pubKey = rsa.convertBase64toPubKey(pubKeyInBase64);
            if (pubKey != null) return pubKey;
        }

        Log.d(this.getClass().getSimpleName(), "[???] getting public key from storage failed");
        return null;
    }

    //get own PublicKey in Base64
    public String getPublicRSAKeyInBase64FromStorage(long selfDeviceId){

        String RSAKEY_STORAGE_USER = RSAKEY_STORAGE + "_" + selfDeviceId;

        Context context = DatabaseManager.INSTANCE.getContext();
        SharedPreferences privKeyStorage = context.getSharedPreferences(RSAKEY_STORAGE_USER, Context.MODE_PRIVATE);
        String pubKeyInBase64 = privKeyStorage.getString(PUBLICKEY, "");

        return pubKeyInBase64;
    }

}
