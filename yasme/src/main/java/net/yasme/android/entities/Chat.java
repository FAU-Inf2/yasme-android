package net.yasme.android.entities;

import android.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import net.yasme.android.storage.DatabaseConstants;

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

    @DatabaseField(columnName = DatabaseConstants.CHAT_LAST_MODIFIED, dataType = DataType.DATE)
    private Date lastModified;

    @DatabaseField(columnName = DatabaseConstants.CHAT_CREATED)
    private Date created;

    @JsonIgnore
    private String profilePicture;

    @JsonIgnore
    @ForeignCollectionField(columnName = DatabaseConstants.MESSAGES)
    private Collection<Message> messages;

    /**
     * Constructors *
     */
    public Chat(long id, User user) {
        this.id = id;
        this.participants = new ArrayList<User>();
        this.messages = new ArrayList<Message>();
        // setup Encryption for this chat
        // TODO: DEVICE-ID statt USERID uebergeben
        //long creatorDevice = user.getId();
        //aes = new MessageEncryption(this, user);
    }

    /**
     * It is needed to set an id after calling this constructor!
     *
     * @param owner
     * @param status
     * @param name
     */
    public Chat(User owner, String status, String name) {
        new Chat(0, new ArrayList<User>(), status, name, owner, new ArrayList<Message>());
    }

    public Chat(long id, List<User> participants, String status, String name,
                User owner) {
        new Chat(id, participants, status, name, owner, new ArrayList<Message>());
    }

    public Chat() {
        // ORMLite needs a no-arg constructor
    }

    public Chat(long id, List<User> participants, String status, String name, User owner,
                Collection<Message> messages) {
        this.id = id;
        this.participants = participants;
        this.status = status;
        this.name = name;
        this.owner = owner;
        this.messages = messages;
    }

    /**
     * Getters *
     */
    public long getId() {
        return id;
    }

    @JsonIgnoreProperties({ "pw", "email", "name", "devices", "lastModified", "created" })
    public ArrayList<User> getParticipants() {
        if (participants == null) {
            participants = new ArrayList<>();
            User dummy = new User("Dummy", 12);
            participants.add(dummy);
            // Without cast IntelliJ is not happy
            Log.d(((Object)this).getClass().getSimpleName(), "participants sind null, Dummy-User hinzugefuegt");
        }
        return new ArrayList<User>(participants);
    }

    public String getStatus() {
        if(status == null || status.isEmpty()) {
            return (getNumberOfParticipants() + " Teilnehmer");
        }
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

    @JsonIgnoreProperties({ "pw", "email", "name", "devices", "lastModified", "created" })
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

    /**
     * Setters *
     */
    public void setId(long id) {
        this.id = id;
    }


    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
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

    public void setLastModified(Timestamp lastModified) {
        this.lastModified = lastModified;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

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
}
