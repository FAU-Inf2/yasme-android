package net.yasme.android.entities;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by bene on 06.05.14.
 */

public class Message implements Serializable {

	private long id;
	private long chat;
	private User sender;
	private Date dateSent;
	private String message;
	private long keyId;

	/** Constructors **/
	public Message() {
	}

	public Message(User sender, String message, long chatid, long keyid) {
		this(sender, message, new Date(), chatid, keyid);
	}

	public Message(User sender, String message, Date dateSent, long chat, long keyid) {
		this.sender = sender;
		this.message = message;
		this.dateSent = dateSent;
		this.chat = chat;
		this.keyId = keyid;
	}

	/** Getters **/
	public User getSender() {
		return sender;
	}

	public String getMessage() {
		return message;
	}

	public Date getDateSent() {
		return dateSent;
	}

	public long getKeyID() {
		return keyId;
	}

	public long getChat() {
		return chat;
	}

	public long getID() {
		return id;
	}

	/** Setters **/
	public void setChat(long chat) {
		this.chat = chat;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setID(long id) {
		this.id = id;
	}

	public void setDateSent(Date dateSent) {
		this.dateSent = dateSent;
	}

	public void setSender(User sender) {
		this.sender = sender;
	}

	public void setKeyID(long keyId) {
		this.keyId = keyId;
	}
}
