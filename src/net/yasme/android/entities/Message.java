package net.yasme.android.entities;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by bene on 06.05.14.
 */

public class Message implements Serializable {

	private static final long serialVersionUID = -3614868307563665399L;
	private long id;
	private long sender;
	private long recipient;
	private Date dateSent;
	private String message;
	private long keyId;

	private long chatID;

	/** Constructors **/
	public Message() {
	}

	public Message(long sender, long recipient, String message) {
		this(sender, recipient, message, new Date());
	}

	public Message(long sender, long recipient, String message, Date dateSent) {
		this.sender = sender;
		this.recipient = recipient;
		this.message = message;
		this.dateSent = dateSent;
	}

	/** Getters **/
	public long getSender() {
		return sender;
	}

	public long getRecipient() {
		return recipient;
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

	public long getChatID() {
		return chatID;
	}

	/** Setters **/

	public void setChatID(long chatID) {
		this.chatID = chatID;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setID(int id) {
		this.id = id;
	}

	public void setDateSent(Date dateSent) {
		this.dateSent = dateSent;
	}

	public void setSender(long sender) {
		this.sender = sender;
	}

	public void setRecipient(long recipient) {
		this.recipient = recipient;
	}

	public void setKeyID(long keyId) {
		this.keyId = keyId;
	}
}
