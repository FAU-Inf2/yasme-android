package net.yasme.android.encryption;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;



public class AESEncryption {
	
	private SecretKey key = null;
	private IvParameterSpec iv = null;
	
	public AESEncryption(){
		//generate AES-Key and Inital-Vector, if necessary
		//otherwise get it from store (NOT YET IMPLEMENTED)
		key = generateKey();
		iv = generateIV();
	}
	
	
	public AESEncryption(String password){
		//generate AES-Key from given password
		key = generateKey(password);
		iv = generateIV();
	}
	
	public AESEncryption(byte[] key, byte[] iv){
		this.key = new SecretKeySpec(key,"AES");
		this.iv = new IvParameterSpec(iv);
	}
	
	//generate Initial-Vector
	public IvParameterSpec generateIV(){
		//random IV
		//SecureRandom random = new SecureRandom();
        //byte INITIAL_IV[] = new byte[16];
		//generate random 16 byte IV, AES is always 16bytes
        //random.nextBytes(INITIAL_IV);
		
        //static IV 
      	byte[] INITIAL_IV = {1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 20, 30, 40, 50, 60, 1};
       
		return new IvParameterSpec(INITIAL_IV);
	}
	
	//generate AESKey
	public SecretKey generateKey(){
		SecretKey keySpec = null;
		try{
			SecureRandom sr = new SecureRandom();
			byte[] key = new byte[16];
			sr.nextBytes(key);
			keySpec = new SecretKeySpec(key,"AES");
		}catch(Exception e){}
		
		return keySpec;
	}
	//generate AESKey from given password
	public SecretKey generateKey(String password){
		SecretKey keySpec = null;
		try{
			byte[] pw = (password).getBytes("UTF-8");
			MessageDigest sha = MessageDigest.getInstance("SHA-256");
			pw = sha.digest(pw);
			pw = Arrays.copyOf(pw, 16); //use only the first 128Bits
			keySpec = new SecretKeySpec(pw, "AES");
		}
		catch (Exception e){}
		
		return keySpec;
	}
	
	public String getIV(){
		return Base64.encodeToString(iv.getIV(), Base64.DEFAULT);
	}
	
	public String getKey(){
		return Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
	}
	
	//encrypt
	public String encrypt(String text) {
		byte[] encrypted = null;
		try {
			encrypted = crypt(text.getBytes("UTF-8"), Cipher.ENCRYPT_MODE);
			return Base64.encodeToString(encrypted, Base64.DEFAULT);
		} catch (Exception e){
			System.out.println(e.getMessage());
			return "Couldn't be encrypted: "+ text;
		}
		
	}
	//decrypt
	public String decrypt(String encrypted) {
		System.out.println("[???]: decrypt"+encrypted);
		try{
			byte[] encrypted_decode = Base64.decode(encrypted.getBytes(), Base64.DEFAULT);
			return new String(crypt(encrypted_decode, Cipher.DECRYPT_MODE));
		} catch (Exception e){
			System.out.println(e.getMessage());
			return "Couldn't be decrypted: "+ encrypted;
		}
				
	}
	
	
			
	// One method for both. "mode" decides, whether it makes encryption or decryption.
		public byte[] crypt(byte[] in, int mode) {
			Cipher cipher;
			byte[] out = new byte[0];
			try {
				cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
				cipher.init(mode, key, iv);		
				out = cipher.doFinal(in);
				
			} catch (Exception e) {}
			return out;
		}
		
		
		
}



