package de.fau.cs.mad.yasme.android.encryption;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import android.util.Log;
import android.widget.Toast;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.controller.Toaster;

public class RSAEncryption extends de.fau.cs.mad.yasme.android.encryption.Base64 {

    private static final int KEYSIZE = 2048;
    private static final String SIGNATURE_MODE = "SHA256withRSA";
    private static final String MODE = "RSA/ECB/PKCS1Padding";

    private KeyPair keys = null;

    public RSAEncryption() {

    }

    //generate RSAKeys
    public KeyPair generateKeyPair(){

        try{

            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            SecureRandom sr = new SecureRandom();
            keyGen.initialize(KEYSIZE, sr);

            keys = keyGen.generateKeyPair();
            return keys;

        }catch(Exception e){
            Log.d(this.getClass().getSimpleName(),"[???] "+e.getMessage());
            Log.d(this.getClass().getSimpleName(),"[???] RSA Keys could not be generated.");
            return null;
        }

    }

    private KeyPair generateStaticKeys(){
        try{
            String privKey_base64 = "MIIEwAIBADANBgkqhkiG9w0BAQEFAASCBKowggSmAgEAAoIBAQDFNN3xTiloIEB3rjQ7E1D0OuPvxSVTIpCfPnsRm6IstcPk56oaR8mh7raPEyHzjPfQkt1AX2bwng1q9doKHTBWMv3U2rNE/LX9J46Ry4h6EnJSW4/Tksz6YpT6dTIVYa96XJHVm7phLgRoPcqTpsvL4g+u1vAJtkz8ZFDqw6aVHdM6GTdung1d+BnCN7ywIl2Y3IGH3eQ7iEb0/Vg+Ou2rvyrjvpw0O3yc5bP2BQ9iStSU3T5UZYob/mdBXzvmwit6TwJ6cKTfRyEWagsHjSAheWY0DHobcsfM2OtZfpSuzAW0kn671wBOgb0wueAK8lWQFzFZsiLQpn5cLt+qRhQVAgMBAAECggEBAJBd8HBboPJqUeeLbw8kR+pTRlRcBcQDlM4PJncwcRu8AOmNMrjEfvZ908WP4xXxx+U37qLWdHyHqBj6XCF1VtJzx+DQLda+DdiC4dsXnhSHdouWHgdr+4T9METeEMkYVycDp3sx4tKLpoxNWASZYa4jEwoSrWzeuSM3qQkEK+oSv3eU9rukkLB1q2KDhqMzg3Bd6N11HRs8ZQ5ys49AuRx6ZBCTLq+mc05IW1vYckvDI1CTskYStiquJWxe9KJ3szeBajJEIg45EqLEliGzBb2eTSBFCKZ+1eCynDtjOQ9EKdZvJQG3/Gr9ws9200aTejO5wv8rNhB7RNCRvJy1s80CgYEA4pXyXj3nCwichHM2g5EIKL/xjw/37Kv26/aXHn7X7YtJRAL/0HLMJpWSN+o5DNmDFJGo0AvIcj/H1xtoOnHtg2zEbe1XzDrwz19x5WknNv8LUq+AlhrCr5JPoT1Q/req++G+dvvEItovqpIJg3Gfdsi7a9M0rf8yZZER7ko79u8CgYEA3s6R3QNMrdbn6aLTpgMwhIi+gNavZ5CKpYwyUWg9TOzZYZ0+tjnnDsIw1y06MJ/l+5g2/ils6gc1RBJelKOpeYtPauKImBYhM3+Y/LpxVR0NGmdBw871ykCglL5A2JIWYyt6k7o4YuT57iMfT/Cm9wz1xFCBeRap3tCCkbt2hTsCgYEAij/5UL0uYpIPhdUSVvY/5zUuOx8AI5zNHS4pCIdWUm7g9ilqUpIotAYg4BL+WjPBAeTZ/o6h7+uwkDP9xWNMCxtrQrNFFayEz9KpmNMvBSRakUnaCDwtu5hnE7do2vHP1r3nS4vUIXvFB6rxOQ0zwfM6P9DvXJbP9h6stRsAOWsCgYEAr/nTdw5OF9dvIDb8l1hZj7Q5UqU9sLyW5R4P+AAuun0vTEvX5jFhb2StEqakGReRm9+jP6cUYNsElRk1Ho0NI/SF61O0svp3iqcy/Bl9vc3ONZZseO0TcIUOz6xcpzDrAbSrgdZJBsL3K8EN0COwm9vemQlE2ZCu5k8lcVjwyVUCgYEAi7/M3Q5Vv9BdEa+ytDGoRSDJB9FYVyhbBn1c+fSlnH4tKoWetfIj78UsKksiIOjiAFiSsJxbMD9fzS32Sj60J+a8Z1YFCxRbvXYGx556vuE+z/7+ZV0tKPWXTK9kTX48zxTftwId+RhP7nt4UukVfGey6gnq5VWIJ5rTwIfjfro=";
            String pubKey_base64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxTTd8U4paCBAd640OxNQ9Drj78UlUyKQnz57EZuiLLXD5OeqGkfJoe62jxMh84z30JLdQF9m8J4NavXaCh0wVjL91NqzRPy1/SeOkcuIehJyUluP05LM+mKU+nUyFWGvelyR1Zu6YS4EaD3Kk6bLy+IPrtbwCbZM/GRQ6sOmlR3TOhk3bp4NXfgZwje8sCJdmNyBh93kO4hG9P1YPjrtq78q476cNDt8nOWz9gUPYkrUlN0+VGWKG/5nQV875sIrek8CenCk30chFmoLB40gIXlmNAx6G3LHzNjrWX6UrswFtJJ+u9cAToG9MLngCvJVkBcxWbIi0KZ+XC7fqkYUFQIDAQAB";

            KeyFactory kf = KeyFactory.getInstance("RSA");

            byte[] privKeyBytes = base64Decode(privKey_base64);
            PrivateKey privKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privKeyBytes));

