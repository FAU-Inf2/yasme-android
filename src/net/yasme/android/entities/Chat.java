package net.yasme.android.entities;

import java.util.ArrayList;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.yasme.android.YasmeChat;
import net.yasme.android.connection.MessageTask;
import net.yasme.android.encryption.MessageEncryption;
import net.yasme.android.exception.RestServiceException;
import android.os.AsyncTask;

/**
 * Created by robert on 28.05.14.
 */
@DatabaseTable(tableName = "chatrooms")
public class Chat {
	public final static String STORAGE_PREFS = "net.yasme.andriod.STORAGE_PREFS";
	public final static String USER_ID = "net.yasme.andriod.USER_ID";
	
	@DatabaseField
	private ArrayList<Message> messages;
	@DatabaseField
	private Id lastMessageID;
	@DatabaseField(generatedId = true, id = true)
	private Id chat_id;
	
	private String user_name;
	private Id user_id;
	String url;
	
	private MessageEncryption aes;
	private MessageTask messageTask;
	public YasmeChat activity;

	/** Constructors **/
	public Chat(Id chat_id, Id user_id, String url, YasmeChat activity) {
		this.chat_id = chat_id;		
		this.user_id = user_id;
		this.activity = activity;
	
		//setup Encryption for this chat
		long creator = user_id.getId();
		long recipient = 2L;
		long devid = 3L;
		aes = new MessageEncryption(activity, chat_id, creator, recipient, devid);

		messageTask = new MessageTask(url);
		
		lastMessageID = new Id(0);
	}
	
	public Chat() {
		// ORMLite needs a no-arg constructor
	}

	/** Getters **/
	public Id getChat_id() {
		return chat_id;
	}

	public ArrayList<Message> getStoredMessages() {
		return messages;
	}

	/** Setters **/
	public void setMessages(ArrayList<Message> messages) {
		this.messages = messages;
	}

	public void setLastMessageID(long newlastMessageID) {
		lastMessageID.setId(newlastMessageID);
	}


	/** Other methods **/
	public void send(String msg) {
		new SendMessageTask().execute(msg, user_name);
	}

	public void update() {
		new GetMessageTask().execute(Long.toString(lastMessageID.getId()), Long.toString(user_id.getId()));
	}

	private class SendMessageTask extends AsyncTask<String, Void, Boolean> {
		String msg;

		protected Boolean doInBackground(String... params) {

			msg = params[0];
			
			Id uid = user_id;
			boolean result = false;
			
			//encrypt Message
			String msg_encrypted = aes.encrypt(msg);
			
			//create Message
			Message createdMessage = new Message(new User(user_name, uid),
						msg_encrypted, chat_id, aes.getKeyId());
			try {
				result = messageTask.sendMessage(createdMessage);
			} catch (RestServiceException e) {
				System.out.println(e.getMessage());
			}
			return result;
		}

		protected void onPostExecute(Boolean result) {
			if (result) {
				update();
				activity.getStatus().setText("Gesendet: " + msg);
			} else {
				activity.getStatus().setText("Senden fehlgeschlagen");
			}
		}
	}

	private class GetMessageTask extends AsyncTask<String, Void, Boolean> {
		ArrayList<Message> messages;

		/**
		 * @return Returns true if it was successful, otherwise false
		 * @param params
		 *            [0] is lastMessageID
		 * @param params
		 *            [1] is user_id
		 */
		protected Boolean doInBackground(String... params) {

			try {
				messages = messageTask.getMessage(params[0], params[1]);
			} catch (RestServiceException e) {
				e.printStackTrace();
			}
			
			if(messages == null) {
				return false;
				}
			if (messages.isEmpty()) {
				 return false;
				 }
			
			// decrypt Messages
			for (Message msg : messages) {
				msg.setMessage(new String(aes.decrypt(msg.getMessage(), msg.getKeyID())));
			}
			
			
			return true;
		}

		/**
		 * Fills the TextViews with the messages
		 * - maybe this should be done also in doInBackground
		 * 
		 * @param Gets
		 *            the result of doInBackground
		 * @param Gets
		 *            the result of doInBackground
		 */
		protected void onPostExecute(Boolean result) {
			if (result) {
				activity.updateViews(messages);
				setLastMessageID(messages.size() + lastMessageID.getId());	
			} else {
				activity.getStatus().setText("Keine neuen Nachrichten");
			}
		}
	}
}
