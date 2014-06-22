package net.yasme.android.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import net.yasme.android.ui.ChatActivity;
import net.yasme.android.connection.ChatTask;
import net.yasme.android.encryption.MessageEncryption;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.DatabaseConstants;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by robert on 28.05.14.
 */
@DatabaseTable(tableName = DatabaseConstants.CHAT_TABLE)
public class Chat implements Serializable {

    @DatabaseField(columnName = DatabaseConstants.CHAT_ID, id = true)
    private long id;

    @ForeignCollectionField(columnName = DatabaseConstants.PARTICIPANTS)
    private Collection<User> participants;

    @DatabaseField(columnName = DatabaseConstants.CHAT_STATUS)
    private String status;

    @DatabaseField(columnName = DatabaseConstants.CHAT_NAME)
    private String name;

    private User owner;

    @DatabaseField
    private int numberOfParticipants;

    @JsonIgnore
    @ForeignCollectionField(columnName = DatabaseConstants.MESSAGES)
    private Collection<Message> messages;

    @JsonIgnore
    private MessageEncryption aes;
    @JsonIgnore
    private String accessToken;

    /**
     * Constructors *
     */
    @JsonIgnore
    public Chat(long id, User user, ChatActivity activity) {
        this.id = id;
        accessToken = activity.getAccessToken();

        participants = new ArrayList<User>();
        messages = new ArrayList<Message>();

        // setup Encryption for this chat
        // TODO: DEVICE-ID statt USERID uebergeben
        long creatorDevice = user.getId();
        aes = new MessageEncryption(activity, this, creatorDevice, accessToken);
    }

    public Chat(User owner, String status, String name) {
        this.owner = owner;
        this.status = status;
        this.name = name;
    }

    public Chat(long id, List<User> participants, String status, String name,
                User owner, int numberOfParticipants) {
        this.id = id;
        this.participants = participants;
        this.status = status;
        this.name = name;
        this.owner = owner;
        this.numberOfParticipants = numberOfParticipants;
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
        /*User dummy = new User("Dummy", 2);
        participants.add(dummy);
        return new ArrayList<User>(participants);
        */
        return new ArrayList<>(participants);
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

    @JsonIgnore
    public ArrayList<Message> getMessages() {
        return new ArrayList<Message>(messages);
    }

    @JsonIgnore
    public MessageEncryption getEncryption() {
        return aes;
    }

    /**
     * Setters *
     */

    public void setId(long id) {
        this.id = id;
    }

    public void setParticipants(List<User> participants) {
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

    @JsonIgnore
    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    /**
     * Other Methods
     */

    @JsonIgnore
    public boolean isOwner(long userId) {
        if (owner.getId() == userId) {
            return true;
        }
        return false;
    }

    // das wird so nicht funktionieren, da kommt dann ein NetworkOnMainThread
    // Exception. Falls diese Methode benötigt wird,
    // muss ein AsyncTask draus gemacht werden (ebenso die removeParticipant Methode)
    // - robert
    @JsonIgnore
    public void addParticipant(User participant, long ownUserId) {
        try {
            if (ChatTask.getInstance().addParticipantToChat(participant.getId(), id, ownUserId, accessToken))
                this.participants.add(participant);
        } catch (RestServiceException e) {
            e.printStackTrace();
        }
    }

    @JsonIgnore
    public void addMessage(Message msg) {
        messages.add(msg);
    }

    @JsonIgnore
    public void removeParticipant(User participant, long ownUserId) {
        try {
            if (ChatTask.getInstance().removePartipantFromChat(participant.getId(), id, ownUserId, accessToken))
                this.participants.remove(participant);
        } catch (RestServiceException e) {
            e.printStackTrace();
        }
    }
}
