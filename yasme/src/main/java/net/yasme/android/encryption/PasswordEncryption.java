package net.yasme.android.encryption;

import android.util.Log;

import org.codehaus.jackson.map.deser.ValueInstantiators;

import java.security.MessageDigest;

/**
 * Created by Marco Eberl on 27.07.2014.
 */
public class PasswordEncryption extends Base64{

    private static final String HASH_ALG = "SHA-512";
    private static final String SALT = "Y45M3";

    //delay Brute-Force-Attacks
    //protect against "using same password for several services"
    public String securePassword(String password){
        return SHA512(salt(password));
    }

    private String SHA512(String password){
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

    private String salt(String password){
        String passwordSalted = password+SALT;
        return passwordSalted;
    }



}
