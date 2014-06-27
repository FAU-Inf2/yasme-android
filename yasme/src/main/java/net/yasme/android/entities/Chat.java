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

    @DatabaseField(columnName = DatabaseConstants.OWNER, foreign = true)
    private User owner;

    @JsonIgnore
    @ForeignCollectionField(columnName = DatabaseConstants.MESSAGES)
    private Collection<Message> messages;

    @JsonIgnore
    private MessageEncryption aes;

    /**
     * Constructors *
     */
    @JsonIgnore
    public Chat(long id, User user, AbstractYasmeActivity activity) {
        this.id = id;
        this.participants = new ArrayList<User>();
        this.messages = new ArrayList<Message>();
        // setup Encryption for this chat
        // TODO: DEVICE-ID statt USERID uebergeben
        long creatorDevice = user.getId();
        aes = new MessageEncryption(activity, this, creatorDevice, activity.getAccessToken());

        //new Chat(id, participants, "", "", null, new ArrayList<Message>(), aes);
    }

    public Chat(User owner, String status, String name) {
        //TODO: id generieren
        new Chat(0, new ArrayList<User>(), status, name, owner, new ArrayList<Message>(), null);
    }

    public Chat(long id, List<User> participants, String status, String name,
                User owner) {
        new Chat(id, participants, status, name, owner, new ArrayList<Message>(), null);
    }

    public Chat() {
        // ORMLite needs a no-arg constructor
    }

    public Chat(long id, Collection<User> participants, String status, String name, User owner,
                Collection<Message> messages, MessageEncryption aes) {
        this.id = id;
        this.participants = participants;
        this.status = status;
        this.name = name;
        this.owner = owner;
        this.messages = messages;
        this.aes = aes;
    }

    /**
     * Getters *
     */
    public long getId() {
        return id;
    }

    public ArrayList<User> getParticipants() {
        if (participants.isEmpty()) {
            User dummy = new User("Dummy", 12);
            participants.add(dummy);
            Log.d(this.getClass().getSimpleName(), "Dummy-User hinzugefuegt");
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
        if(owner == null) {
            owner = new User("Dummy", 12);
        }
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
    /*@JsonIgnore
    public void addParticipant(User participant, long ownUserId) {
        try {
            if (ChatTask.getInstance().addParticipantToChat(participant.getId(), id, ownUserId, accessToken))
                this.participants.add(participant);
        } catch (RestServiceException e) {
            e.printStackTrace();
        }
    }*/

    @JsonIgnore
    public void addMessage(Message msg) {
        if(messages == null) {
            messages = new ArrayList<Message>();
        }
        messages.add(msg);
    }

    @Override
    public String toString() {
        return "Chat{" +
                "id=" + id +
                ", participants=" + participants +
                ", status='" + status + '\'' +
                ", name='" + name + '\'' +
                ", owner=" + owner +
                '}';
    }
    /*@JsonIgnore
    public void removeParticipant(User participant, long ownUserId) {
        try {
            if (ChatTask.getInstance().removePartipantFromChat(participant.getId(), id, ownUserId, accessToken))
                this.participants.remove(participant);
        } catch (RestServiceException e) {
            e.printStackTrace();
        }
    }*/
}
