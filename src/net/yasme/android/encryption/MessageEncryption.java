package net.yasme.android.encryption;

import net.yasme.android.R;
import net.yasme.android.connection.KeyTask;
import net.yasme.android.entities.Id;
import net.yasme.android.entities.MessageKey;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Base64;

//um den Schluessel zum Verschluesseln abzurufen muss bekannt sein, mit welcher KeyId der Chat verschluesselt wird
//hier wird vorausgesetzt, dass zum Verschluesseln nur ein Key vorhanden is
//ACHTUNG: Zum Entschluesseln koennen aber mehrere vorhanden sein
//Mapping: Chats --> KeyID
//Tabelle: KeyID --> Key


//ChatKeyMapping						Chat-ID: 2
//Chat-ID	|	Key-ID					Key-ID	|	Key
//---------------------					--------------------------------
//	2		|	1							1	|	KEY, IV
//											2	| 	KEY, IV



//TO-DO: Bevor Nachrichten vom Server geholt werden, muessen neue Keys vom Server geholt werden und diese Tabellen aktualisiert werden

public class MessageEncryption {

	Id keyid; //contains the latest keyid for encryption
	byte[] currentkey; //needed for restoring the current key, if decryption need more than one key
	byte[] currentiv; //needed for restoring the current IV, if decryption need more than one key
	Id chatid;
	long creator;
	long recipient;
	long devid;
	String url;
	Context context;
	
	private final String CHATKEYMAPPING = "ChatKeyMapping"; //tablename for chatkeymapping

	AESEncryption aes; 
	private KeyTask keytask;

	
	public MessageEncryption(Context context, Id chatid, long creator, long recipient, long devid){
		this.context = context;
		this.chatid = chatid;
		
		SharedPreferences sharedPref = context.getSharedPreferences(CHATKEYMAPPING,Context.MODE_PRIVATE);
		
		//if no old key for this chat, then generate a new one, beginning with ID "1"
		if (!sharedPref.contains(Long.toString(chatid.getId()))){
			keyid = new Id(1);
			aes = new AESEncryption("geheim");
			saveKey(context, chatid, keyid);
			sendKey(context.getResources().getString(R.string.server_url), chatid, keyid, creator, recipient, devid);
			
			//###DEBUG
			System.out.println("[???]: KeyID "+keyid.getId()+" fuer Chat "+chatid.getId() + " wurde erstellt und gespeichert und an Server gesendet");
			//###
		}
		
		//if old key is already available
		else{
			
			//check, which Key is need to encrypt
			long keyidfromstorage = sharedPref.getLong(Long.toString(chatid.getId()), 0);
			keyid = new Id(keyidfromstorage);
			
			//get Key from storage
			byte[][] keydata = getKeyfromLocalStorage(context, chatid, keyid);
			//if Key is available
			if(keydata != null){
				this.currentkey = keydata[0];
				this.currentiv = keydata[1];
				
				aes = new AESEncryption(currentkey, currentiv);
				//###DEBUG
				System.out.println("[???]: Key "+ keyid.getId()+" fuer Chat "+ chatid.getId()+" wurde geladen");
				///###
			}
			
			//TO-DO:
			//What happens, if the needed key is not available
			//is this a real scenario?
		}
	}
		
	
	//encrypt
	public String encrypt(String text) {
		return aes.encrypt(text);
	}
	
	public String decrypt(String encrypted, long keyid){
		System.out.println("[???] Decrypt with:");
		System.out.println("[???] aktuelle KEYID:"+this.keyid.getId());
		System.out.println("[???] benoetigte KEYID:"+keyid);

		if (this.keyid.getId() == keyid){
			return aes.decrypt(encrypted);
		}
		
		//another key is needed
		else{
			//get Key from storage
			byte[][] keydata = getKeyfromLocalStorage(context, chatid, new Id(keyid));
			//if Key is available
			if(keydata != null){
				byte[] key = keydata[0];
				byte[] iv = keydata[1];
				
				//get older key needed for decryption
				aes = new AESEncryption(key, iv);
				System.out.println("[???]: alter Key wurde zum Entschl�sseln geladen");
				String decrypted = aes.decrypt(encrypted);
				
				//restore the current key needed for encryption
				aes = new AESEncryption(this.currentkey, this.currentiv);
				
				return decrypted;
			}
				return "Key for Decryption could not be found";
		}

	}
	
	public long getKeyId(){
		return this.keyid.getId();
	}
	
	//send Key to server
	public boolean sendKey(String url, Id chatid, Id keyid, long creator, long recipient, long devid){
		this.url = url;
		this.chatid = chatid;
		this.keyid = keyid;
		this.creator = creator;
		this.recipient = recipient;
		this.devid = devid;
		new SendKeyTask().execute();
		return true;
	}
	
	//save needed key for chatid, and save key for keyid
	public void saveKey(Context context, Id chatid, Id keyid){
		SharedPreferences keyPref = context.getSharedPreferences(Long.toString(chatid.getId()),Context.MODE_PRIVATE);
		SharedPreferences chatPref = context.getSharedPreferences(CHATKEYMAPPING,Context.MODE_PRIVATE);

		SharedPreferences.Editor keyeditor = keyPref.edit();
		SharedPreferences.Editor chateditor = chatPref.edit();
		//safe Key-Id for this Chat-ID
		chateditor.putLong(Long.toString(chatid.getId()), keyid.getId());
		//safe Key+IV, which belongs to Key-Id
		keyeditor.putString(Long.toString(keyid.getId()), aes.getKey()+","+aes.getIV());

		keyeditor.commit();	
		chateditor.commit();	
		
		//update current key
		this.currentkey = aes.getKeyinByte();
		this.currentiv = aes.getIVinByte();

	}
	
	public byte[][] getKeyfromLocalStorage(Context context, Id chatid, Id keyid){
		SharedPreferences sharedPref = context.getSharedPreferences(Long.toString(chatid.getId()),Context.MODE_PRIVATE);
		if(sharedPref.contains(Long.toString(keyid.getId()))){
			String base64 = sharedPref.getString(Long.toString(keyid.getId()), "");
						
			//if Key is available
			if(base64 != ""){
				String[] base64arr = base64.split(",");
				String base64key = base64arr[0];
				String base64iv = base64arr[1];
				
				//convert to byte
				byte[][] keydata = new byte[2][16];
				keydata[0] = Base64.decode(base64key.getBytes(), Base64.DEFAULT);
				keydata[1] = Base64.decode(base64iv.getBytes(), Base64.DEFAULT);
			
				//[0] --> Key in Base64, [1] --> IV in Base64
				return keydata;
			}
			//Key is not available
			return null;
		}
		//Key is not available
		return null;
		
	}
	
	//Async-Task for sending Key to Server
	private class SendKeyTask extends AsyncTask<String, Void, Boolean> {
		String key;

		protected Boolean doInBackground(String... params) {
			
			try{
				
				key = aes.getKey();
				byte encType = 1;
				
				//setup MessageKey-Object
				MessageKey keydata = new MessageKey(keyid.getId(), creator, recipient, devid, key, encType, "test");
				
				//send MessageKey-Object
				keytask = new KeyTask(url);
				keytask.saveKey(keydata);
				
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			return true;
		}

		protected void onPostExecute(Boolean result) {
			
		}
	}
}
