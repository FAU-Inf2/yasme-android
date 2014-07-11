package net.yasme.android.storage;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.yasme.android.entities.Chat;
import net.yasme.android.entities.User;

import java.io.Serializable;

/**
 * Created by robert on 23.06.14.
 */
@DatabaseTable(tableName = DatabaseConstants.CHAT_USER_TABLE)
public class ChatUser implements Serializable{

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(columnName = DatabaseConstants.CHAT_FIELD_NAME, foreign = true)
    private Chat chat;

    @DatabaseField(columnName = DatabaseConstants.USER_FIELD_NAME, foreign = true)
    private User user;

    public ChatUser() {
        // ORMLite needs a no-arg constructor
    }

    public ChatUser(Chat chat, User user) {
        this.chat = chat;
        this.user = user;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
