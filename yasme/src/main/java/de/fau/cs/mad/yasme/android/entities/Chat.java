package de.fau.cs.mad.yasme.android.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.storage.DatabaseConstants;

/**
 * Created by Robert Meissner <robert.meissner@studium.fau.de> on 28.05.14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@DatabaseTable(tableName = DatabaseConstants.CHAT_TABLE)
public class Chat implements Serializable {

    @DatabaseField(columnName = DatabaseConstants.CHAT_ID, id = true)
    private long id;

    private List<User> participants;

    @DatabaseField(columnName = DatabaseConstants.NAME_CHANGED)
    private boolean nameChanged = false;

    @DatabaseField(columnName = DatabaseConstants.STATUS_CHANGED)
    private boolean statusChanged = false;

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

    @JsonIgnore
    @DatabaseField(columnName = DatabaseConstants.LAST_MESSAGE, foreign = true)
    private Message lastMessage;

    /**
     * Constructors *
     */
    public Chat(long id) {
        this.id = id;
        this.participants = new ArrayList<User>();
        this.messages = new ArrayList<Message>();
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

    public Chat(Chat origin) throws IllegalAccessException {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.set(this, field.get(origin));
        }
    }

    /**
     * Getters *
     */
    public long getId() {
        return id;
    }

    /**
     * Setters *
     */
    public void setId(long id) {
        this.id = id;
    }

    public int size() {
        if (null == participants) {
            Log.e(((Object) this).getClass().getSimpleName(), "Participants are null");
            return -1;
        }
        return participants.size();
    }

    @JsonIgnoreProperties({"pw", "email", "name", "devices", "lastModified", "created"})
    public ArrayList<User> getParticipants() {
        if (participants == null) {
            participants = new ArrayList<>();
            User mDummy = new User("Dummy", 12);
            participants.add(mDummy);
            // Without cast IntelliJ is not happy
            Log.e(((Object) this).getClass().getSimpleName(), "Participants are null");
        }
        return new ArrayList<User>(participants);
    }

    public void setParticipants(List<User> participants) {
        this.participants = participants;
    }

    public void setNameChanged(boolean nameChanged) {
        this.nameChanged = nameChanged;
    }

    public void setStatusChanged(boolean statusChanged) {
        this.statusChanged = statusChanged;
    }

    public boolean addParticipant(User newUser) {
        if (participants == null) {
            Log.e(((Object) this).getClass().getSimpleName(), "Participants are null");
            return false;
        }
        participants.add(newUser);
        return true;
    }

    public boolean removeParticipant(User delUser) {
        if (participants == null) {
            Log.e(((Object) this).getClass().getSimpleName(), "Participants are null, sry.");
            return false;
        }
        boolean ret = false;
        for (User u : participants) {
            if (u.getId() != delUser.getId()) {
                continue;
            }
            ret = participants.remove(u);
            break;
        }
        return true;
    }

    public String getStatus() {
        if (!this.statusChanged || this.status == null || this.status.isEmpty()) {
            return (getNumberOfParticipants() + " YASMEs");
        }
        return this.status;
    }

    public void setStatus(String status, boolean isNotDefault) {
        this.statusChanged = isNotDefault;
        this.status = status;
    }

    public boolean getStatusChanged() {
        return this.statusChanged;
    }

    public boolean getNameChanged() {
        return this.nameChanged;
    }

    @JsonProperty("name")
    public String getName() {
        if (this.nameChanged && this.name != null && this.name.length() > 0) {
            return name;
        }
        // Create a new name
        String returnName = "";
        try {
            int size = getParticipants().size();
            for (int i = 0; i < size; i++) {
                returnName += participants.get(i).getName();
                if (i < size - 1) {
                    returnName += ", ";
                }
            }
        } catch (Exception e) {
            Log.d(this.getClass().getSimpleName(), e.getMessage());
        }
        return returnName;
    }

    public void setName(String name, boolean isNotDefault) {
        this.nameChanged = isNotDefault;
        this.name = name;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Timestamp lastModified) {
        this.lastModified = lastModified;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    @JsonIgnoreProperties({"pw", "email", "name", "devices", "lastModified", "created"})
    public User getOwner() {
        if (owner == null) {
            owner = new User("Dummy", 12);
        }
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    @JsonIgnore
    public int getNumberOfParticipants() {
        if (participants != null) {
            return participants.size();
        }
        return 0;
    }

    @JsonIgnore
    public ArrayList<Message> getMessages() {
        if (messages == null) {
            return new ArrayList<Message>();
        }
        return new ArrayList<Message>(messages);
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
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

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Chat clone() {
        try {
            return new Chat(this);
        } catch (IllegalAccessException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }
}
