package net.yasme.android.entities;

import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import net.yasme.android.ui.AbstractYasmeActivity;
import net.yasme.android.encryption.MessageEncryption;
import net.yasme.android.storage.DatabaseConstants;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by robert on 28.05.14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@DatabaseTable(tableName = DatabaseConstants.CHAT_TABLE)
public class Chat implements Serializable {

    @DatabaseField(columnName = DatabaseConstants.CHAT_ID, id = true)
    private long id;

    private List<User> participants;

    @DatabaseField(columnName = DatabaseConstants.CHAT_STATUS)
    private String status;

    @DatabaseField(columnName = DatabaseConstants.CHAT_NAME)
    private String name;

    @DatabaseField(columnName = DatabaseConstants.OWNER, foreign = true)
    private User owner;

    private Date lastModified;
    private Date created;
    private String profilePicture;

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

    /**
     * It is needed to set an id after calling this constructor!
     *
     * @param owner
     * @param status
     * @param name
     */
    public Chat(User owner, String status, String name) {
        new Chat(0, new ArrayList<User>(), status, name, owner, new ArrayList<Message>(), null);
    }

    public Chat(long id, List<User> participants, String status, String name,
                User owner) {
        new Chat(id, participants, status, name, owner, new ArrayList<Message>(), null);
    }

    public Chat() {
        // ORMLite needs a no-arg constructor
    }

    public Chat(long id, List<User> participants, String status, String name, User owner,
                Collection<Message> messages, MessageEncryption aes) {
        this.id = id;
        this.participants = participants;
        this.status = status;
        this.name = name;
        this.owner = owner;
        this.messages = messages;

        if (aes != null) {
            this.aes = aes;
        } else {
            //TODO aes = new MessageEncryption();
        }
    }

    /**
     * Getters *
     */
    public long getId() {
        return id;
    }

    @JsonIgnore
    public ArrayList<User> getParticipants() {
        if (participants == null || participants.isEmpty()) {
            participants = new ArrayList<>();
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
                int size = getParticipants().size();
                for (int i = 0; i < size; i++) {
                    name += getParticipants().get(i).getName();
                    if(i < size - 1) {
                        name += ", ";
                    }
                }
            } catch (Exception e) {

            }
        }
        return name;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public Date getCreated() {
        return created;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public User getOwner() {
        if (owner == null) {
            owner = new User("Dummy", 12);
        }
        return owner;
    }

    @JsonIgnore
    public int getNumberOfParticipants() {

        if (participants != null)
            return participants.size();

        return 0;
    }

    @JsonIgnore
    public ArrayList<Message> getMessages() {
        if(messages == null) {
            return new ArrayList<Message>();
        }
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

    @JsonIgnore
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

    public void setLastModified(Timestamp lastModified) {
        this.lastModified = lastModified;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
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

    @JsonIgnore
    public void addMessage(Message msg) {
        if (messages == null) {
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
}
