package net.yasme.android.entities;

public class MessageKey {

	private long id = -1;
	private long creator = -1;
	private long recipient = -1;
	private long devId = -1;
	private String key = "";
	private byte encType = -1;
	private long encInfoId = -1;
	private String encInfo = "";
	private String sign = "";

	/** Constructors **/
	public MessageKey(long id, long creator, long recipient, long devId,
			String key, byte encType, long encInfoId, String encInfo,
			String sign) {
		this.id = id;
		this.creator = creator;
		this.recipient = recipient;
		this.devId = devId;
		this.key = key;
		this.encType = encType;
		this.encInfoId = encInfoId;
		this.encInfo = encInfo;
		this.sign = sign;
	}

	public MessageKey(long id, long creator, long recipient, long devId,
			String key, byte encType, String sign) {
		this.id = id;
		this.creator = creator;
		this.recipient = recipient;
		this.devId = devId;
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
		return creator;
	}

	public long getRecipient() {
		return recipient;
	}

	public long getDevId() {
		return devId;
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
		this.creator = creator;
	}

	public void setRecipient(long recipient) {
		this.recipient = recipient;
	}

	public void setDevId(long devId) {
		this.devId = devId;
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
		if (creator < 0) {
			return false;
		}
		if (recipient < 0) {
			return false;
		}
		if (devId < 0) {
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
