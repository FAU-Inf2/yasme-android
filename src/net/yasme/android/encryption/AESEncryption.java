package net.yasme.android.encryption;

import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import android.util.Base64;

public class AESEncryption {
	
	private SecretKey key = null;
	
	public AESEncryption(){
		try{
			KeyGenerator kg = KeyGenerator.getInstance("AES");
			kg.init(128);
			key = kg.generateKey();
		}
		catch (Exception e){}
	}
	
	public AESEncryption(String password){
		try{
			byte[] pwinbyte = (password).getBytes("UTF-8");
			MessageDigest sha = MessageDigest.getInstance("SHA-256");
			pwinbyte = sha.digest(pwinbyte);
			pwinbyte = Arrays.copyOf(pwinbyte, 16); //use only the first 128Bits
			key = new SecretKeySpec(pwinbyte, "AES");
		}
		catch (Exception e){}
	}
	
//	//generate random AESKey
//	public static SecretKey getAESKey(){
//		SecretKey keySpec = null;
//		try{
//			KeyGenerator kg = KeyGenerator.getInstance("AES");
//			kg.init(128);
//			keySpec = kg.generateKey();
//			return keySpec;
//		}
//		catch (Exception e){}
//		return keySpec;
//	}
	
	//generate AESKey from password
//	public static SecretKey getAESKey(String password){
//		SecretKey keySpec = null;
//		try{
//			byte[] key = (password).getBytes("UTF-8");
//			MessageDigest sha = MessageDigest.getInstance("SHA-256");
//			key = sha.digest(key);
//			key = Arrays.copyOf(key, 16); //use only the first 128Bits
//			keySpec = new SecretKeySpec(key, "AES");
//		}
//		catch (Exception e){}
//		return keySpec;
//	}
	//encrypt
	public String encrypt(String text) {
		byte[] encrypted = crypt(text.getBytes(), Cipher.ENCRYPT_MODE);
		return Base64.encodeToString(encrypted, Base64.DEFAULT);
	}
	//decrypt
	public byte[] decrypt(String encrypted) {
		byte[] encrypted_decode = Base64.decode(encrypted.getBytes(), Base64.DEFAULT);
		return crypt(encrypted_decode, Cipher.DECRYPT_MODE);
		
	}
			
	// One method for both. "mode" decides, whether it makes encryption or decryption.
	public byte[] crypt(byte[] in, int mode) {
		Cipher cipher;
		byte[] out = new byte[0];
		try {
			cipher = Cipher.getInstance("AES");
			cipher.init(mode, key);		
			out = cipher.doFinal(in);
			
		} catch (Exception e) {}
		return out;
	}
			
}
