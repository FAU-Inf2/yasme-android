package net.yasme.android.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import net.yasme.android.R;
import net.yasme.android.connection.ConnectionTask;
import net.yasme.android.entities.User;
import net.yasme.android.storage.DatabaseManager;

/**
 * Created by robert on 15.06.14.
 */
public abstract class AbstractYasmeActivity  extends Activity {
    public final static String USER_ID = "net.yasme.android.USER_ID";
    public final static String USER_NAME = "net.yasme.android.USER_NAME";
    public final static String USER_MAIL = "net.yasme.android.USER_MAIL";
    public final static String USER_PW = "net.yasme.android.USER_PW";
    public final static String DEVICE_ID = "net.yasme.android.DEVICE_ID";

    public final static String CHAT_ID = "net.yasme.android.CHAT_ID";
    public final static String LAST_MESSAGE_ID = "net.yasme.android.LAST_MESSAGE_ID";

    public final static String ACCESSTOKEN = "net.yasme.android.ACCESSTOKEN";

    public final static String STORAGE_PREFS = "net.yasme.android.STORAGE_PREFS";
    public final static String DEVICE_PREFS = "net.yasme.android.STORAGE_PREFS";

    //GCM Properties
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public static final String SENDER_ID = "104759172131";
    public static final  int   PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    protected User selfUser;
    protected String accessToken;

    protected SharedPreferences storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!ConnectionTask.isInitialized()) {
            ConnectionTask.initParams(getResources().getString(R.string.server_scheme),
                    getResources().getString(R.string.server_host),
                    getResources().getString(R.string.server_port));
        }

        storage = getSharedPreferences(STORAGE_PREFS, 0);
        Long userId = storage.getLong(USER_ID, 0);
        String userName = storage.getString(USER_NAME, "anonym");
        String userMail = storage.getString(USER_MAIL, "anonym@yasme.net");
        String userPw = storage.getString(USER_PW, "password");

        accessToken = storage.getString(ACCESSTOKEN, null);

        //Initialize database (once in application)
        if(!DatabaseManager.isInitialized()) {
            DatabaseManager.init(this, userId, accessToken);
        }
        selfUser = new User();
        selfUser.setId(userId);
        selfUser.setName(userName);
        selfUser.setEmail(userMail);
        selfUser.setPw(userPw);

    }

    public User getSelfUser() {
        return selfUser;
    }

    public String getUserMail() {
        return selfUser.getEmail();
    }

    public long getUserId() {
        return selfUser.getId();
    }

    public String getAccessToken() {
        return accessToken;
    }
}
