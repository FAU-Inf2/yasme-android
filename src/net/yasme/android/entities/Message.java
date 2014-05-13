package net.yasme.android.entities;

import java.util.Date;

/**
 * Created by bene on 06.05.14.
 */

public class Message {

	private long sender;
	private long recipient;
	private Date dateSent;
	private String message;
	private int id;

	/** Constructors **/
	public Message(long sender, long recipient, String message) {
		this(sender, recipient, message, new Date());
	}

	public Message() {

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
	
	public int getID() {
		return id;
	}

	/** Setters **/
	public void setMessage(String message) {
		this.message = message;
	}

	public void setID(int id) {
		this.id = id;
	}

	public void setSender(long sender) {
		this.sender = sender;
	}
}
