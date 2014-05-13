package net.yasme.android.entities;

/**
 * Created by Stefan on 10.05.14.
 */
public class User {

    private String id;
    private String pw;

    public User(String id, String pw) {
        this.id = id;
        this.pw = pw;
    }

    public User(){}

    public String getPw() {
        return pw;
    }

    public String getId() {
        return id;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }

    // no setter for the id!!
}
