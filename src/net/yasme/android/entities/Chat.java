package net.yasme.android.entities;

import java.util.ArrayList;

import net.yasme.android.YasmeChat;
import net.yasme.android.connection.MessageTask;
import net.yasme.android.encryption.MessageEncryption;
import net.yasme.android.exception.RestServiceException;
import android.os.AsyncTask;

/**
 * Created by robert on 28.05.14.
 */
// @DatabaseTable
public class Chat {
	public final static String STORAGE_PREFS = "net.yasme.andriod.STORAGE_PREFS";
	public final static String USER_ID = "net.yasme.andriod.USER_ID";
	
	private ArrayList<Message> messages;
	private long lastMessageID;
	public long index;
	
	private String chat_id;
	private String user_name;
	private String user_id;
	String url;
	
	private MessageEncryption aes;
	private MessageTask messageTask;
	public YasmeChat activity;

	/** Constructors **/
	public Chat(int chat_id, String user_id, String url, YasmeChat activity) {
		this.chat_id = Integer.toString(chat_id);		
		this.user_id = user_id;
		this.activity = activity;
	
		//setup Encryption for this chat
		long creator = Long.parseLong(user_id);
		long recipient = 2L;
		long devid = 3L;
		aes = new MessageEncryption(activity, new Id(chat_id), creator, recipient, devid);

		messageTask = new MessageTask(url);
		
		lastMessageID = 0;
	}

	/** Getters **/
	public String getChat_id() {
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
		this.lastMessageID = newlastMessageID;
	}
	
	public void incLastMessageID() {
		lastMessageID++;
	}

	/** Other methods **/
	public void send(String msg) {
		new SendMessageTask().execute(msg, user_name);
		incLastMessageID();
		//update();
	}

	public void update() {
		new GetMessageTask().execute(Long.toString(lastMessageID), user_id);
	}

	private class SendMessageTask extends AsyncTask<String, Void, Boolean> {
		String msg;

		protected Boolean doInBackground(String... params) {

			msg = params[0];
			
			long uid = Long.parseLong(user_id);
			boolean result = false;
			
			//Message verschlüsseln
			String msg_encrypted = aes.encrypt(msg);
			
			try {
				result = messageTask
						.sendMessage(new Message(new User(user_name, uid),
								msg_encrypted, Long.parseLong(chat_id), aes.getKeyId()));
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
		
			if (messages.size() - 1 == index) {
				return false;
			}
			int new_index = messages.size() - 1;
			
			// decrypt Messages
			for (Message msg : messages) {
				msg.setMessage(new String(aes.decrypt(msg.getMessage(), msg.getKeyID())));
				}
			
			index = new_index;
			setLastMessageID(Long.toString(messages.get(messages.size() - 1)
					.getID()));
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
				setLastMessageID(messages.size() - 1 + lastMessageID);
				//activity.getStatus().setText(Integer.toString(messages.size()));
				//activity.getStatus().setText("LastMessageID: " + lastMessageID);
			} else {
				activity.getStatus().setText("Keine neuen Nachrichten");
			}
		}
	}
}
