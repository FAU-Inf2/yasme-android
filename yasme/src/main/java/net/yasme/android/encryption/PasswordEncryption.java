package net.yasme.android.encryption;

import android.util.Log;

import org.codehaus.jackson.map.deser.ValueInstantiators;

import java.security.MessageDigest;

/**
 * Created by Marco Eberl on 27.07.2014.
 */
public class PasswordEncryption extends Base64{

    private static final String HASH_ALG = "SHA-512";

    //protect against Bruto-Force-Attacks
    public String securePassword(String password){
        return SHA512(password);
    }

    public String SHA512(String password){
       try {
           MessageDigest md;
           md = MessageDigest.getInstance("SHA-512");
           byte[] hash = new byte[40];
           md.update(password.getBytes("UTF-8"));
           hash = md.digest();
           return base64Encode(hash);
       }
       catch (Exception e){
           Log.d(this.getClass().getSimpleName(), "[???] Hashing Password failed");
           Log.d(this.getClass().getSimpleName(), "[???] Error: " + e.getMessage());
       }
       return null;
    }



}
