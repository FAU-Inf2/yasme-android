package net.yasme.android.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by bene on 06.05.14.
 */

@DatabaseTable(tableName = "messages")
public class Message implements Serializable {

    @DatabaseField(id = true)
    private long id;

    @DatabaseField(canBeNull = true, foreign = true)
    private long chat;


    private User sender;
    private Date dateSent;
    private String message;
    private long messageKeyId;
    private MessageKey messageKey;

    /**
     * Constructors *
     */
    public Message() {
    }

    public Message(User sender, String message, long chat, long messageKeyId) {
        this(sender, message, new Date(), chat, messageKeyId);
    }

    public Message(User sender, String message, Date dateSent, long chat, long messageKeyId) {
        this.sender = sender;
        this.message = message;
        this.dateSent = dateSent;
        this.chat = chat;
        this.messageKeyId = messageKeyId;
    }

    /**
     * Getters *
     */
    public User getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public Date getDateSent() {
        return dateSent;
    }

    public MessageKey getMessageKey() {
        return messageKey;
    }

    public long getChat() {
        return chat;
    }

    public long getId() {
        return id;
    }

    public long getMessageKeyId() {
        return messageKeyId;
    }

    /**
     * Setters *
     */
    public void setChat(long chat) {
        this.chat = chat;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public void setMessageKey(MessageKey messageKey) {
        this.messageKey = messageKey;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setMessageKeyId(long messageKeyId) {
        this.messageKeyId = messageKeyId;
    }
}

