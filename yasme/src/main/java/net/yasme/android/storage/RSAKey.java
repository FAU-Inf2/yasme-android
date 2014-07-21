package net.yasme.android.storage;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.yasme.android.entities.User;

/**
 * Created by robert on 19.07.14.
 */
@DatabaseTable(tableName = DatabaseConstants.RSA_KEY_TABLE)
public class RSAKey {

    @DatabaseField(columnName = DatabaseConstants.RSA_KEY_ID, generatedId = true)
    private long id;

    @DatabaseField(columnName = DatabaseConstants.RSA_KEY_DEVICE_ID, canBeNull = false)
    private long deviceId;

    @DatabaseField(columnName = DatabaseConstants.RSA_KEY_PUBLIC_KEY, canBeNull = false)
    private String publicKey;

    @DatabaseField(columnName = DatabaseConstants.RSA_KEY_USER, foreign = true,
            foreignAutoCreate=true, foreignAutoRefresh=true)
    private User user;

    public RSAKey(long deviceId, String publicKey, User user) {
        this.deviceId = deviceId;
        this.publicKey = publicKey;
        this.user = user;
    }

    public RSAKey () {
        //ORMLite
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getId() {

        return id;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public User getUser() {
        return user;
    }
}
