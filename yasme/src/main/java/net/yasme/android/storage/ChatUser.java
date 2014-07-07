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
    long id;

    @DatabaseField(columnName = DatabaseConstants.CHAT_FIELD_NAME, foreign = true)
    Chat chat;

    @DatabaseField(columnName = DatabaseConstants.USER_FIELD_NAME, foreign = true)
    User user;

    public ChatUser() {
        // ORMLite needs a no-arg constructor
    }

    public ChatUser(Chat chat, User user) {
        this.chat = chat;
        this.user = user;
    }
}
