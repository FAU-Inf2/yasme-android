package net.yasme.android.entities;

import java.util.ArrayList;
import net.yasme.android.YasmeChat;
import net.yasme.android.connection.MessageTask;
import net.yasme.android.encryption.AESEncryption;
import net.yasme.android.exception.RestServiceException;
import android.os.AsyncTask;


/**
 * Created by robert on 28.05.14.
 */
public class Chat {

	private String chat_id;
	private ArrayList<Message> messages;
	//private long lastMessageID;
	public long index;
	
	private String user_name;
	private String user_id;
	
	private AESEncryption aes;
	private MessageTask messageTask;
	public YasmeChat activity;
	
	
	/** Constructors **/
	public Chat(String user_name, String user_id, String url, YasmeChat activity) {
		this.user_name = user_name;
		this.user_id = user_id;
		aes = new AESEncryption("geheim");
		messageTask = new MessageTask(url);
		this.activity = activity;
	}
	
	
	/** Getters **/
	public String getChat_id() {
		return chat_id;
	}

	public ArrayList<Message> getStoredMessages() {
		return messages;
	}
	
	public String getUser_name() {
		return user_name;
	}
	
	public String getUser_id() {
		return user_id;
	}

	/** Setters **/
	public void setMessages(ArrayList<Message> messages) {
		this.messages = messages;
	}
	
	
	/** Other methods **/
	public void send(String msg) {
		new SendMessageTask().execute(msg, user_name);
		update();
	}
	
	public void update() {
		new GetMessageTask().execute(user_id);
	}
	
	private class SendMessageTask extends AsyncTask<String, Void, Boolean> {
		String msg;

		protected Boolean doInBackground(String... params) {

			msg = params[0];
			// encrypt message
			String msg_encrypted = aes.encrypt(msg);

			// creating message object
			long uid = Long.parseLong(user_id);
			boolean result = false;
			try {
				result = messageTask.sendMessage(new Message(uid, 2,
						msg_encrypted, 0));
			} catch (RestServiceException e) {
				System.out.println(e.getMessage());
			}
			return result;
		}

		protected void onPostExecute(Boolean result) {
			if(result) {
				update();
			} else {
				activity.getStatus().setText("Senden fehlgeschlagen");
			}
		}
	}
	
	
	private class GetMessageTask extends AsyncTask<String, Void, Boolean> {
		ArrayList<Message> messages;

		/**
		 * @return Returns true if it was successful, otherwise false
		 * @param params [0] is lastMessageID
		 */
		protected Boolean doInBackground(String... params) {

			messages = messageTask.getMessage(params[0]);

			if (messages.isEmpty()) {
				return false;
			}
			if (messages.size() - 1 == index) {
				return false;
			}
			int new_index = messages.size() - 1;

			for (int i = 0; i <= index; i++) {
				messages.remove(0);
			}

			// decrypt Messages
			for (Message msg : messages) {
				msg.setMessage(new String(aes.decrypt(msg.getMessage())));
			}
			index = new_index;
			return true;
		}

		/**
		 * Fills the TextViews with the messages
		 * @param Gets the result of doInBackground
		 */
		protected void onPostExecute(Boolean result) {
			if(result) {
				activity.updateViews(messages);
			} else {
				activity.getStatus().setText("Keine neuen Nachrichten");
			}
		}
	}
}
