package net.yasme.android.encryption;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

public class MessageSignatur {

	private static final String RSAKEYSTORAGE = "rsaKeyStorage";
	
	Context context;
	private RSAEncryption rsa;

	public MessageSignatur(Context context) {
		this.context = context;
	}
	
	public void generateRSAKeys(){
		rsa.generateKeyPair();
		saveRSAKeys();
		
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
	
	
	
}
