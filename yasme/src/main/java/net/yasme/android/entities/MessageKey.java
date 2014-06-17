package net.yasme.android.entities;

//creator --> de

import java.io.Serializable;

public class MessageKey implements Serializable {

    public static MessageKey last;
    private long uniqueId;  // for database storing


	private long id = -1;
	private Device creatorDevice = null;   // fuer Auswahl des oeffentlichen									// Schluessels
	private Device recipientDevice = null; // fuer Auswahl des DH-Anteils
	private Chat chat = null;
	private String messageKey = "";
	private String initVector = "";
	private byte encType = -1;
	private long encInfoId = -1;
	private String encInfo = "";
	private String sign = "";
	private long timestamp = -1;

	/** Constructors **/
	public MessageKey(long id, Device creatorDevice, Device recipientDevice,
			Chat chat, String key, byte encType, long encInfoId,
			String encInfo, String sign, long timestamp) {
		this.id = id;
		this.creatorDevice = creatorDevice;
		this.recipientDevice = recipientDevice;
		this.chat = chat;
		this.messageKey = key;
		this.encType = encType;
		this.encInfoId = encInfoId;
		this.encInfo = encInfo;
		this.sign = sign;
        this.timestamp = timestamp;
	}

	public MessageKey(long id, Device creatorDevice, Device recipientDevice,
			Chat chat, String key, byte encType, String sign) {
		this.id = id;
		this.creatorDevice = creatorDevice;
		this.recipientDevice = recipientDevice;
		this.chat = chat;
		this.messageKey = key;
		this.encType = encType;
		this.sign = sign;
	}

	public MessageKey() {

	}

	/** Getters **/
	public long getId() {
		return id;
	}

	public Device getCreator() {
		return creatorDevice;
	}

	public Device getRecipient() {
		return recipientDevice;
	}

	public Chat getChat() {
		return chat;
	}

	public String getKey() {
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

	/** Setters **/
	public void setId(long id) {
		this.id = id;
	}

	public void setCreator(Device creator) {
		this.creatorDevice = creator;
	}

	public void setRecipient(Device recipient) {
		this.recipientDevice = recipient;
	}

	public void setChat(Chat chat) {
		this.chat = chat;
	}

	public void setKey(String key) {
		this.messageKey = key;
	}

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

	public Boolean isValid() {
		if (id < 0) {
			return false;
		}

        //TODO: bitte überprüfen, ob meine Anpassung stimmt ;)
		if (recipientDevice != null) {
			return false;
		}
		if (recipientDevice != null) {
			return false;
		}
		if (chat != null) {
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
		return true;
	}
}
