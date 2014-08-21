package de.fau.cs.mad.yasme.android.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import de.fau.cs.mad.yasme.android.storage.DatabaseConstants;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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

    @JsonIgnore
    private List<Device> devices;   // Just for convenience

    @DatabaseField(columnName = DatabaseConstants.USER_LAST_MODIFIED)
    private Date lastModified;

    @DatabaseField(columnName = DatabaseConstants.USER_CREATED)
    private Date created;

    @JsonIgnore
    private String profilePicture;


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

    public User(long id) {
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

    @JsonIgnoreProperties({ "id", "user", "publicKey", "product", "lastModified" })
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

    @JsonIgnore
    public void addToContacts() {
        contactFlag = 1;
    }

    @JsonIgnore
    public void removeFromContacts() {
        contactFlag = 0;
    }

    @JsonIgnore
    public boolean isContact() {
        return contactFlag == 1;
    }

}

