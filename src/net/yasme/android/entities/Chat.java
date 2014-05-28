package net.yasme.android.entities;

import java.util.ArrayList;


/**
 * Created by robert on 28.05.14.
 */
public class Chat {

	private String chat_id;
	private ArrayList<Message> messages;
	private long lastMessageID;

	public String getChat_id() {
		return chat_id;
	}

	public ArrayList<Message> getStoredMessages() {
		return messages;
	}

	public void setMessages(ArrayList<Message> messages) {
		this.messages = messages;
	}
	
}
