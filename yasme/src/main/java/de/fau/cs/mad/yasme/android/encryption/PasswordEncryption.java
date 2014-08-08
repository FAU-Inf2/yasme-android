package de.fau.cs.mad.yasme.android.encryption;

import android.util.Log;

import de.fau.cs.mad.yasme.android.entities.User;

import java.security.MessageDigest;


/**
 * Created by Marco Eberl on 27.07.2014.
 */
public class PasswordEncryption{

    private static final String SALT = "Y45M3";
    private User user;

    //delay Brute-Force-Attacks
    //protect against "using same password for several services"
    public PasswordEncryption(User user) {
        this.user = user;
    }

    public User securePassword(){
        String secure = getSecurePassword();
        user.setPw(secure);
        return user;
    }

    public String getSecurePassword(){
        return SHA512(salt(SALT,user.getPw()));
    }

    private String SHA512(String password){
       try {
           MessageDigest md = MessageDigest.getInstance("SHA-512");
           md.update(password.getBytes("UTF-8"));
           byte[] hash = md.digest();
           Base64 base64 = new Base64();
           return new String(base64.base64Encode(hash));
       }
       catch (Exception e){
           Log.d(this.getClass().getSimpleName(), "[???] Hashing Password failed");
           Log.d(this.getClass().getSimpleName(), "[???] Error: " + e.getMessage());
       }
       return null;
    }

    private String salt(String salt, String password){
        String passwordSalted = salt+password;
        return passwordSalted;
    }
}
