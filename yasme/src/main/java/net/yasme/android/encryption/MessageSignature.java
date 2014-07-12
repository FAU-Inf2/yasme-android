package net.yasme.android.encryption;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;


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

    //private String RSAKEYSTORAGE = "rsaKeyStorage"; //Storage for Private and Public Keys from user
    //private final String PUBLICKEYS = "publicKeys"; //Storage for all Public Keys of user's friends

    //Context context;
    private RSAEncryption rsa; // = new RSAEncryption();
    long selfDeviceId;
    //TODO: entscheiden, ob fuie eigene KEYS fuer userID oder deviceID gespeichert werden

    public MessageSignature(PrivateKey privKey, PublicKey pubKey, long selfDeviceId) {
        //this.context = context;
        this.selfDeviceId = selfDeviceId;
        this.rsa = new RSAEncryption(privKey,pubKey);
        //addIfNotExists UserId to the storagename, because there are more than one user on device who need a private key
        //RSAKEYSTORAGE += "_" + Long.toString(creatorDevice);
    }

    // TODO: Generating keys in YasmeDeviceRegistration
    /*
    public void generateRSAKeys(){
        rsa.generateKeyPair();
        saveRSAKeys();
    }
    */

    //save own RSAKeys
    /*
    public boolean saveRSAKeys(){
        // rsa.getPrivKeyinBase64()
        // rsa.getPubKeyinBase64()
        try {
            SharedPreferences rsakeys = context.getSharedPreferences(RSAKEYSTORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor keyeditor = rsakeys.edit();

            keyeditor.putString("privateKey", rsa.getPrivKeyinBase64());
            keyeditor.putString("publicKey", rsa.getPubKeyinBase64());

            keyeditor.commit();

            return true;
        } catch (Exception e){
            Log.d(this.getClass().getSimpleName(),"[???] saving rsa keys failed");
            Log.d(this.getClass().getSimpleName(),"[???] "+e.getMessage());
            return false;
        }
    }
    */

    //save a public Key from a friend
    public boolean savePublicKey(long deviceId, String publicKeyinBase64){
        //TODO: Save friends key to database
        return true;
    }

    //get own PrivateKey from LocalStorage
    //public PrivateKey getPrivateRSAKey(){
        //return rsa.getPrivKey();
        /*
        SharedPreferences rsakeys = context.getSharedPreferences(RSAKEYSTORAGE, Context.MODE_PRIVATE);
        String privKey_base64 = rsakeys.getString("privateKey", "");
        Log.d(this.getClass().getSimpleName(),"[???] Private Key Base64:"+privKey_base64);

        //if Key is available
        if (privKey_base64 != "") {

            try{
                //convert to byte
                byte[] privKeyBytes = Base64.decode(privKey_base64, Base64.DEFAULT);

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
        */


    //}


    //get own PublicKey from LocalStorage
    //public PublicKey getPublicRSAKey(){
        //return rsa.getPubKey();
        /*
        SharedPreferences rsakeys = context.getSharedPreferences(RSAKEYSTORAGE, Context.MODE_PRIVATE);
        String pubKey_base64 = rsakeys.getString("publicKey", "");

        //if Key is available
        if (pubKey_base64 != "") {

            try{
                //convert to byte
                byte[] publicKeyBytes = Base64.decode(pubKey_base64, Base64.DEFAULT);

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
        */
    //}

    /*
    //get own PublicKey in Base64
    public String getPublicRSAKeyInBase64(){
        SharedPreferences rsakeys = context.getSharedPreferences(RSAKEYSTORAGE, Context.MODE_PRIVATE);
        String pubKey_base64 = rsakeys.getString("publicKey", "");

        //if Key is available
        if (pubKey_base64 != "") {
            return pubKey_base64;
        }

        return null;
    }
    */

    //sign
    public String sign(String text){
        //PrivateKey privKey = getPrivateRSAKey();
        return rsa.sign(text, rsa.getPrivKey());
    }

    //verify
    public boolean verify(String signature_base64, String text_base64, PublicKey pubKey){
        return rsa.verify(signature_base64, text_base64, pubKey);
    }

    //encrypt
    public String encrypt(String text, PublicKey pubKey){
        return rsa.encrypt(text, pubKey);
    }

    //decrypt
    public String decrypt(String text){
        //PrivateKey privKey = getPrivateRSAKey();
        return rsa.decrypt(text, rsa.getPrivKey());
    }


    //get a Public Key for a specific user from server
    public String[] getPubKeyfromServer(){
       //TODO
        //gibt Array an Base64-Public-Keys zurueck
        return null;
    }

    //get a Public Key for specific user from LocalStorage
    public PublicKey getPubKeyFromUser(long deviceId){
        //TODO: Get publicKey from Database
        //get public key for the specific deviceId
        return rsa.getPubKey(); //TODO: ersetzen
    }


}
