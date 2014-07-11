package net.yasme.android.encryption;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import android.util.Base64;
import android.util.Log;

public class RSAEncryption {

    private static final int KEYSIZE = 2048;
    private static final String SIGNATURE_MODE = "SHA256withRSA";
    private static final String MODE = "RSA/ECB/PKCS1Padding";

    private KeyPair keys = null;

    public RSAEncryption(PrivateKey privKey, PublicKey pubKey) {
        setKeyPair(privKey, pubKey);
    }

    //generate RSAKeys
    public void generateKeyPair(){

        try{
//            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
//            SecureRandom sr = new SecureRandom();
//            keyGen.initialize(KEYSIZE, sr);
//
//            keys = keyGen.generateKeyPair();

            //TODO: remove generating static Key
        	/* START */

            String privKey_base64 = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDw/axWRQcWq6AZ+OsFOYe/x6V7";
            String pubKey_base64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA8P2sVkUHFqugGfjrBTmHv8ele2pRS4/0";

            byte[] privKeyBytes = Base64.decode(privKey_base64, Base64.DEFAULT);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privKeyBytes));

            byte[] publicKeyBytes = Base64.decode(pubKey_base64, Base64.DEFAULT);
            PublicKey pubKey = kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

            setKeyPair(privKey, pubKey);

            /* END */
        }catch(Exception e){
            Log.d(this.getClass().getSimpleName(),"[???] "+e.getMessage());
            Log.d(this.getClass().getSimpleName(),"[???] RSA Keys could not be generated.");
        }

    }

    public PublicKey getPubKey(){
        return keys.getPublic();
    }

    public PrivateKey getPrivKey(){
        return keys.getPrivate();
    }

    public String getPubKeyinBase64(){
        Key pubKey = keys.getPublic();
        return Base64.encodeToString(pubKey.getEncoded(), Base64.DEFAULT);
    }

    public String getPrivKeyinBase64(){
        Key privKey = keys.getPrivate();
        return Base64.encodeToString(privKey.getEncoded(), Base64.DEFAULT);
    }
/*
    public byte[] getPrivKeyinByte(){
        return keys.getPrivate().getEncoded();
    }

    public byte[] getPubKeyinByte(){
        return keys.getPublic().getEncoded();
    }
*/
    public void setKeyPair(PrivateKey privKey, PublicKey pubKey){
        keys = new KeyPair(pubKey, privKey);
    }


    //sign
    public String sign(String text, PrivateKey privKey) {
        byte[] signatured = null;

        try {
            Signature sig = Signature.getInstance(SIGNATURE_MODE);
            sig.initSign(privKey);
            sig.update(text.getBytes("UTF-8"));

            signatured = sig.sign();

            return Base64.encodeToString(signatured, Base64.DEFAULT);
        } catch (Exception e){
            Log.d(this.getClass().getSimpleName(),"[???] sign failed");
            Log.d(this.getClass().getSimpleName(),"[???] "+e.getMessage());
            return null;
        }

    }


    //verify the signature
    public boolean verify(String signature_base64, String text_base64, PublicKey pubKey) {

        try {
            byte[] signature = Base64.decode(signature_base64.getBytes("UTF-8"), Base64.DEFAULT);
            byte[] encrypted = Base64.decode(text_base64.getBytes("UTF-8"), Base64.DEFAULT);

            Signature sig = Signature.getInstance(SIGNATURE_MODE);
            sig.initVerify(pubKey);
            //TODO: Laut Tutorial muss hier das Ergebnis nach der Entschluesselung stehen
            sig.update(encrypted);

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


            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception e){
            Log.d(this.getClass().getSimpleName(),"[???] encrypt failed");
            Log.d(this.getClass().getSimpleName(),"[???]" + e.getMessage());
            return "Couldn't be encrypted: "+ text;
        }

    }

    //decrypt
    public String decrypt(String encrypted, PrivateKey privKey) {
        byte[] decrypted = null;

        try{
            byte[] encrypted_decode = Base64.decode(encrypted.getBytes(), Base64.DEFAULT);
            Cipher cipher = Cipher.getInstance(MODE);
            cipher.init(Cipher.DECRYPT_MODE, privKey);
            decrypted = cipher.doFinal(encrypted_decode);

            return new String(decrypted);
        } catch (Exception e){
            Log.d(this.getClass().getSimpleName(),"[???] decrypt failed");
            Log.d(this.getClass().getSimpleName(),"[???] "+e.getMessage());
            return "Couldn't be decrypted: "+ encrypted;
        }

    }

}

