package net.yasme.android.entities;

//creator --> de

public class MessageKey {

	private long id = -1;
	private long creatorDevice = -1; //fuer Auswahl des oeffentlichen Schluessels
	private long recipientDevice = -1; //fuer Auswahl des DH-Anteils
	//TO-DO: aus long chat muss Chat chat werden
	private long chat = -1;
	private String key = "";
	private byte encType = -1;
	private long encInfoId = -1;
	private String encInfo = "";
	private String sign = "";

	/** Constructors **/
	public MessageKey(long id, long creatorDevice, long recipientDevice, long chat,
			String key, byte encType, long encInfoId, String encInfo,
			String sign) {
		this.id = id;
		this.creatorDevice = creatorDevice;
		this.recipientDevice = recipientDevice;
		this.chat = chat;
		this.key = key;
		this.encType = encType;
		this.encInfoId = encInfoId;
		this.encInfo = encInfo;
		this.sign = sign;
	}

	public MessageKey(long id, long creatorDevice, long recipientDevice, long chat,
			String key, byte encType, String sign) {
		this.id = id;
		this.creatorDevice = creatorDevice;
		this.recipientDevice = recipientDevice;
		this.chat = chat;
		this.key = key;
		this.encType = encType;
		this.sign = sign;
	}

	public MessageKey() {

	}

	/** Getters **/
	public long getId() {
		return id;
	}

	public long getCreator() {
		return creatorDevice;
	}

	public long getRecipient() {
		return recipientDevice;
	}

	public long getChat() {
		return chat;
	}

	public String getKey() {
		return key;
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

	public void setCreator(long creator) {
		this.creatorDevice = creator;
	}

	public void setRecipient(long recipient) {
		this.recipientDevice = recipient;
	}

	public void setChat(long chat) {
		this.chat = chat;
	}

	public void setKey(String key) {
		this.key = key;
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
		if (creatorDevice < 0) {
			return false;
		}
		if (recipientDevice < 0) {
			return false;
		}
		if (chat < 0) {
			return false;
		}
		if (key.length() <= 0) {
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
