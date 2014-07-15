package net.yasme.android.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.yasme.android.storage.DatabaseConstants;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by Stefan on 10.05.14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@DatabaseTable(tableName = DatabaseConstants.USER_TABLE)
public class User implements Serializable {

    @DatabaseField(columnName = DatabaseConstants.USER_ID, id = true)
    private long id;

    @DatabaseField(columnName = DatabaseConstants.USER_NAME)
    private String name;

    //@DatabaseField(columnName = DatabaseConstants.USER_EMAIL)
    private String email;

    private String pw;

    private List<Device> devices;   // Just for convenience

    @JsonIgnore
    private Date lastModified;

    @JsonIgnore
    private Date created;

    @JsonIgnore
    private String profilePicture;

    //@JsonIgnore
    //@DatabaseField(columnName = DatabaseConstants.CHAT_ID, foreign = true)
    //private Chat chat; // only for client Database

    @JsonIgnore
    @DatabaseField(columnName = DatabaseConstants.CONTACT)
    private int contactFlag = 0;


    public User(String pw, String name, String email) {
        this.pw = pw;
        this.name = name;
        this.email = email;
    }

    public User(String email, String pw) {
        this.email = email;
        this.pw = pw;
    }

    //TODO: changeOrder
    public User(String name, long id) {
        this.name = name;
        this.id = id;
    }

    public User(String name, String email, long id) {
        this.name = name;
        this.email = email;
        this.id = id;
    }

    public User() {
        // ORMLite needs a no-arg constructor
    }

    /*
     * Getters
     */
    //@JsonIgnore
    //public ArrayList<Chat> getChats() {
    //    return new ArrayList<Chat>(chats);
    //}

    public List<Device> getDevices() {
        return devices;
    }


    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPw() {
        return pw;
    }

    public long getId() {
        return id;
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

    /*
     * Setters
     */
    //@JsonIgnore
    //public void addChat(Chat chat) {
    //    this.chats.addIfNotExists(chat);
    //}

    //TODO: Aufruf von setChat in addChat umwandeln
    //public void setChat(Chat chat) {
    //    this.chats.addIfNotExists(chat);
    //}

    //@JsonIgnore
    //public void setChat(ArrayList<Chat> chats) {
    //    this.chats = chats;
    //}


    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    //@JsonIgnore
    //public void removeChat(Chat chat) {
    //    chats.remove(chat);
    //}

    @JsonIgnore
    public void addToContacts() {
        contactFlag = 1;
    }

    @JsonIgnore
    public void removeFromContacts() {
        contactFlag = 0;
    }

    @JsonIgnore
    public int isContact() {
        return contactFlag;
    }


    //public Bitmap loadProfilePicture() {

    //}

    //public void storeAndSetProfilePicture(Bitmap picture) {
    //    // Set the modified date to now since the stored picture's name will contain that date
    //    setLastModified(new Date());
    //    String storedPath;
    //    try {
    //        PictureManager.INSTANCE.storePicture(this, picture);
    //    } catch (IOException e) {
    //
    //    }
    //}
}

