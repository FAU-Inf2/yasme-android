package net.yasme.android.encryption;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;

import javax.crypto.Cipher;

import android.util.Base64;

public class RSAEncryption {

private static final int KEYSIZE = 2048;
private KeyPair keys = null;	

	//generate RSAKeys
	public KeyPair generateKeyPair(){
			
		try{
		    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		    SecureRandom sr = new SecureRandom();
		    keyGen.initialize(KEYSIZE, sr);
		    
		    keys = keyGen.generateKeyPair();
		    
		    return keys;
		}catch(Exception e){}
		
		return null;
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
	
	public byte[] getPrivKeyinByte(){
		return keys.getPrivate().getEncoded();
	}
	
	public byte[] getPubKeyinByte(){
		return keys.getPublic().getEncoded();
	}
	
	public void setKeyPair(PrivateKey privKey, PublicKey pubKey){
		keys = new KeyPair(pubKey, privKey);
	}
	
	
	//sign
	public String sign(String text, PrivateKey privKey) {
		byte[] signatured = null;
		
		try {
			//TODO: MD5 or better SHA?
			Signature sig = Signature.getInstance("SHA256withRSA");
		    sig.initSign(privKey);
		    sig.update(text.getBytes("UTF-8"));
		    
		    signatured = sig.sign();
		    
			return Base64.encodeToString(signatured, Base64.DEFAULT);
		} catch (Exception e){
			System.out.println(e.getMessage());
			return null;
		}
		
	}
	
	
	//verify the signature
	public boolean verify(String signature_base64, String encrypted_base64, PublicKey pubKey) {
	
		try {
			byte[] signature = Base64.decode(signature_base64.getBytes("UTF-8"), Base64.DEFAULT);
			byte[] encrypted = Base64.decode(encrypted_base64.getBytes("UTF-8"), Base64.DEFAULT);
			
			//TODO: MD5 or better SHA?
			Signature sig = Signature.getInstance("SHA256withRSA");
		    sig.initVerify(pubKey);
		    //TODO: Laut Tutorial muss hier das Ergebnis nach der Entschlüsselung stehen
		    sig.update(encrypted);
		    
		    return sig.verify(signature); 
		     
		} catch (Exception e){
			System.out.println(e.getMessage());
			return false;
		}
		
	}
	
	
	//encrypt
	public String encrypt(String text, PublicKey pubKey) {
		byte[] encrypted = null;
		
		try {
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);		
			encrypted = cipher.doFinal(text.getBytes("UTF-8"));
			
			return Base64.encodeToString(encrypted, Base64.DEFAULT);
		} catch (Exception e){
			System.out.println(e.getMessage());
			return "Couldn't be encrypted: "+ text;
		}
		
	}
	
	//decrypt
	public String decrypt(String encrypted, PrivateKey privKey) {
		byte[] decrypted = null;

		try{
			byte[] encrypted_decode = Base64.decode(encrypted.getBytes("UTF-8"), Base64.DEFAULT);
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, privKey);		
			decrypted = cipher.doFinal(encrypted_decode);
			
			return new String(decrypted);
		} catch (Exception e){
			System.out.println(e.getMessage());
			return "Couldn't be decrypted: "+ encrypted;
		}
				
	}
		
}
