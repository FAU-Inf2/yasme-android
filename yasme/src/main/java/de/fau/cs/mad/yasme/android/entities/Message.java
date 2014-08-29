package de.fau.cs.mad.yasme.android.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import de.fau.cs.mad.yasme.android.encryption.MessageEncryption;
import de.fau.cs.mad.yasme.android.storage.DatabaseConstants;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Benedikt Lorch <benedikt.lorch@studium.fau.de> on 06.05.14.
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

    @DatabaseField(columnName = DatabaseConstants.DATE, dataType = DataType.DATE_LONG)
    private Date dateSent;

    @DatabaseField(columnName = DatabaseConstants.MESSAGE)
    private String message;

    @DatabaseField(columnName = DatabaseConstants.MESSAGE_MESSAGEKEY_ID)
    private long messageKeyId;

    @DatabaseField(columnName = DatabaseConstants.AUTHENTICATED)
    private byte authenticated = 0;

    @DatabaseField(columnName = DatabaseConstants.ERROR_ID)
    private int errorId = MessageEncryption.ErrorType.OK;

    @DatabaseField(columnName = DatabaseConstants.READ)
    private boolean read = false;

    @DatabaseField(columnName = DatabaseConstants.SENT)
    private boolean sent = false;

    @DatabaseField(columnName = DatabaseConstants.RECEIVED)
    private boolean received = false;

    private MessageKey messageKey;

    /**
     * Constructors *
     */
    public Message() {
        // ORMLite needs a no-arg constructor
    }

    public Message(long id, Date dateSent, User sender, String message, Chat chat, long messageKeyId) {
        this.id = id;
        this.dateSent = dateSent;
        this.sender = sender;
        this.message = message;
        this.chat = chat;
        this.messageKeyId = messageKeyId;
    }

    public Message(User sender, String message, Chat chat, long messageKeyId) {
        //Chat chat = new Chat();
        //chat.setId(chatId);

        this.chat = chat;
        this.sender = sender;
        this.message = message;
        this.messageKeyId = messageKeyId;
    }
    /**
     * Getters *
     */
    @JsonIgnoreProperties({ "pw", "email", "name", "devices", "lastModified", "created" })
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

    @JsonIgnoreProperties({ "participants", "status", "name", "owner", "lastModified", "created", "profilePicture", "messages" })
    public Chat getChat() {
        return chat;
    }

    public long getId() {
        return id;
    }

    public long getMessageKeyId() {
        return messageKeyId;
    }

    @JsonIgnore
    public boolean getAuthenticity(){
        if (authenticated == 1) return true;
        else return false;
    }

    @JsonIgnore
    public int getErrorId() {
        return errorId;
    }

    @JsonIgnore
    public boolean wasRead() {
        return read;
    }

    @JsonIgnore
    public boolean wasSent() {
        return sent;
    }

    @JsonIgnore
    public boolean wasReceived() {
        return received;
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

    @JsonIgnore
    public boolean setAuthenticity(boolean bool){
        if (bool){
            authenticated = 1;
            return true;
        }
        else {
            authenticated = 0;
            return false;
        }
    }

    @JsonIgnore
    public void setErrorId(int id) {
        this.errorId = id;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }
}

