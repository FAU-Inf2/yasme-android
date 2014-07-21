package net.yasme.android.encryption;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
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

    //TODO: user wird nicht wirklich benoetigt
    public MessageSignature(long selfDeviceId, User user) {
        this.selfDeviceId = selfDeviceId;
        this.rsa = new RSAEncryption();
        this.user = user;
    }

    public MessageSignature(long selfDeviceId) {
        this.selfDeviceId = selfDeviceId;
        this.rsa = new RSAEncryption();
        generateRSAKeys();

    }

    public MessageSignature() {
        this.rsa = new RSAEncryption();
        generateRSAKeys();
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

            Log.d(this.getClass().getSimpleName(), "[???] RSA Keys generated and saved");


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
       //TODO: static Public Key entfernen
        /*START*/

        String pubKey_base64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxTTd8U4paCBAd640OxNQ9Drj78UlUyKQnz57EZuiLLXD5OeqGkfJoe62jxMh84z30JLdQF9m8J4NavXaCh0wVjL91NqzRPy1/SeOkcuIehJyUluP05LM+mKU+nUyFWGvelyR1Zu6YS4EaD3Kk6bLy+IPrtbwCbZM/GRQ6sOmlR3TOhk3bp4NXfgZwje8sCJdmNyBh93kO4hG9P1YPjrtq78q476cNDt8nOWz9gUPYkrUlN0+VGWKG/5nQV875sIrek8CenCk30chFmoLB40gIXlmNAx6G3LHzNjrWX6UrswFtJJ+u9cAToG9MLngCvJVkBcxWbIi0KZ+XC7fqkYUFQIDAQAB";
        KeyFactory kf = null;
        PublicKey pubKey = null;
        try {
            kf = KeyFactory.getInstance("RSA");
            byte[] publicKeyBytes = Base64.decode(pubKey_base64, Base64.DEFAULT);
            pubKey = kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*END*/

        //PublicKey pubKey = getPubKeyFromUser(deviceIdFromRecipient);

        if (pubKey != null){
            return rsa.encrypt(text, pubKey);
        } else {
            return text;
            //TODO: Diesen else-Zweig wieder entfernen
        }
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

        //if Key is available
        if (rsaKey != null) {

            try{

                String pubKeyInBase64 = rsaKey.getPublicKey();

                //convert to byte
                byte[] publicKeyBytes = Base64.decode(pubKeyInBase64, Base64.DEFAULT);

                //convert to PublicKey
                KeyFactory kf = KeyFactory.getInstance("RSA");
                PublicKey pubKey = kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

                Log.d(this.getClass().getSimpleName(),"[???] Public Key for Device "+deviceId + "could be found.");


                return pubKey;

            } catch (Exception e){
                Log.d(this.getClass().getSimpleName(),"[???] getting public key from storage failed");
                Log.d(this.getClass().getSimpleName(),"[???] "+e.getMessage());
                return null;
            }
        }

        Log.d(this.getClass().getSimpleName(),"[???] Public Key for Device "+deviceId + "could not be found.");


        return null;

    }


    //get a Public Key for a specific user from server
    public String[] getPubKeyfromServer(){
        //TODO
        //gibt Array an Base64-Public-Keys zurueck
        return null;
    }

}
