package net.yasme.android.ui;

import android.app.Activity;
import android.os.Bundle;

import net.yasme.android.R;
import net.yasme.android.connection.ConnectionTask;

/**
 * Created by robert on 15.06.14.
 */
public abstract class AbstractYasmeActivity  extends Activity {
    public final static String USER_MAIL = "net.yasme.android.USER_MAIL";
    public final static String USER_NAME = "net.yasme.android.USER_NAME";
    public final static String USER_ID = "net.yasme.android.USER_ID";

    public final static String CHAT_ID = "net.yasme.android.CHAT_ID";
    public final static String LAST_MESSAGE_ID = "net.yasme.android.LAST_MESSAGE_ID";

    public final static String ACCESSTOKEN = "net.yasme.android.ACCESSTOKEN";

    public final static String STORAGE_PREFS = "net.yasme.android.STORAGE_PREFS";

    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!ConnectionTask.isInitialized()) {
            ConnectionTask.initParams(getResources().getString(R.string.server_scheme),
                    getResources().getString(R.string.server_host),
                    getResources().getString(R.string.server_port));
        }
    }
}
