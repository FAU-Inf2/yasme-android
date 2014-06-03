package net.yasme.android.entities;

/**
 * Created by Stefan on 15.05.14.
 */
public class Device {

	private String id;
	private String platform; // android, ios or windowsmobile
	private String type; // mobilephone, tablet or desktop
	private String userID;

	private String number; // optional

	public Device(String userID, String platform, String type, String number) {
		this.userID = userID;
		this.platform = platform;
		this.type = type;
		this.number = number;
	}
	
	public Device(String userID, String platform, String type) {
		this.userID = userID;
		this.platform = platform;
		this.type = type;
	}

	public Device() {
	}

	public String getUserID() {
		return userID;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getId() {
		return id;
	}

	public String getPlatform() {
		return platform;
	}

	public String getType() {
		return type;
	}

	public String getNumber() {
		return number;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setNumber(String number) {
		this.number = number;
	}
}
