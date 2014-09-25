package de.fau.cs.mad.yasme.android.entities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import de.fau.cs.mad.yasme.android.storage.DatabaseConstants;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

/**
 * Created by Martin Sturm <martin.sturm@fau.de> on 24.07.14.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@DatabaseTable(tableName = DatabaseConstants.DEVICE_TABLE)
public class Device {

    @DatabaseField(columnName = DatabaseConstants.DEVICE_ID, id = true)
    private long id;

    @DatabaseField(columnName = DatabaseConstants.DEVICE_USER, foreign = true)
    private User user;

    @DatabaseField(columnName = DatabaseConstants.DEVICE_PUBLIC_KEY)
    private String publicKey;

    @DatabaseField(columnName = DatabaseConstants.DEVICE_PRODUCT)
    private String product; // product name e.g. Google Nexus 5

    @DatabaseField(columnName = DatabaseConstants.DEVICE_LAST_MODIFIED, dataType = DataType.DATE)
    private Date lastModified;

    @DatabaseField(columnName = DatabaseConstants.DEVICE_TRUSTED)
    private boolean trusted = false;

    public Device() {
    }

    public Device(long id) {
        this.id = id;
    }


    // Getter
    public long getId() {
        return id;
    }

    @JsonIgnoreProperties({ "pw", "email", "name", "devices", "lastModified", "created" })
    public User getUser() {
        return user;
    }

    public String getProduct(){return this.product;}

    public String getPublicKey() {
        return publicKey;
    }

    public Date getLastModified() {
        return this.lastModified;
    }

    public boolean isTrusted() {
        return this.trusted;
    }


    // Setter
    public void setId(long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setProduct(String product){this.product = product;}

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setTrusted(boolean trusted) {
        this.trusted=trusted;
    }
}
