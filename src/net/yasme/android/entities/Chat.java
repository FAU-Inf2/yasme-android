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
// @DatabaseTable
public class Chat {
	private ArrayList<Message> messages;
	private String lastMessageID;
	public long index;

	private String chat_id;
	private String user_name;
	private String user_id;

	private AESEncryption aes;
	private MessageTask messageTask;
	public YasmeChat activity;

	/** Constructors **/
	public Chat(int chat_id, String user_name, String user_id, String url,
			YasmeChat activity) {
		this.chat_id = Integer.toString(chat_id);
		this.user_name = user_name;
		this.user_id = user_id;

		aes = new AESEncryption("geheim");
		messageTask = new MessageTask(url);
		this.activity = activity;

		lastMessageID = "1";
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
		// TODO: letzte dem Client bekannte lastMessageID übergeben
		// Aktuell: Debugwert: 0
		new GetMessageTask().execute(lastMessageID, user_id);
	}

	private class SendMessageTask extends AsyncTask<String, Void, Boolean> {
		String msg;

		protected Boolean doInBackground(String... params) {

			msg = params[0];
			// encrypt message

			// String msg_encrypted = aes.encrypt(msg); Edit by Flo

			// creating message object
			long uid = Long.parseLong(user_id);
			boolean result = false;
			try {

				// result = messageTask.sendMessage(new Message(uid,
				// msg_encrypted, Long.parseLong(chat_id)));

				// TEMP VERSION:
				result = messageTask.sendMessage(new Message(uid, msg, Long
						.parseLong(chat_id)));

			} catch (RestServiceException e) {
				System.out.println(e.getMessage());
			}
			return result;
		}

		protected void onPostExecute(Boolean result) {
			if (result) {
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
		 * @param params
		 *            [0] is lastMessageID
		 */
		protected Boolean doInBackground(String... params) {

			try {
				messages = messageTask.getMessage(params[0], params[1]);
			} catch (RestServiceException e) {
				e.printStackTrace();
			}

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

				// msg.setMessage(new String(aes.decrypt(msg.getMessage())));

				// TEMP VERSION:
				msg.setMessage(new String(msg.getMessage()));
			}
			index = new_index;
			return true;
		}

		/**
		 * Fills the TextViews with the messages
		 * 
		 * @param Gets
		 *            the result of doInBackground
		 */
		protected void onPostExecute(Boolean result) {
			if (result) {
				activity.updateViews(messages);
			} else {
				activity.getStatus().setText("Keine neuen Nachrichten");
			}
		}
	}
}
