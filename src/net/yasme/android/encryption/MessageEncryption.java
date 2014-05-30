package net.yasme.android.encryption;

import net.yasme.android.R;
import net.yasme.android.connection.KeyTask;
import net.yasme.android.entities.Id;
import net.yasme.android.entities.MessageKey;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Base64;

//um den Schlüssel zum Verschlüsseln abzurufen muss bekannt sein, mit welcher KeyId der Chat verschlüsselt wird
//hier wird vorausgesetzt, dass zum Verschlüsseln nur ein Key vorhanden is
//ACHTUNG: Zum Entschlüsseln können aber mehrere vorhanden sein
//Tabelle für Chats --> KeyID nötig
//Tabelle für KeyID --> Key nötig


//ChatKeyMapping						Chat-ID
//Chat-ID	|	Key-ID					Key-ID	|	Key
//---------------------					--------------------------------
//	2		|	1							1	|	KEY, IV

//WICHTIG: Bevor Nachrichten vom Server geholt werden, müssen neue Keys vom Server geholt werden und diese Tabellen aktualisiert werden

public class MessageEncryption {

	Id keyid; //contains the latest keyid for encryption
	byte[] currentkey; //needed for restoring the current key
	byte[] currentiv;
	Id chatid;
	long creator;
	long recipient;
	long devid;
	String url;
	Context context;

	AESEncryption aes; 
	private KeyTask keytask;

	
	public MessageEncryption(Context context, Id chatid, long creator, long recipient, long devid){
		this.context = context;
		this.chatid = chatid;
		
		//TODO: set Ressource ID for ChatKeyMapping
		SharedPreferences sharedPref = context.getSharedPreferences("ChatKeyMapping",Context.MODE_PRIVATE);
		
		//if no old key for this chat, then generate a new one, beginning with ID "1"
		if (!sharedPref.contains(Long.toString(chatid.getId()))){
			keyid = new Id(1);
			aes = new AESEncryption("geheim");
			saveKey(context, chatid, keyid);
			sendKey(context.getResources().getString(R.string.server_url), chatid, keyid, creator, recipient, devid);
			//###DEBUG
			System.out.println("[???]: KeyID "+keyid.getId()+" für Chat "+chatid.getId() + " wurde erstellt und gespeichert");
			System.out.println("[???]: Key wurde gesendet");
			//###
		}
		
		//if old key is already available
		else{
			
			//check, which Key is need to encrypt
			long keyidfromstorage = sharedPref.getLong(Long.toString(chatid.getId()), 0);
			keyid = new Id(keyidfromstorage);
			
			//get Key from storage
			String[] base64arr = getKeyfromLocalStorage(context, chatid, keyid);
			//if Key is available
			if(base64arr != null){
				String base64key = base64arr[0];
				String base64iv = base64arr[1];
				this.currentkey = Base64.decode(base64key.getBytes(), Base64.DEFAULT);
				this.currentiv = Base64.decode(base64iv.getBytes(), Base64.DEFAULT);
				
				aes = new AESEncryption(currentkey, currentiv);
				//###DEBUG
				System.out.println("[???]: Key "+ keyid.getId()+" für Chat "+ chatid.getId()+" wurde geladen");
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
		System.out.println("[???] thiskeyid:"+this.keyid.getId());
		System.out.println("[???] keyid:"+keyid);

		if (this.keyid.getId() == keyid){
			return aes.decrypt(encrypted);
		}
		
		//another key is needed
		else{
			//get Key from storage
			String[] base64arr = getKeyfromLocalStorage(context, chatid, new Id(keyid));
			//if Key is available
			if(base64arr != null){
				String base64key = base64arr[0];
				String base64iv = base64arr[1];
				byte[] key = Base64.decode(base64key.getBytes(), Base64.DEFAULT);
				byte[] iv = Base64.decode(base64iv.getBytes(), Base64.DEFAULT);
				
				//get older key needed for decryption
				aes = new AESEncryption(key, iv);
				System.out.println("[???]: alter Key wurde zum Entschlüsseln geladen");
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
		SharedPreferences chatPref = context.getSharedPreferences("ChatKeyMapping",Context.MODE_PRIVATE);

		SharedPreferences.Editor keyeditor = keyPref.edit();
		SharedPreferences.Editor chateditor = chatPref.edit();
		//safe Key-Id for this Chat-ID
		chateditor.putLong(Long.toString(chatid.getId()), keyid.getId());
		//safe Key+IV, which belongs to Key-Id
		keyeditor.putString(Long.toString(keyid.getId()), aes.getKey()+","+aes.getIV());

		keyeditor.commit();	
		chateditor.commit();	

	}
	
	public String[] getKeyfromLocalStorage(Context context, Id chatid, Id keyid){
		SharedPreferences sharedPref = context.getSharedPreferences(Long.toString(chatid.getId()),Context.MODE_PRIVATE);
		if(sharedPref.contains(Long.toString(keyid.getId()))){
			String base64 = sharedPref.getString(Long.toString(keyid.getId()), "");
			String[] base64arr = base64.split(",");
			
			//[0] --> Key in Base64, [1] --> IV in Base64
			return base64arr;
		}
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
			//TO-DO
			//save Key on local device
		}
	}
}
