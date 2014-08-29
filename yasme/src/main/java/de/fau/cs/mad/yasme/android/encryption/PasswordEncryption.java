package de.fau.cs.mad.yasme.android.encryption;

import de.fau.cs.mad.yasme.android.controller.Log;

import de.fau.cs.mad.yasme.android.entities.User;

import java.security.MessageDigest;


/**
 * Created by Marco Eberl <mfrankie89@aol.de> on 27.07.2014.
 */
public class PasswordEncryption{

    private static final String SALT = "Y45M3";
    private User user;

    public PasswordEncryption(User user) {
        this.user = user;
    }

    /**
     * get a secure password (hashed and salted)
     *
     * @return user-object containing the secure password
     */
    public User securePassword(){
        String secure = getSecurePassword();
        user.setPw(secure);
        return user;
    }


    /**
     * get a secure password (hashed and salted)
     *
     * @return hashed and salted password as String
     */
    public String getSecurePassword(){
        return SHA512(salt(SALT,user.getPw()));
    }


    /**
     * hash the password using SHA-512
     *
     * @param password password that should be hashed
     * @return hashed String
     */
    private String SHA512(String password){
       try {
           MessageDigest md = MessageDigest.getInstance("SHA-512");
           md.update(password.getBytes("UTF-8"));
           byte[] hash = md.digest();
           Base64 base64 = new Base64();
           return new String(base64.base64Encode(hash));
       }
       catch (Exception e){
           Log.d(this.getClass().getSimpleName(), "Hashing Password failed");
           Log.d(this.getClass().getSimpleName(), "Error: " + e.getMessage());
       }
       return null;
    }

    /**
     * salt the password
     *
     * @param salt salt-value that should be used
     * @param password password that should be secured
     * @return salted password
     */
    private String salt(String salt, String password){
        String passwordSalted = salt+password;
        return passwordSalted;
    }
}
