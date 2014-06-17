package net.yasme.android.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.yasme.android.storage.DatabaseConstants;

import java.io.Serializable;

/**
 * Created by Stefan on 10.05.14.
 */

@DatabaseTable(tableName = "users")
public class User implements Serializable {

    @DatabaseField(columnName = DatabaseConstants.USER_ID, id = true)
    private long id;

    @DatabaseField(columnName = DatabaseConstants.USER_MAIL)
    private String email;

    @DatabaseField(columnName = DatabaseConstants.USER_NAME)
    private String name;

    private String pw;

    // private List<Chat> chats = new ArrayList<Chat>();        // list of all chats the user participates


    public User(String pw, String name, String email) {
        this.pw = pw;
        this.name = name;
        this.email = email;
    }

    public User(String email, String pw) {
        this.email = email;
        this.pw = pw;
    }

    //TODO: changeOrder
    public User(String name, long id){
        this.name = name;
        this.id = id;
    }

    public User(String name, String email, long id){
        this.name = name;
        this.email = email;
        this.id = id;
    }

    public User() {
    }

    /*
    public List<Chat> getChats() {
        return this.chats;
    }
    */

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPw() {
        return pw;
    }

    public long getId() {
        return id;
    }

    /*
    public void setChat(Chat chat) {
        this.chats.add(chat);
    }
    */

    public void setPw(String pw) {
        this.pw = pw;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(long id) {
        this.id = id;
    }

    /*
    public void removeChat(Chat chat) {
        chats.remove(chat);
    }
    */
}

