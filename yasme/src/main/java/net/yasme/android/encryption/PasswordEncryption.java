package net.yasme.android.encryption;

import android.util.Log;

import java.security.MessageDigest;

/**
 * Created by Marco Eberl on 27.07.2014.
 */
public class PasswordEncryption {

    private static final String HASH_ALG = "SHA-512";

    public String securePassword(String password){
        return "test";
    }

    public byte[] hash(String password){
       try {
           MessageDigest md;
           md = MessageDigest.getInstance("SHA-512");
           byte[] sha1hash = new byte[40];
           md.update(password.getBytes("UTF-8"));
           sha1hash = md.digest();
           return sha1hash;
       }
       catch (Exception e){
           Log.d(this.getClass().getSimpleName(), "[???] Hashing Password failed");
           Log.d(this.getClass().getSimpleName(), "[???] Error: " + e.getMessage());
       }
       return null;
    }

}
