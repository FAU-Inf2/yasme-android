package de.fau.cs.mad.yasme.android.encryption;

/**
 * Created by Marco Eberl on 22.07.2014.
 */

import android.content.Context;
import android.content.SharedPreferences;
import de.fau.cs.mad.yasme.android.controller.Log;

import de.fau.cs.mad.yasme.android.entities.MessageKey;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.DebugManager;

import java.security.PrivateKey;
import java.security.PublicKey;

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

    /**
     * generate a random RSA-KeyPair (Private and Public Key)
     */
    public void generateRSAKeys(){
        rsa.generateKeyPair();
    }

    /**
     * encode generated RSA-KeyPair to base64 and store it to local storage (SharedPreferences)
     * there is a own SharedPreference for every user on the device
     *
     * @param deviceId deviceId from the user logged in currently
     * @return true/false
     */
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

            // For Developer-Devices only
            if (DebugManager.INSTANCE.isDebugMode()) {
                Log.d(getClass().getSimpleName(), "Store keys to external storage");
                DebugManager.INSTANCE.storePrivatePublicKeyToExternalStorage(rsa.getPrivKeyinBase64(),rsa.getPubKeyinBase64());
            }

            Log.d(this.getClass().getSimpleName(), "RSA Keys generated and saved");


            return true;
        } catch (Exception e){
            Log.e(this.getClass().getSimpleName(), "saving rsa keys failed");
            Log.e(this.getClass().getSimpleName(),e.getMessage());
            return false;
        }
    }

    /**
     * get the generated RSA Public Key in Base64
     * method is needed in registration when the Public Key needs to be sent to the server
     *
     * @return base64 encoded string
     */
    public String getGeneratedPubKeyInBase64(){
        return rsa.getPubKeyinBase64();
    }

    /**
     * encrypt the given messageKey using the RSA PublicKey from the recipient
     *
     * @param messageKey messageKey containing the AES-Key that should be encrypted
     * @return messageKey containing the RSA-encrypted AES-Key
     */
    public MessageKey encrypt(MessageKey messageKey){

        PublicKey pubKey = getPubKeyFromUser(messageKey, RECIPIENT);

        if (pubKey != null){
            String keyEncrypted = rsa.encrypt(messageKey.getMessageKey(), pubKey);
            messageKey.setKey(keyEncrypted);
            return messageKey;
        }

        return null;
    }

    /**
     * encrypt the given messageKey using the own RSA Private Key
     *
     * @param messageKey messageKey containing the encrypted AES-Key that should be decrypted
     * @return messageKey containing the decrypted AES-Key
     */
    public MessageKey decrypt(MessageKey messageKey){

        long selfDeviceId = messageKey.getRecipientDevice().getId();
        PrivateKey privKey = getPrivateRSAKeyFromStorage(selfDeviceId);

        if (privKey != null) {
            String key = rsa.decrypt(messageKey.getMessageKey(), privKey);
            if (key == null) {
                return null;
            }
            messageKey.setKey(key);
            return messageKey;
        }

        return null;
    }

    /**
     * sign an AES-Key using the own RSA Private Key
     *
     * @param messageKey messageKey containing the AES-Key that should be signed
     * @return  messageKey containing the signature
     */
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

    /**
     * verify the signature from given messageKey using the RSA Public Key from the creatorDevice
     *
     * @param messageKey messageKey containing the signature
     * @return true/false
     */
    public boolean verify(MessageKey messageKey){

        PublicKey pubKey = getPubKeyFromUser(messageKey, CREATOR);

        if (pubKey != null) {
            Log.d(getClass().getSimpleName(), "Verify key");
            return rsa.verify(messageKey.getSign(), messageKey.getMessageKey(), pubKey);
        }
        Log.d(getClass().getSimpleName(), "PubKey is null");
        return false;
    }

    /**
     * load own RSA Private Key from local storage
     *
     * @param selfDeviceId deviceId from the user logged in currently
     * @return PrivateKey
     */
    public PrivateKey getPrivateRSAKeyFromStorage(long selfDeviceId){

        String RSAKEY_STORAGE_USER = RSAKEY_STORAGE + "_" + selfDeviceId;

        Context context = DatabaseManager.INSTANCE.getContext();
        SharedPreferences privKeyStorage = context.getSharedPreferences(RSAKEY_STORAGE_USER, Context.MODE_PRIVATE);
        String privKeyInBase64 = privKeyStorage.getString(PRIVATEKEY, "");

        Log.d(this.getClass().getSimpleName(),"Load private Key from storage: " + RSAKEY_STORAGE_USER);

        //if Key is available
        if (privKeyInBase64 != "") {

            PrivateKey privKey = rsa.convertBase64toPrivKey(privKeyInBase64);

            if (privKey != null){
                Log.d(this.getClass().getSimpleName(),"Private Key was successfully loaded from storage");
                return privKey;
            }

            Log.d(this.getClass().getSimpleName(), "Getting private key from storage failed");
            return null;
        }

            Log.d(this.getClass().getSimpleName(), "Private Key could not be found.");
            return null;

    }

    /**
     * load the needed RSA Public Key from local storage/Device-Object
     *
     * @param messageKey messageKey containing the information about creatorDevice and recipientDevice
     * @param type define, the RSA Public Key from the recipient or the creator is needed
     * @return PublicKey
     */
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
            Log.d(this.getClass().getSimpleName(), "Wrong use of function: getPubKeyFromUser()");
            return null;
        }

        //convert Base64toPublicKey
        if (pubKeyInBase64 != null) {
            PublicKey pubKey = rsa.convertBase64toPubKey(pubKeyInBase64);
            if (pubKey != null) return pubKey;
        }

        Log.d(this.getClass().getSimpleName(), "Getting public key from storage failed");
        return null;
    }

    /*
    //get own PublicKey in Base64
    public String getPublicRSAKeyInBase64FromStorage(long selfDeviceId){

        String RSAKEY_STORAGE_USER = RSAKEY_STORAGE + "_" + selfDeviceId;

        Context context = DatabaseManager.INSTANCE.getContext();
        SharedPreferences privKeyStorage = context.getSharedPreferences(RSAKEY_STORAGE_USER, Context.MODE_PRIVATE);
        String pubKeyInBase64 = privKeyStorage.getString(PUBLICKEY, "");

        return pubKeyInBase64;
    }
    */

}
