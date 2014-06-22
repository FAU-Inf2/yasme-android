package net.yasme.android.encryption;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

public class MessageSignatur {

    private String RSAKEYSTORAGE = "rsaKeyStorage"; //Storage for Private and Public Keys from user
    private final String PUBLICKEYS = "publicKeys"; //Storage for all Public Keys of user's friends

    Context context;
    private RSAEncryption rsa = new RSAEncryption();
    long creatorDevice;
    //TODO: entscheiden, ob fuie eigene KEYS fuer userID oder deviceID gespeichert werden

    public MessageSignatur(Context context, long creatorDevice) {
        this.context = context;
        this.creatorDevice = creatorDevice;
        //add UserId to the storagename, because there are more than one user on device who need a private key
        RSAKEYSTORAGE += "_" + Long.toString(creatorDevice);
    }

    public void generateRSAKeys(){
        rsa.generateKeyPair();
        saveRSAKeys();
        //TODO: send Public Key to Server
    }

    //save own RSAKeys
    public boolean saveRSAKeys(){
        //TODO: Loesche alle alten Eintraege???
        try {
            SharedPreferences rsakeys = context.getSharedPreferences(RSAKEYSTORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor keyeditor = rsakeys.edit();

            keyeditor.putString("privateKey", rsa.getPrivKeyinBase64());
            keyeditor.putString("publicKey", rsa.getPubKeyinBase64());

            keyeditor.commit();

            return true;
        } catch (Exception e){
            System.out.println("[???] saving rsa keys failed");
            System.out.println("[???] "+e.getMessage());
            return false;
        }
    }

    //save a public Key from a friend
    public boolean savePublicKey(long deviceId, String publicKeyinBase64){
        //TODO
        //speichere Key in SharedPreferendes PublicKeys
        return true;
    }

    //get own PrivateKey from LocalStorage
    public PrivateKey getPrivateRSAKey(){
        SharedPreferences rsakeys = context.getSharedPreferences(RSAKEYSTORAGE, Context.MODE_PRIVATE);
        String privKey_base64 = rsakeys.getString("privateKey", "");
        System.out.println("[???] Private Key Base64:"+privKey_base64);

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
                System.out.println("[???] getting private key from storage failed");
                System.out.println("[???] "+e.getMessage());
                return null;
            }
        }

        return null;

    }

    //get own PublicKey from LocalStorage
    public PublicKey getPublicRSAKey(){
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
                System.out.println("[???] getting public key from storage failed");
                System.out.println("[???] "+e.getMessage());
                return null;
            }
        }

        return null;

    }

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

    //sign
    public String sign(String text){
        PrivateKey privKey = getPrivateRSAKey();
        return rsa.sign(text, privKey);
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
        PrivateKey privKey = getPrivateRSAKey();
        return rsa.decrypt(text, privKey);
    }


    //get a Public Key for a specific user from server
    public String[] getPubKeyfromServer(){
       //TODO
        //gibt Array an Base64-Public-Keys zurueck
        return null;
    }

    //get a Public Key for specific user from LocalStorage
    public PublicKey getPubKeyFromUser(long deviceId){
        //TODO:
        //get public key for the specific deviceId
        return getPublicRSAKey(); //TODO: ersetzen
    }


}
