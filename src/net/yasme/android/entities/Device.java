package net.yasme.android.entities;

/**
 * Created by Stefan on 15.05.14.
 */
public class Device {

    public enum Platform {
        ANDROID, IOS, WINDOWSPHONE;
    }

    private long id;
    private Platform platform; // android, ios or windowsmobile
    private String type; // mobilephone, tablet or desktop
    private long user;
    private String number; // optional

    private String publicKey;

    public Device(long user, Platform platform, String type, String number) {
        this.user = user;
        this.platform = platform;
        this.type = type;
        this.number = number;
    }

    public Device(long id, long user, Platform platform, String type, String number, String publicKey) {
        this.id = id;
        this.user = user;
        this.platform = platform;
        this.type = type;
        this.number = number;
        this.publicKey = publicKey;
    }

    public Device() {
    }

    public long getUser() {
        return user;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(long user) {
        this.user = user;
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
}

