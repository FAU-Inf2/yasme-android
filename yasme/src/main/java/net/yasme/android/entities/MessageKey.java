package net.yasme.android.entities;

//creator --> de

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.yasme.android.storage.DatabaseConstants;

import java.io.Serializable;

@DatabaseTable(tableName = DatabaseConstants.MESSAGE_KEY_TABLE)
public class MessageKey implements Serializable {

    @DatabaseField(columnName = DatabaseConstants.KEY_ID)
    private long id = -1;

    @DatabaseField(columnName = DatabaseConstants.KEY_CHAT, canBeNull = false, foreign = true)
    private Chat chat = null;

    @DatabaseField(columnName = DatabaseConstants.VECTOR)
    private String initVector = "";

    @DatabaseField(columnName = DatabaseConstants.KEY)
    private String messageKey = "";

    @DatabaseField(columnName = DatabaseConstants.TIMESTAMP)
    private long timestamp = -1;

    public static MessageKey last;

	private Device creatorDevice = null;   // fuer Auswahl des oeffentlichen Schluessels
	private Device recipientDevice = null; // fuer Auswahl des DH-Anteils
	private byte encType = -1;
	private long encInfoId = -1;
	private String encInfo = "";
	private String sign = "";

	/** Constructors **/
	public MessageKey(long id, Device creatorDevice, Device recipientDevice,
			Chat chat, String key, String initVector, byte encType, long encInfoId,
			String encInfo, String sign, long timestamp) {
		this.id = id;
		this.creatorDevice = creatorDevice;
		this.recipientDevice = recipientDevice;
		this.chat = chat;
		this.messageKey = key;
        this.initVector = initVector;
		this.encType = encType;
		this.encInfoId = encInfoId;
		this.encInfo = encInfo;
		this.sign = sign;
        this.timestamp = timestamp;
	}

	public MessageKey(long id, Device creatorDevice, Device recipientDevice,
			Chat chat, String key, String initVector, byte encType, String sign) {
		this.id = id;
		this.creatorDevice = creatorDevice;
		this.recipientDevice = recipientDevice;
		this.chat = chat;
		this.messageKey = key;
        this.initVector = initVector;
		this.encType = encType;
		this.sign = sign;
	}

	public MessageKey() {
        // ORMLite needs a no-arg constructor
    }

	/** Getters **/
	public long getId() {
		return id;
	}

	public Device getCreatorDevice() {
		return creatorDevice;
	}

	public Device getRecipientDevice() {
		return recipientDevice;
	}

	public Chat getChat() {
		return chat;
	}

	public String getMessageKey() {
		return messageKey;
	}

	public byte getEncType() {
		return encType;
	}

	public long getEncInfoId() {
		return encInfoId;
	}

	public String getEncInfo() {
		return encInfo;
	}

	public String getSign() {
		return sign;
	}

    public String getInitVector(){ return initVector; }

    public long getTimestamp() { return timestamp; }

	/** Setters **/
	public void setId(long id) {
		this.id = id;
	}

	public void setCreatorDevice(Device creator) {
		this.creatorDevice = creator;
	}

	public void setRecipientDevice(Device recipient) {
		this.recipientDevice = recipient;
	}

	public void setChat(Chat chat) {
		this.chat = chat;
	}

	public void setKey(String key) {
		this.messageKey = key;
	}

    public void setInitVector(String initVector) { this.initVector = initVector; }

	public void setEncType(byte encType) {
		this.encType = encType;
	}

	public void setEncInfoId(long encInfoId) {
		this.encInfoId = encInfoId;
	}

	public void setEncInfo(String encInfo) {
		this.encInfo = encInfo;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

	public Boolean isValid() {
		if (id < 0) {
			return false;
		}

        //TODO: bitte überprüfen, ob meine Anpassung stimmt ;)
		if (recipientDevice == null || creatorDevice.getId() < 0) {
            return false;
		}
		if (creatorDevice == null || recipientDevice.getId() < 0) {
            return false;
		}
		if (chat == null  || chat.getId() < 0) {
            return false;
		}
		if (messageKey.length() <= 0) {
            return false;
		}
		if (encType < 0) {
            return false;
		}
		if (sign.length() <= 0) {
            return false;
		}
        if (initVector.length() <= 0) {
            return false;
        }
		return true;
	}
}
