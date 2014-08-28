package de.fau.cs.mad.yasme.android.encryption;

import de.fau.cs.mad.yasme.android.controller.Log;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import de.fau.cs.mad.yasme.android.entities.MessageKey;

public class AESEncryption extends de.fau.cs.mad.yasme.android.encryption.Base64 {

    private static final int KEYSIZE = 16; //128bit
    private static final String HASH_ALG = "SHA-256";
    private static final String MODE = "AES/CBC/PKCS5Padding";

    private SecretKey key = null;
	private IvParameterSpec iv = null;

    /**
     * initialize AES-Encryption with random Key and InititalVector
     */
	public AESEncryption() {
        key = generateKey();
        iv = generateIV();
	}

    /**
     * initialize AES-Encryption with a Key generated from given password
     */
    public AESEncryption(String password) {
		key = generateKey(password);
		iv = generateIV();
	}

    /**
     * initialize AES-Encryption with a Key/InitalVector extracting from given MessageKey-Object
     */
    public AESEncryption(MessageKey messageKey) {
        String keyBase64 = messageKey.getMessageKey();
        String ivBase64 = messageKey.getInitVector();

        byte[] keyBytes = base64Decode(keyBase64);
        byte[] ivBytes = base64Decode(ivBase64);

        // convert key needed for decryption
        key = new SecretKeySpec(keyBytes, "AES");
        iv = new IvParameterSpec(ivBytes);
    }

    /**
     * generate a random Inital Vector (needed in CBC-Mode)
     *
     * @return InitialVector
     */

	public IvParameterSpec generateIV() {
		//random IV
		SecureRandom random = new SecureRandom();
		byte INITIAL_IV[] = new byte[KEYSIZE];
        random.nextBytes(INITIAL_IV);

		//static IV
		//byte[] INITIAL_IV = { 1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 20, 30, 40, 50, 60, 1 };

		return new IvParameterSpec(INITIAL_IV);
	}

    /**
     * generate random AES-Key
     *
     * @return AES-Key
     */
	public SecretKey generateKey() {
		SecretKey keySpec = null;
		try {
			SecureRandom sr = new SecureRandom();
			byte[] key = new byte[KEYSIZE];
			sr.nextBytes(key);
			keySpec = new SecretKeySpec(key, "AES");
		} catch (Exception e) {
            Log.e(this.getClass().getSimpleName(),e.getMessage());
        }


		return keySpec;
	}

    /**
     * generate a AES-Key from given password (hash the password and use the first 128bits as AES-Key)
     *
     * @param password password from user
     * @return AES-Key
     */
	public SecretKey generateKey(String password) {
		SecretKey keySpec = null;
		try {
			byte[] pw = (password).getBytes("UTF-8");
			MessageDigest sha = MessageDigest.getInstance(HASH_ALG);
			pw = sha.digest(pw);
			pw = Arrays.copyOf(pw, KEYSIZE); // use only the first 128Bits
			keySpec = new SecretKeySpec(pw, "AES");
		} catch (Exception e) {
            Log.e(this.getClass().getSimpleName(),e.getMessage());
		}

		return keySpec;
	}


    /**
     * Getter-methods
     *
     * @return Key/Iv depending on the type that is needed
     */

    public SecretKey getKey(){
        return key;
    }

    public IvParameterSpec getIV(){
        return iv;
    }

    public String getIVinBase64() {
        return base64Encode(iv.getIV());
    }

    public String getKeyinBase64() {
        return base64Encode(key.getEncoded());
    }

    public byte[] getIVinByte() {
        return iv.getIV();
    }

    public byte[] getKeyinByte() {
        return key.getEncoded();
    }

    /**
     * convert a Base64-decoded Key to AES-Key (Type: SecretKey)
     *
     * @param base64 Base64-decoded AES-Key
     * @return AES-Key
     */
	public SecretKey base64toKey(String base64){
		byte[] keyBytes = base64Decode(base64);
		return new SecretKeySpec(keyBytes, "AES");
	}

    /**
     * convert a Base64-decoded InitialVector to Initial Vector (Type: IvParameterSpec)
     *
     * @param base64 Base64-decoded InitialVector
     * @return InitialVector
     */
	public IvParameterSpec base64toIV(String base64){
		byte[] IvBytes = base64Decode(base64);
		return new IvParameterSpec(IvBytes);
	}

    /**
     * encrypt a string and encode the result to base64
     *
     * @param text message that should be encrypted
     * @return encrypted string, encoded in base64
     */
	public String encrypt(String text) {
		byte[] encrypted = null;
		try {
			
			Cipher cipher;
			cipher = Cipher.getInstance(MODE);
            Log.d(getClass().getSimpleName(),"Encrypt with key: " + Arrays.toString(key.getEncoded()));
            Log.d(getClass().getSimpleName(),"Encrypt with iv: " + Arrays.toString(iv.getIV()));
			cipher.init(Cipher.ENCRYPT_MODE, key, iv);		
			encrypted = cipher.doFinal(text.getBytes("UTF-8"));
				
			return base64Encode(encrypted);

		} catch (Exception e) {
			Log.e(this.getClass().getSimpleName(),e.getMessage());
			return "Couldn't be encrypted: " + text;
		}

	}

    /**
     * decode a base64-encoded string and decrypt it
     *
     * @param encrypted encrypted base64-encoded string
     * @return decrypted string
     */
	public String decrypt(String encrypted) {
		byte[] decrypted = null;

		try{
			byte[] encrypted_decode = base64Decode(encrypted, "UTF-8");

			Cipher cipher;
			cipher = Cipher.getInstance(MODE);
            Log.d(getClass().getSimpleName(),"Decrypt with key: " + Arrays.toString(key.getEncoded()));
            Log.d(getClass().getSimpleName(),"Decrypt with iv: " + Arrays.toString(iv.getIV()));
			cipher.init(Cipher.DECRYPT_MODE, key, iv);		
			decrypted = cipher.doFinal(encrypted_decode);
			
			return new String(decrypted);
		} catch (Exception e) {
			Log.e(this.getClass().getSimpleName(),e.getMessage());
			return "Couldn't be decrypted: " + encrypted;
		}

	}
	
		


}
