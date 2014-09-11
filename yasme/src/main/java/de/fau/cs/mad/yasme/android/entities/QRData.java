package de.fau.cs.mad.yasme.android.entities;

/**
 * Created by Martin Sturm <martin.sturm@fau.de> on 11.09.2014.
 */

public class QRData {

    private long userId;
    private long deviceId;
    private String publicKey;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
