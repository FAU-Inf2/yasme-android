package net.yasme.android.entities;

import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import net.yasme.android.ui.AbstractYasmeActivity;
import net.yasme.android.connection.ChatTask;
import net.yasme.android.encryption.MessageEncryption;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.DatabaseConstants;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by robert on 28.05.14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@DatabaseTable(tableName = DatabaseConstants.CHAT_TABLE)
public class Chat implements Serializable {

    @DatabaseField(columnName = DatabaseConstants.CHAT_ID, id = true)
    private long id;

    //    @ForeignCollectionField(columnName = DatabaseConstants.PARTICIPANTS)
    private Collection<User> participants;

    @DatabaseField(columnName = DatabaseConstants.CHAT_STATUS)
    private String status;

    @DatabaseField(columnName = DatabaseConstants.CHAT_NAME)
    private String name;

    private User owner;

    @JsonIgnore
    @ForeignCollectionField(columnName = DatabaseConstants.MESSAGES)
    private Collection<Message> messages;

    @DatabaseField(columnName = DatabaseConstants.CONTACT)
    private int conatactFlag = 0;

    @JsonIgnore
    private MessageEncryption aes;
    @JsonIgnore
    private String accessToken;

    /**
     * Constructors *
     */
    @JsonIgnore
    public Chat(long id, User user, AbstractYasmeActivity activity) {
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
        this.participants = new ArrayList<User>();
    }

    public Chat(long id, List<User> participants, String status, String name,
                User owner) {
        this.id = id;
        this.participants = participants;
        this.status = status;
        this.name = name;
        this.owner = owner;
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
        if (participants == null) {
            participants = new ArrayList<User>();
            User dummy = new User("Dummy", 12);
            participants.add(dummy);
            //Log.d(this.getClass().getSimpleName(), "Dummy-User hinzugefuegt");
        }
        return new ArrayList<User>(participants);
    }

    public String getStatus() {
        return status;
    }

    public String getName() {
        if (name == null) {
            name = "";
        }
        if (name.length() <= 0) {
            try {
                for (int i = 0; i < getParticipants().size(); i++) {
                    if (name.length() <= 0) {
                        name += ", ";
                    }
                    name += getParticipants().get(i).getName();
                }
            } catch (Exception e) {

            }
        }
        return name;
    }

    public User getOwner() {
        return owner;
    }

    public int getNumberOfParticipants() {

        if (participants != null)
            return participants.size();

        return 0;
    }

    @JsonIgnore
    public ArrayList<Message> getMessages() {
        return new ArrayList<Message>(messages);
    }

    @JsonIgnore
    public MessageEncryption getEncryption() {
        if (aes == null)
            System.out.println("[DEBUG] Chat wurde erstellt ohne gueltiges Encryption-Object --> Class: Chat.getEncryption())");
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

    public void setEncryption(MessageEncryption aes) {
        this.aes = aes;
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

    public void addToContacts() {
        conatactFlag = 1;
    }

    public void removeFromContacts() {
        conatactFlag = 0;
    }
}