            byte[] publicKeyBytes = base64Decode(pubKey_base64);
            PublicKey pubKey = kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

            setKeyPair(privKey, pubKey);
            return keys;

        }catch(Exception e){
            Log.d(this.getClass().getSimpleName(),"[???] "+e.getMessage());
            return null;
        }

    }

    public String getPubKeyinBase64(){
        Key pubKey = keys.getPublic();
        return base64Encode(pubKey.getEncoded());
    }

    public String getPrivKeyinBase64(){
        Key privKey = keys.getPrivate();
        return base64Encode(privKey.getEncoded());
    }

    public void setKeyPair(PrivateKey privKey, PublicKey pubKey){
        keys = new KeyPair(pubKey, privKey);
    }

    //sign
    public String sign(String text, PrivateKey privKey) {
        byte[] signatured = null;

        try {
            Signature sig = Signature.getInstance(SIGNATURE_MODE);
            sig.initSign(privKey);
            sig.update(text.getBytes());

            signatured = sig.sign();

            return base64Encode(signatured);
        } catch (Exception e){
            Log.d(this.getClass().getSimpleName(),"[???] sign failed");
            Log.d(this.getClass().getSimpleName(),"[???] "+e.getMessage());
            return null;
        }

    }


    //verify the signature
    public boolean verify(String signature_base64, String text_base64, PublicKey pubKey) {

        try {
            byte[] signature = base64Decode(signature_base64);

            Signature sig = Signature.getInstance(SIGNATURE_MODE);
            sig.initVerify(pubKey);
            sig.update(text_base64.getBytes());
            return sig.verify(signature);

        } catch (Exception e){
            Log.d(this.getClass().getSimpleName(),"[???] verify failed");
            Log.d(this.getClass().getSimpleName(),"[???]"+e.getMessage());
            return false;
        }

    }

    //encrypt
    public String encrypt(String text, PublicKey pubKey) {
        byte[] encrypted = null;

        try {
            Cipher cipher = Cipher.getInstance(MODE);
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            encrypted = cipher.doFinal(text.getBytes());
            //encrypted = cipher.doFinal(text.getBytes("UTF-8")); //--> wenn normaler Text (kein Schluessel) encrypted wird

            Log.d(this.getClass().getSimpleName(),"[???] RSA Encryption successful.");

            return base64Encode(encrypted);
        } catch (Exception e){
            Log.d(this.getClass().getSimpleName(),"[???] encrypt failed");
            Log.d(this.getClass().getSimpleName(),"[???]" + e.getMessage());
            return "Couldn't be encrypted: "+ text;
        }

    }

    //decrypt
    public String decrypt(String encrypted, PrivateKey privKey) {
        try{
            byte[] encrypted_decode = base64Decode(encrypted);
            Cipher cipher = Cipher.getInstance(MODE);
            cipher.init(Cipher.DECRYPT_MODE, privKey);
            byte[] decrypted = cipher.doFinal(encrypted_decode);
            Log.d(this.getClass().getSimpleName(),"[???] RSA Decryption successful.");
            return new String(decrypted);
        } catch (Exception e){
            Log.d(this.getClass().getSimpleName(),"[???] Key-decryption failed: " + encrypted + " PrivKey: " + privKey.toString());
            Log.d(this.getClass().getSimpleName(),"[???] " + e.getMessage());
            Toaster.getInstance().toast(R.string.key_decryption_failed, Toast.LENGTH_LONG);
            return null;
        }
    }

    public PublicKey convertBase64toPubKey(String base64){
        try{
            //convert to byte
            byte[] publicKeyBytes = base64Decode(base64);
            //convert to PublicKey
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey pubKey = kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
            return pubKey;

        } catch (Exception e){
            Log.d(this.getClass().getSimpleName(),"[???] Converting Base64 to PublicKey failed");
            Log.d(this.getClass().getSimpleName(),"[???] "+e.getMessage());
            return null;
        }

    }

    public PrivateKey convertBase64toPrivKey(String base64){
        try{
            //convert to byte
            byte[] privKeyBytes = base64Decode(base64);
            //convert to PrivateKey
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privKeyBytes));

            return privKey;

        } catch (Exception e){
            Log.d(this.getClass().getSimpleName(),"[???] Converting Base64 to PrivateKey failed");
            Log.d(this.getClass().getSimpleName(),"[???] "+e.getMessage());
            return null;
        }
    }

}
