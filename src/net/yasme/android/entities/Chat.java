package net.yasme.android.entities;

import android.os.AsyncTask;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.yasme.android.YasmeChat;
import net.yasme.android.connection.ChatTask;
import net.yasme.android.connection.MessageTask;
import net.yasme.android.encryption.MessageEncryption;
import net.yasme.android.exception.RestServiceException;

import java.util.ArrayList;

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
	private long lastMessageID;
	@DatabaseField(generatedId = true, id = true)
	private long chat_id;

	private String user_name;
	private long user_id;
	String url;

	private MessageEncryption aes;
	private MessageTask messageTask;
	public YasmeChat activity;

    private String status;
    private String name;
    private int parCounter;
    private ArrayList<User> participants;

	/**
	 * Constructors *
	 */
	public Chat(long chat_id, long user_id, String url, YasmeChat activity) {
		this.chat_id = chat_id;
		this.user_id = user_id;
		this.activity = activity;

		// setup Encryption for this chat
		// TODO: DEVICE-ID statt USERID uebergeben
		long creatorDevice = user_id;
		aes = new MessageEncryption(activity, chat_id, creatorDevice);

		messageTask = new MessageTask(url);

		lastMessageID = 0L;
	}

	public Chat() {
		// ORMLite needs a no-arg constructor
	}

	/**
	 * Getters *
	 */
	public long getChat_id() {
		return chat_id;
	}

	public ArrayList<Message> getStoredMessages() {
		return messages;
	}

    public ArrayList<User> getParticipants() { return participants; }

    public String getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

	/**
	 * Setters *
	 */
	public void setMessages(ArrayList<Message> messages) {
		this.messages = messages;
	}

	public void setLastMessageID(long newlastMessageID) {
		lastMessageID = newlastMessageID;
	}

    public void setStatus(String newStatus) {
        status = newStatus;
    }

    public void setName(String newName) {
        name = newName;
    }

    public void addParticipant(User participant) {
        try {
            new ChatTask(url).addParticipantToChat(participant.getId(), chat_id);
        } catch (RestServiceException e) {
            e.printStackTrace();
        }
    }

    public void removeParticipant(User participant) {
        try {
            new ChatTask(url).removePartipantFromChat(participant.getId(), chat_id);
        } catch (RestServiceException e) {
            e.printStackTrace();
        }
    }

	/**
	 * Other methods *
	 */
	public void send(String msg) {
		new SendMessageTask().execute(msg, user_name);
	}

	public void update() {
		new GetMessageTask().execute(lastMessageID, user_id);
	}

	private class SendMessageTask extends AsyncTask<String, Void, Boolean> {
		String msg;

		protected Boolean doInBackground(String... params) {

			msg = params[0];

			long uid = user_id;
			boolean result = false;

			// encrypt Message
			String msg_encrypted = aes.encrypt(msg);

			// create Message
			Message createdMessage = new Message(new User(user_name, uid),
					msg_encrypted, chat_id, aes.getKeyId());
			try {

                //TODO: AccessToken auslesen und als String sendMessage übergeben
                //Current: Default Value 0
                result = messageTask.sendMessage(createdMessage,"0");
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

	// TODO: erweitere Methode, sodass auch Keys abgeholt werden und danach
	// gel�scht werden

	private class GetMessageTask extends AsyncTask<Long, Void, Boolean> {
		ArrayList<Message> messages;

		/**
		 * @param params
		 *            [0] is lastMessageID
		 * @param params
		 *            [1] is user_id
		 * @return Returns true if it was successful, otherwise false
		 */
		protected Boolean doInBackground(Long... params) {

			try {
                //TODO: AccessToken auslesen und als String getMessage() übergeben
                //Current: Default Value 0
                messages = messageTask.getMessage(params[0], params[1],"0");
			} catch (RestServiceException e) {
				e.printStackTrace();
			}

            if (messages == null) {
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
         * //@param Gets the result of doInBackground
         * //@param Gets the result of doInBackground
         */
        protected void onPostExecute(Boolean result) {
            if (result) {
                activity.updateViews(messages);
				lastMessageID = messages.size() + lastMessageID;	
            } else {
                activity.getStatus().setText("Keine neuen Nachrichten");
            }
        }
    }
    /*
    //Async-Task for getting Keys from server
	//TODO: koennen mehrere Keys sein
	//TODO: 
	//TODO: Client muss letzte ID seiner Key-Id mitschicken
	private class GetKeyTask extends AsyncTask<String, Void, Boolean> {
		MessageKey messagekey;

		protected Boolean doInBackground(String... params) {
			
			
			try{
				//get MessageKey-Object
				keytask = new KeyTask(url);
				//messagekey = keytask.getKey(Long.toString(creator), Long.toString(chatid.getId()), Long.toString(devid));
				
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			return true;
		}

		protected void onPostExecute(Boolean result) {
			//safe new Key in Chat-Tabelle
			
		}
	}
	*/
}
