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

        RSAKEY_STORAGE += "_"+deviceId;

        try {

            Context context = DatabaseManager.INSTANCE.getContext();
            SharedPreferences privKeyStorage = context.getSharedPreferences(RSAKEY_STORAGE, Context.MODE_PRIVATE);
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
    //recipientDevice in MessageKey is needed
    public MessageKey encrypt(MessageKey messageKey){
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

        //PublicKey pubKey = getPubKeyFromUser(messageKey, RECIPIENT);

        if (pubKey != null){
            String keyEncrypted = rsa.encrypt(messageKey.getMessageKey(), pubKey);
            messageKey.setKey(keyEncrypted);
            return messageKey;
        }

        return null;
    }

    //decrypt
    public MessageKey decrypt(MessageKey messageKey){

        //TODO: static Public Key entfernen
        /*START*/

        String privKey_base64 = "MIIEwAIBADANBgkqhkiG9w0BAQEFAASCBKowggSmAgEAAoIBAQDFNN3xTiloIEB3rjQ7E1D0OuPvxSVTIpCfPnsRm6IstcPk56oaR8mh7raPEyHzjPfQkt1AX2bwng1q9doKHTBWMv3U2rNE/LX9J46Ry4h6EnJSW4/Tksz6YpT6dTIVYa96XJHVm7phLgRoPcqTpsvL4g+u1vAJtkz8ZFDqw6aVHdM6GTdung1d+BnCN7ywIl2Y3IGH3eQ7iEb0/Vg+Ou2rvyrjvpw0O3yc5bP2BQ9iStSU3T5UZYob/mdBXzvmwit6TwJ6cKTfRyEWagsHjSAheWY0DHobcsfM2OtZfpSuzAW0kn671wBOgb0wueAK8lWQFzFZsiLQpn5cLt+qRhQVAgMBAAECggEBAJBd8HBboPJqUeeLbw8kR+pTRlRcBcQDlM4PJncwcRu8AOmNMrjEfvZ908WP4xXxx+U37qLWdHyHqBj6XCF1VtJzx+DQLda+DdiC4dsXnhSHdouWHgdr+4T9METeEMkYVycDp3sx4tKLpoxNWASZYa4jEwoSrWzeuSM3qQkEK+oSv3eU9rukkLB1q2KDhqMzg3Bd6N11HRs8ZQ5ys49AuRx6ZBCTLq+mc05IW1vYckvDI1CTskYStiquJWxe9KJ3szeBajJEIg45EqLEliGzBb2eTSBFCKZ+1eCynDtjOQ9EKdZvJQG3/Gr9ws9200aTejO5wv8rNhB7RNCRvJy1s80CgYEA4pXyXj3nCwichHM2g5EIKL/xjw/37Kv26/aXHn7X7YtJRAL/0HLMJpWSN+o5DNmDFJGo0AvIcj/H1xtoOnHtg2zEbe1XzDrwz19x5WknNv8LUq+AlhrCr5JPoT1Q/req++G+dvvEItovqpIJg3Gfdsi7a9M0rf8yZZER7ko79u8CgYEA3s6R3QNMrdbn6aLTpgMwhIi+gNavZ5CKpYwyUWg9TOzZYZ0+tjnnDsIw1y06MJ/l+5g2/ils6gc1RBJelKOpeYtPauKImBYhM3+Y/LpxVR0NGmdBw871ykCglL5A2JIWYyt6k7o4YuT57iMfT/Cm9wz1xFCBeRap3tCCkbt2hTsCgYEAij/5UL0uYpIPhdUSVvY/5zUuOx8AI5zNHS4pCIdWUm7g9ilqUpIotAYg4BL+WjPBAeTZ/o6h7+uwkDP9xWNMCxtrQrNFFayEz9KpmNMvBSRakUnaCDwtu5hnE7do2vHP1r3nS4vUIXvFB6rxOQ0zwfM6P9DvXJbP9h6stRsAOWsCgYEAr/nTdw5OF9dvIDb8l1hZj7Q5UqU9sLyW5R4P+AAuun0vTEvX5jFhb2StEqakGReRm9+jP6cUYNsElRk1Ho0NI/SF61O0svp3iqcy/Bl9vc3ONZZseO0TcIUOz6xcpzDrAbSrgdZJBsL3K8EN0COwm9vemQlE2ZCu5k8lcVjwyVUCgYEAi7/M3Q5Vv9BdEa+ytDGoRSDJB9FYVyhbBn1c+fSlnH4tKoWetfIj78UsKksiIOjiAFiSsJxbMD9fzS32Sj60J+a8Z1YFCxRbvXYGx556vuE+z/7+ZV0tKPWXTK9kTX48zxTftwId+RhP7nt4UukVfGey6gnq5VWIJ5rTwIfjfro=";
        KeyFactory kf = null;
        PrivateKey privKey = null;
        try {
            kf = KeyFactory.getInstance("RSA");
            byte[] privKeyBytes = Base64.decode(privKey_base64, Base64.DEFAULT);
            privKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privKeyBytes));
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*END*/

        //long selfDeviceId = messageKey.getRecipientDevice().getId();
        //PrivateKey privKey = getPrivateRSAKeyFromStorage(selfDeviceId);

        if (privKey != null) {
            String key = rsa.decrypt(messageKey.getMessageKey(), privKey);
            messageKey.setKey(key);
            return messageKey;
        }

        return null;
    }

    //sign
    //creatorDevice in MessageKey is needed
    public MessageKey sign(MessageKey messageKey){
        //TODO: static Private Key entfernen
        /*START*/

        String privKey_base64 = "MIIEwAIBADANBgkqhkiG9w0BAQEFAASCBKowggSmAgEAAoIBAQDFNN3xTiloIEB3rjQ7E1D0OuPvxSVTIpCfPnsRm6IstcPk56oaR8mh7raPEyHzjPfQkt1AX2bwng1q9doKHTBWMv3U2rNE/LX9J46Ry4h6EnJSW4/Tksz6YpT6dTIVYa96XJHVm7phLgRoPcqTpsvL4g+u1vAJtkz8ZFDqw6aVHdM6GTdung1d+BnCN7ywIl2Y3IGH3eQ7iEb0/Vg+Ou2rvyrjvpw0O3yc5bP2BQ9iStSU3T5UZYob/mdBXzvmwit6TwJ6cKTfRyEWagsHjSAheWY0DHobcsfM2OtZfpSuzAW0kn671wBOgb0wueAK8lWQFzFZsiLQpn5cLt+qRhQVAgMBAAECggEBAJBd8HBboPJqUeeLbw8kR+pTRlRcBcQDlM4PJncwcRu8AOmNMrjEfvZ908WP4xXxx+U37qLWdHyHqBj6XCF1VtJzx+DQLda+DdiC4dsXnhSHdouWHgdr+4T9METeEMkYVycDp3sx4tKLpoxNWASZYa4jEwoSrWzeuSM3qQkEK+oSv3eU9rukkLB1q2KDhqMzg3Bd6N11HRs8ZQ5ys49AuRx6ZBCTLq+mc05IW1vYckvDI1CTskYStiquJWxe9KJ3szeBajJEIg45EqLEliGzBb2eTSBFCKZ+1eCynDtjOQ9EKdZvJQG3/Gr9ws9200aTejO5wv8rNhB7RNCRvJy1s80CgYEA4pXyXj3nCwichHM2g5EIKL/xjw/37Kv26/aXHn7X7YtJRAL/0HLMJpWSN+o5DNmDFJGo0AvIcj/H1xtoOnHtg2zEbe1XzDrwz19x5WknNv8LUq+AlhrCr5JPoT1Q/req++G+dvvEItovqpIJg3Gfdsi7a9M0rf8yZZER7ko79u8CgYEA3s6R3QNMrdbn6aLTpgMwhIi+gNavZ5CKpYwyUWg9TOzZYZ0+tjnnDsIw1y06MJ/l+5g2/ils6gc1RBJelKOpeYtPauKImBYhM3+Y/LpxVR0NGmdBw871ykCglL5A2JIWYyt6k7o4YuT57iMfT/Cm9wz1xFCBeRap3tCCkbt2hTsCgYEAij/5UL0uYpIPhdUSVvY/5zUuOx8AI5zNHS4pCIdWUm7g9ilqUpIotAYg4BL+WjPBAeTZ/o6h7+uwkDP9xWNMCxtrQrNFFayEz9KpmNMvBSRakUnaCDwtu5hnE7do2vHP1r3nS4vUIXvFB6rxOQ0zwfM6P9DvXJbP9h6stRsAOWsCgYEAr/nTdw5OF9dvIDb8l1hZj7Q5UqU9sLyW5R4P+AAuun0vTEvX5jFhb2StEqakGReRm9+jP6cUYNsElRk1Ho0NI/SF61O0svp3iqcy/Bl9vc3ONZZseO0TcIUOz6xcpzDrAbSrgdZJBsL3K8EN0COwm9vemQlE2ZCu5k8lcVjwyVUCgYEAi7/M3Q5Vv9BdEa+ytDGoRSDJB9FYVyhbBn1c+fSlnH4tKoWetfIj78UsKksiIOjiAFiSsJxbMD9fzS32Sj60J+a8Z1YFCxRbvXYGx556vuE+z/7+ZV0tKPWXTK9kTX48zxTftwId+RhP7nt4UukVfGey6gnq5VWIJ5rTwIfjfro=";
        KeyFactory kf = null;
        PrivateKey privKey = null;
        try {
            kf = KeyFactory.getInstance("RSA");
            byte[] privKeyBytes = Base64.decode(privKey_base64, Base64.DEFAULT);
            privKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privKeyBytes));
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*END*/

        //long selfDeviceId = messageKey.getCreatorDevice().getId();
        //PrivateKey privKey = getPrivateRSAKeyFromStorage(selfDeviceId);

        if (privKey != null) {
            String keySigned = rsa.sign(messageKey.getMessageKey(), privKey);
            messageKey.setSign(keySigned);
            return messageKey;
        }

        return null;
    }

    //verify
    //creatorDevice in MessageKey is needed
    public boolean verify(MessageKey messageKey){

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

        //PublicKey pubKey = getPubKeyFromUser(messageKey, CREATOR);


        if (pubKey != null) {
            return rsa.verify(messageKey.getSign(), messageKey.getMessageKey(), pubKey);
        }

        return false;
    }

    //get own PrivateKey from LocalStorage
    public PrivateKey getPrivateRSAKeyFromStorage(long selfDeviceId){

        RSAKEY_STORAGE += "_"+selfDeviceId;

        Context context = DatabaseManager.INSTANCE.getContext();
        SharedPreferences privKeyStorage = context.getSharedPreferences(RSAKEY_STORAGE, Context.MODE_PRIVATE);
        String privKeyInBase64 = privKeyStorage.getString(PRIVATEKEY, "");


        //if Key is available
        if (privKeyInBase64 != "") {

            PrivateKey privKey = rsa.convertBase64toPrivKey(privKeyInBase64);

            if (privKey != null){
                Log.d(this.getClass().getSimpleName(),"[???] Private Key was successfully loaded from storage");
                return privKey;
            }

            Log.d(this.getClass().getSimpleName(), "[???] getting public key from storage failed");
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

        RSAKEY_STORAGE += "_"+selfDeviceId;

        Context context = DatabaseManager.INSTANCE.getContext();
        SharedPreferences privKeyStorage = context.getSharedPreferences(RSAKEY_STORAGE, Context.MODE_PRIVATE);
        String pubKeyInBase64 = privKeyStorage.getString(PUBLICKEY, "");

        return pubKeyInBase64;
    }

    //TODO: wird die Methode hier benoetigt?
    /*
    //save public key from a user
    public boolean savePublicKeyFromUser(long deviceId, String publicKeyinBase64){
        // TODO: save to DeviceDB
        //RSAKey pubKey = new RSAKey(deviceId, publicKeyinBase64, selfUser);
        //db.getRsaKeyDAO().addOrUpdate(pubKey);
        return true;
    }
    */

}
