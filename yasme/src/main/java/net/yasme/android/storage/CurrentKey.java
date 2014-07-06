package net.yasme.android.storage;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.yasme.android.entities.Chat;
import net.yasme.android.entities.MessageKey;

/**
 * Created by robert on 06.07.14.
 */
@DatabaseTable(tableName = DatabaseConstants.CURRENT_KEY_TABLE)
public class CurrentKey {

    @DatabaseField(columnName = DatabaseConstants.CURRENT_KEY_CHAT)
    private Chat chat;

    @DatabaseField(columnName = DatabaseConstants.CURRENT_KEY)
    private MessageKey messageKey;

    public CurrentKey(Chat chat, MessageKey messageKey) {
        this.chat = chat;
        this.messageKey = messageKey;
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
