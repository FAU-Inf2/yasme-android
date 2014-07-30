package net.yasme.android.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

/**
 * Created by Stefan on 15.05.14.
 */

public class OwnDevice {

    public enum Platform {
        ANDROID,
        IOS,
        WINDOWSPHONE;
    }

    private long id;
    private Platform platform; // android, ios or windowsmobile
    private String type;    // mobilephone, tablet or desktop
    private User user;
    private String number;  // optional
    private String publicKey;
    private String product; // product name e.g. Google Nexux 5
    private String pushId;

    private Date created;

    private Date lastModified;

    public OwnDevice(User user, Platform platform, String publicKey, String type, String number, String product, String pushId) {
        this.user = user;
        this.platform = platform;
        this.type = type;
        this.number = number;
        this.product = product;
        this.pushId = pushId;
        this.publicKey = publicKey;
    }

    public OwnDevice() {
    }

    public OwnDevice(long id) {
        this.id = id;
    }

    @JsonIgnoreProperties({ "pw", "email", "name", "devices", "lastModified", "created" })
    public User getUser() {
        return this.user;
    }

    public String getPushId(){return this.pushId;}

    public String getProduct(){return this.product;}

    public void setId(long id) {
        this.id = id;
    }

    public void setProduct(String product){this.product = product;}

    public void setUser(User user) {
        this.user = user;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public long getId() {
        return id;
    }

    public Platform getPlatform() {
        return platform;
    }

    public String getType() {
        return type;
    }

    public String getNumber() {
        return number;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public void setPushId(String pushId){this.pushId = pushId;}

    public void setType(String type) {
        this.type = type;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public Date getCreated() {
        return this.created;
    }

    public Date getLastModified() {
        return this.lastModified;
    }
}
