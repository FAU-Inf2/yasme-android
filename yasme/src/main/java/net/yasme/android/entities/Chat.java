package net.yasme.android.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import net.yasme.android.YasmeChat;
import net.yasme.android.connection.ChatTask;
import net.yasme.android.encryption.MessageEncryption;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.DatabaseConstants;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by robert on 28.05.14.
 */
@DatabaseTable(tableName = "chat")
public class Chat {

    @DatabaseField(columnName = DatabaseConstants.CHAT_ID, generatedId = true)
    private long chatId;

    @ForeignCollectionField(columnName = DatabaseConstants.PARTICIPANTS, eager = true)
    private Collection<User> participants;

    @DatabaseField(columnName = DatabaseConstants.CHAT_STATUS)
    private String status;

    @DatabaseField(columnName = DatabaseConstants.CHAT_NAME)
    private String name;

    @JsonIgnore
    @DatabaseField
    private int numberOfParticipants;

    @JsonIgnore
    @ForeignCollectionField(columnName = DatabaseConstants.MESSAGES)
    private Collection<Message> messages;

    @JsonIgnore
    private User owner;

    @JsonIgnore
    private MessageEncryption aes;
    @JsonIgnore
    private String accessToken;

    /**
     * Constructors *
     */
    @JsonIgnore
    public Chat(long id, User user, YasmeChat activity) {
        this.chatId = id;
        accessToken = activity.accessToken;

        participants = new ArrayList<User>();
        messages = new ArrayList<Message>();

        // setup Encryption for this chat
        // TODO: DEVICE-ID statt USERID uebergeben
        long creatorDevice = user.getId();
        System.out.println("[???] User/Device: "+creatorDevice);
        aes = new MessageEncryption(activity, this, creatorDevice, accessToken);
    }


    public Chat(User owner, String status, String name) {
        this.owner = owner;
        this.status = status;
        this.name = name;
    }

    @JsonIgnore
    public Chat() {
        // ORMLite needs a no-arg constructor
    }
    /**
     * Getters *
     */
    public long getId() {
        return chatId;
    }

    public ArrayList<User> getParticipants() {
        User dummy = new User("Dummy", 2);
        participants.add(dummy);
        return new ArrayList<User>(participants);
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

    public MessageEncryption getEncryption() {
        return aes;
    }

    /**
     * Setters *
     */

    public void setId(long id) {
        this.chatId = id;
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
        this.messages = messages;
    }

    /**
     * Other Methods
     */

    public boolean isOwner(long userId){
        if(owner.getId() == userId){
            return true;
        }
        return false;
    }

    // das wird so nicht funktionieren, da kommt dann ein NetworkOnMainThread
    // Exception. Falls diese Methode benötigt wird,
    // muss ein AsyncTask draus gemacht werden (ebenso die removeParticipant Methode)
    // - robert
    public void addParticipant(User participant, long ownUserId) {
        try {
            if(ChatTask.getInstance().addParticipantToChat(participant.getId(), chatId, ownUserId, accessToken))
                this.participants.add(participant);
        } catch (RestServiceException e) {
            e.printStackTrace();
        }
    }

    public void addMessage(Message msg) {
        messages.add(msg);
    }

    public void removeParticipant(User participant, long ownUserId) {
        try {
            if(ChatTask.getInstance().removePartipantFromChat(participant.getId(), chatId, ownUserId, accessToken))
                this.participants.remove(participant);
        } catch (RestServiceException e) {
            e.printStackTrace();
        }
    }
}
