package net.yasme.android.entities;

/**
 * Created by martin on 08.08.2014.
 */
public class ServerInfo {
    String message;
    boolean loginAllowed = true;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getLoginAllowed() {
        return loginAllowed;
    }

    public void setLoginAllowed(boolean loginAllowed) {
        this.loginAllowed = loginAllowed;
    }

    public boolean hasMessage() {
        return message != null && message.length() > 0;
    }
}
