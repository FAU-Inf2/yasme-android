package net.yasme.android.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.yasme.android.storage.DatabaseConstants;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by bene on 06.05.14.
 */

@DatabaseTable(tableName = DatabaseConstants.MESSAGE_TABLE)
public class Message implements Serializable {

    @DatabaseField(columnName = DatabaseConstants.MESSAGE_ID, id = true)// generatedId = true, allowGeneratedIdInsert = true)
    private long id;

    @DatabaseField(columnName = DatabaseConstants.CHAT, canBeNull = false, foreign = true,
            foreignAutoCreate=true, foreignAutoRefresh=true)
    private Chat chat;

    @DatabaseField(columnName = DatabaseConstants.SENDER, foreign = true,
            foreignAutoCreate=true, foreignAutoRefresh=true)
    private User sender;

    @DatabaseField(columnName = DatabaseConstants.DATE)
    private Date dateSent;

    @DatabaseField(columnName = DatabaseConstants.MESSAGE)
    private String message;

    @DatabaseField(columnName = DatabaseConstants.MESSAGE_MESSAGEKEY_ID)
    private long messageKeyId;

    private MessageKey messageKey;

    /**
     * Constructors *
     */
    public Message() {
        // ORMLite needs a no-arg constructor
    }

    public Message(long id, Chat chat, User sender, Date dateSent, String message, long messageKeyId) {
        this.id = id;
        this.chat = chat;
        this.sender = sender;
        this.dateSent = dateSent;
        this.message = message;
        this.messageKeyId = messageKeyId;
    }

    public Message(User sender, String message, long chatId, long messageKeyId) {
        Chat chat = new Chat();
        chat.setId(chatId);

        this.chat = chat;
        //this.dateSent = new Date(); // Set date at server. Otherwise dates may differ from device to device
        this.sender = sender;
        this.message = message;
        this.messageKeyId = messageKeyId;

        //new Message(sender, message, new Date(), chat, messageKeyId);
    }

    public Message(User sender, String message, Chat chat, long messageKeyId) {
        this.chat = chat;
        //this.dateSent = new Date();   // Set date at server. Otherwise dates may differ from device to device
        this.sender = sender;
        this.message = message;
        this.messageKeyId = messageKeyId;

        //new Message(sender, message, new Date(), chat, messageKeyId);
    }

    public Message(User sender, String message, Date dateSent, Chat chat, long messageKeyId) {
        this.sender = sender;
        this.message = message;
        this.dateSent = dateSent;
        this.chat = chat;
        //this.id = id++;
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

    @JsonIgnore
    public long getChatId() {
        return chat.getId();
    }

    public Chat getChat() {
        return chat;
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

    @JsonIgnore
    public void setChatAndId(long chatId) {
        this.chat = new Chat();
        this.chat.setId(chatId);
    }

    public void setChat(Chat chat) {
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

