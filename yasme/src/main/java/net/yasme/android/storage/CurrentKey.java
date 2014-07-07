package net.yasme.android.storage;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.yasme.android.entities.Chat;
import net.yasme.android.entities.MessageKey;

import java.io.Serializable;

/**
 * Created by robert on 06.07.14.
 */
@DatabaseTable(tableName = DatabaseConstants.CURRENT_KEY_TABLE)
public class CurrentKey implements Serializable {

    @DatabaseField(columnName = DatabaseConstants.CURRENT_KEY_ID, generatedId = true)
    long id;

    @DatabaseField(columnName = DatabaseConstants.CURRENT_KEY_CHAT, foreign = true)
    private Chat chat;

    @DatabaseField(columnName = DatabaseConstants.CURRENT_KEY, foreign = true)
    private MessageKey messageKey;

    public CurrentKey(Chat chat, MessageKey messageKey) {
        this.chat = chat;
        this.messageKey = messageKey;
    }

    public CurrentKey() {
        //ORMLite
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public MessageKey getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(MessageKey messageKey) {
        this.messageKey = messageKey;
    }
}
