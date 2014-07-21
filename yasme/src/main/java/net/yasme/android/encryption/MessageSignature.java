package net.yasme.android.encryption;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import net.yasme.android.entities.User;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.storage.RSAKey;


/*
# KeyVerteilung:
# Bei Regisirierung holt User sich von allen Kontakten die öffentliche Keys
# Bei Registrierung schickt User RSA-Key an Server, sein Device benachrichtigt alle bekannten Kontake
# bei Hinzufuegen eines neuen Kontakts wird dieser ebenfalls benachrichtigt
# --> Kontakt holt Key vom Server bzw. App muss selber RSA-Key des Kontakts holen
#
# ansonsten beim Nachrichten senden/empfangen, AsyncTask, falls Key nicht vorhanden sein sollte

# Methode1: Abholen RSA-Key von einem bestimmten Usern (Array) holen
# Methode2: Senden des RSA-Keys an Server (mit Liste der notwendigen Empfänger --> Kontakte)
# Methode3: Informiere Kontakt, dass er meinen RSA-Key abholen soll
 */

public class MessageSignature {

    private final String PRIVATEKEYS = "rsaKeyStorage"; //Storage for Private and Public Keys from user
    //private final String PUBLICKEYS = "publicKeys"; //Storage for all Public Keys of user's friends

    //Context context;
    private RSAEncryption rsa; // = new RSAEncryption();
    private DatabaseManager db = DatabaseManager.INSTANCE;
    private User user;

    long selfDeviceId;
    //TODO: entscheiden, ob fuie eigene KEYS fuer userID oder deviceID gespeichert werden

    public MessageSignature(long selfDeviceId, User user) {
        //this.context = context;
        this.selfDeviceId = selfDeviceId;
        this.rsa = new RSAEncryption();
        this.user = user;
        //addIfNotExists UserId to the storagename, because there are more than one user on device who need a private key
        //RSAKEYSTORAGE += "_" + Long.toString(creatorDevice);
    }

    public MessageSignature() {

    }

    // TODO: Generating keys in YasmeDeviceRegistration

    public void generateRSAKeys(){
        rsa.generateKeyPair();
        saveRSAKeys();
    }

    //save own RSAKeys
    public boolean saveRSAKeys(){

        try {
            //save Public Key in Database
            savePublicKey(selfDeviceId, rsa.getPubKeyinBase64(), user);

            //save Private Key in SharedPreferences
            Context context = DatabaseManager.INSTANCE.getContext();
            SharedPreferences privKeyStorage = context.getSharedPreferences(PRIVATEKEYS, Context.MODE_PRIVATE);
            SharedPreferences.Editor keyeditor = privKeyStorage.edit();

            if (privKeyStorage.getString(Long.toString(selfDeviceId), "") != ""){
                keyeditor.remove(Long.toString(selfDeviceId));
            }
            keyeditor.putString(Long.toString(selfDeviceId), rsa.getPrivKeyinBase64());
            keyeditor.commit();

            return true;
        } catch (Exception e){
            Log.d(this.getClass().getSimpleName(), "[???] saving rsa keys failed");
            Log.d(this.getClass().getSimpleName(),"[???] "+e.getMessage());
            return false;
        }
    }

    //verify
    public boolean verify(String signature_base64, String text_base64, long deviceIdFromSender){
        PublicKey pubKey = getPubKeyFromUser(deviceIdFromSender);
        return rsa.verify(signature_base64, text_base64, pubKey);
    }

    //encrypt
    public String encrypt(String text, long deviceIdFromRecipient){
        PublicKey pubKey = getPubKeyFromUser(deviceIdFromRecipient);
        return rsa.encrypt(text, pubKey);
    }

    //decrypt
    public String decrypt(String text){
        PrivateKey privKey = getPrivateRSAKey();
        return rsa.decrypt(text, privKey);
    }

    //sign
    public String sign(String text){
        PrivateKey privKey = getPrivateRSAKey();
        return rsa.sign(text, privKey);
    }


    //save a public Key from a friend
    public boolean savePublicKey(long deviceId, String publicKeyinBase64, User friend){
        RSAKey pubKey = new RSAKey(deviceId, publicKeyinBase64, friend);
        db.getRsaKeyDAO().addOrUpdate(pubKey);
        return true;
    }

    //get own PrivateKey from LocalStorage
    public PrivateKey getPrivateRSAKey(){
        Context context = DatabaseManager.INSTANCE.getContext();
        SharedPreferences privKeyStorage = context.getSharedPreferences(PRIVATEKEYS, Context.MODE_PRIVATE);

        String privKeyInBase64 = privKeyStorage.getString(Long.toString(selfDeviceId), "");
        Log.d(this.getClass().getSimpleName(),"[???] Private Key for Device "+selfDeviceId+"was successfully loaded from storage");

        //if Key is available
        if (privKeyInBase64 != "") {

            try{
                //convert to byte
                byte[] privKeyBytes = Base64.decode(privKeyInBase64, Base64.DEFAULT);

                //convert to PrivateKey
                KeyFactory kf = KeyFactory.getInstance("RSA");
                PrivateKey privKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privKeyBytes));

                return privKey;

            } catch (Exception e){
                Log.d(this.getClass().getSimpleName(),"[???] getting private key from storage failed");
                Log.d(this.getClass().getSimpleName(),"[???] "+e.getMessage());
                return null;
            }
        }

        return null;
    }


    //get own PublicKey from LocalStorage
    public PublicKey getPublicRSAKey(){
        return getPubKeyFromUser(selfDeviceId);
    }


    //get own PublicKey in Base64
    public String getPublicRSAKeyInBase64(){
        RSAKey rsaKey = db.getRsaKeyDAO().get(selfDeviceId);
        String pubKeyInBase64 = rsaKey.getPublicKey();
        return pubKeyInBase64;
    }


    //get a Public Key for specific user from LocalStorage
    public PublicKey getPubKeyFromUser(long deviceId){
        //TODO: sucht get wirklich nach devideId?
        RSAKey rsaKey = db.getRsaKeyDAO().get(deviceId);
        String pubKeyInBase64 = rsaKey.getPublicKey();

        //if Key is available
        if (pubKeyInBase64 != null) {

            try{
                //convert to byte
                byte[] publicKeyBytes = Base64.decode(pubKeyInBase64, Base64.DEFAULT);

                //convert to PublicKey
                KeyFactory kf = KeyFactory.getInstance("RSA");
                PublicKey pubKey = kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

                return pubKey;

            } catch (Exception e){
                Log.d(this.getClass().getSimpleName(),"[???] getting public key from storage failed");
                Log.d(this.getClass().getSimpleName(),"[???] "+e.getMessage());
                return null;
            }
        }

        return null;

    }


    //get a Public Key for a specific user from server
    public String[] getPubKeyfromServer(){
        //TODO
        //gibt Array an Base64-Public-Keys zurueck
        return null;
    }

}
