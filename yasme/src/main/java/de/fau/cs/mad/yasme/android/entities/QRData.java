package de.fau.cs.mad.yasme.android.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by Martin Sturm <martin.sturm@fau.de> on 11.09.2014.
 */

@JsonPropertyOrder({ "deviceId", "publicKey" })
public class QRData {

    private long deviceId;
    private String publicKey;

    @JsonIgnore
    private Device serverDevice;

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

    @JsonIgnore
    public Device getServerDevice() {
        return serverDevice;
    }

    @JsonIgnore
    public void setServerDevice(Device serverDevice) {
        this.serverDevice = serverDevice;
    }
}
