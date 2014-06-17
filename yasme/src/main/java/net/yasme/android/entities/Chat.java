package net.yasme.android.entities;

import android.os.AsyncTask;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import net.yasme.android.YasmeChat;
import net.yasme.android.connection.ChatTask;
import net.yasme.android.connection.MessageTask;
import net.yasme.android.encryption.MessageEncryption;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.DatabaseConstants;

import java.util.ArrayList;

/**
 * Created by robert on 28.05.14.
 */
public class Chat {

    @DatabaseField(columnName = DatabaseConstants.CHAT_ID, id = true)
    private long id;

    @DatabaseField
    private ArrayList<User> participants = new ArrayList<User>();

    @DatabaseField(columnName = DatabaseConstants.CHAT_STATUS)
    private String status;

    @DatabaseField(columnName = DatabaseConstants.CHAT_NAME)
    private String name;

    private User owner;

    @DatabaseField
    private int numberOfParticipants;

    @ForeignCollectionField(columnName = DatabaseConstants.MESSAGES)
    private ForeignCollection<Message> messages;
    //private ArrayList<Message> messages;

    @DatabaseField(columnName = DatabaseConstants.LAST_MESSAGE_ID)
    private long lastMessageID;

    private User self; //<----??? only one User Object instead of userName, userId, etc.

    private MessageEncryption aes;
    private MessageTask messageTask;
    public YasmeChat activity;
    private String accessToken;

    /**
     * Constructors *
     */
    public Chat(long id, User user, YasmeChat activity) {
        this.id = id;
        this.self = user;
        this.activity = activity;
        accessToken = activity.accessToken;

        messageTask = MessageTask.getInstance(activity);

        lastMessageID = 0L;

        // setup Encryption for this chat
        // TODO: DEVICE-ID statt USERID uebergeben
        long creatorDevice = user.getId();
        aes = new MessageEncryption(activity, this, creatorDevice, accessToken);
    }

    public Chat() {
        // ORMLite needs a no-arg constructor
    }

    /**
     * Getters *
     */
    public long getId() {
        return id;
    }

    public ArrayList<User> getParticipants() {
        User dummy = new User("Dummy", 444);
        participants.add(dummy);
        return participants;
    }

    public String getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public User getOwner() {
        return owner;
    }

    public int getNumberOfParticipants() {
        return numberOfParticipants;
    }

    public ArrayList<Message> getMessages() {
        return new ArrayList<Message>(messages);
    }

    public long getLastMessageID() {
        return lastMessageID;
    }

    public User getSelf() {
        return self;
    }

    public boolean isOwner(long userId){
        if(owner.getId() == userId){
            return true;
        }
        return false;
    }

    /**
     * Setters *
     */

    public void addMessage(Message msg) {
        messages.add(msg);
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setParticipants(ArrayList<User> participants) {
        this.participants = participants;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public void setNumberOfParticipants(int numberOfParticipants) {
        this.numberOfParticipants = numberOfParticipants;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = (ForeignCollection<Message>) messages;
    }

    public void setLastMessageID(long lastMessageID) {
        this.lastMessageID = lastMessageID;
    }

    public void addParticipant(User participant) {

        try {
            if(ChatTask.getInstance().addParticipantToChat(participant.getId(), self.getId(), id, accessToken))
                this.participants.add(participant);
        } catch (RestServiceException e) {
            e.printStackTrace();
        }
    }

    public void removeParticipant(User participant) {

        try {

            if(ChatTask.getInstance().removePartipantFromChat(participant.getId(), id ,self.getId(), accessToken))
                this.participants.remove(participant);

        } catch (RestServiceException e) {
            e.printStackTrace();
        }
    }

    /**
     * Other methods *
     */
    public void send(String msg) {
        new SendMessageTask().execute(msg, self.getName(), self.getEmail(), Long.toString(self.getId()));
    }

    public void update() {
        new GetMessageTask().execute(lastMessageID, self.getId());
    }

    private class SendMessageTask extends AsyncTask<String, Void, Boolean> {
        String msg;

        protected Boolean doInBackground(String... params) {

            msg = params[0];

            String uName = params[1];
            String uMail = params[2];
            long uId = Long.parseLong(params[3]);
            boolean result = false;

            // encrypt Message
            String msg_encrypted = aes.encrypt(msg);

            // create Message
            Message createdMessage = new Message(new User(uName, uMail,  uId),
                    msg_encrypted, id, aes.getKeyId());
            try {
                result = messageTask.sendMessage(createdMessage, accessToken);
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
    // geloescht werden

    private class GetMessageTask extends AsyncTask<Long, Void, Boolean> {
        ArrayList<Message> messages;

        /**
         * @param params [0] is lastMessageID
         *        params [1] is user_id
         * @return Returns true if it was successful, otherwise false
         */
        protected Boolean doInBackground(Long... params) {

            try {
                messages = messageTask.getMessage(params[0], params[1], accessToken);
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
                msg.setMessage(new String(aes.decrypt(msg.getMessage(), msg.getMessageKeyId())));
            }

            return true;
        }

        /**
         * Fills the TextViews with the messages
         * - maybe this should be done also in doInBackground
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
}
