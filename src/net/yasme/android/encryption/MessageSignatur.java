package net.yasme.android.encryption;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

public class MessageSignatur {

	private String RSAKEYSTORAGE = "rsaKeyStorage"; //Storage for Private and Public Keys from user
	
	Context context;
	private RSAEncryption rsa;
	long userId;

	public MessageSignatur(Context context, long userId) {
		this.context = context;
		this.userId = userId;
		//add UserId to the storagename, because there are more than one user on device who need a private key
		RSAKEYSTORAGE += "_" + Long.toString(userId); 
	}
	
	public void generateRSAKeys(){
		rsa.generateKeyPair();
		saveRSAKeys();
		//TODO: send Public Key to Server
	}
	
	//save RSAKeys
	public boolean saveRSAKeys(){
		//TODO: Loesche alle alten Eintraege???
		try {
			SharedPreferences rsakeys = context.getSharedPreferences(RSAKEYSTORAGE, Context.MODE_PRIVATE);
	        SharedPreferences.Editor keyeditor = rsakeys.edit();
	
	        keyeditor.putString("privateKey", rsa.getPrivKeyinBase64());
	        keyeditor.putString("publicKey", rsa.getPubKeyinBase64());
	
	        keyeditor.commit();
	        return true;
		} catch (Exception e){
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	//get PrivateKey from LocalStorage
	public PrivateKey getPrivateRSAKey(){
		SharedPreferences rsakeys = context.getSharedPreferences(RSAKEYSTORAGE, Context.MODE_PRIVATE);
		String privKey_base64 = rsakeys.getString("privateKey", "");
		
		 //if Key is available
        if (privKey_base64 != "") {
                
            try{
            	//convert to byte
                byte[] privKeyBytes = Base64.decode(privKey_base64, Base64.DEFAULT);
                
                //convert to PrivateKey
    			KeyFactory kf = KeyFactory.getInstance("RSA");
    			PrivateKey privKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privKeyBytes));
    			//PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
    			
    			return privKey;
    			
    		} catch (Exception e){
    			System.out.println(e.getMessage());
    			return null;
    		}		
        }
        
        return null;
     
	}
	
	//sign
	public String sign(String text){
		PrivateKey privKey = getPrivateRSAKey();
		return rsa.sign(text, privKey);
	}
	
	//verify
	public boolean verify(String signature_base64, String text_base64, PublicKey pubKey){
		return rsa.verify(signature_base64, text_base64, pubKey);
	}
	
	//get a Public Key for a specific server
	public String[] getPubKeyfromServer(){
		//gibt Array an Base64-Public-Keys zurück
		return null;
	}
	
	//get a Public Key from LocalStorage
	public PublicKey getPubKey(long userId){
		return null;
	}
	
	
}
