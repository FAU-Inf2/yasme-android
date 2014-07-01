package net.yasme.android.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.yasme.android.storage.DatabaseConstants;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by bene on 06.05.14.
 */

@DatabaseTable(tableName = DatabaseConstants.MESSAGE_TABLE)
public class Message implements Serializable {

    @DatabaseField(columnName = DatabaseConstants.MESSAGE_ID,
            allowGeneratedIdInsert = true, generatedId = true)
    private long id;

    @DatabaseField(columnName = DatabaseConstants.CHAT, canBeNull = false, foreign = true,
            foreignAutoCreate=true, foreignAutoRefresh=true)
    private Chat chat;

    @DatabaseField(columnName = DatabaseConstants.SENDER, foreign = true)
    private User sender;

    @DatabaseField(columnName = DatabaseConstants.DATE)
    private Date dateSent;

    @DatabaseField(columnName = DatabaseConstants.MESSAGE)
    private String message;

    private long messageKeyId;
    private MessageKey messageKey;

    /**
     * Constructors *
     */
    public Message() {
        // ORMLite needs a no-arg constructor
    }

    public Message(User sender, String message, long chat, long messageKeyId) {
        this(sender, message, new Date(), chat, messageKeyId);
    }

    public Message(User sender, String message, Date dateSent, long chatId, long messageKeyId) {
        this.sender = sender;
        this.message = message;
        this.dateSent = dateSent;
        this.chat = new Chat();
        chat.setId(chatId);
        this.id = id++;
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
        return chat.getId();
    }

    public long getId() {
        return id;
    }

    public long getMessageKeyId() {
        return messageKeyId;
    }

    /**
     * Setters
     */
    public void setChat(long chatId) {
        this.chat = new Chat();
        this.chat.setId(chatId);
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

