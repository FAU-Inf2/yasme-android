package net.yasme.android.entities;


/**
 * Created by Stefan on 24.05.14.
 */
public class LoginUser {

    private Long userID;
    private String pw;

    public LoginUser(Long userID, String pw){
        this.userID = userID;
        this.pw = pw;
    }

    public LoginUser(){}

    public Long getUserID() {
        return userID;
    }

    public String getPw() {
        return pw;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }
}
