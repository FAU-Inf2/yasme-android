package net.yasme.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import net.yasme.android.BuildConfig;
import net.yasme.android.R;
import net.yasme.android.connection.ConnectionTask;
import net.yasme.android.controller.FragmentObservable;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.controller.Toastable;
import net.yasme.android.controller.Toaster;
import net.yasme.android.entities.User;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.ui.activities.ChatListActivity;
import net.yasme.android.ui.activities.ContactActivity;
import net.yasme.android.ui.activities.InviteToChatActivity;


/**
 * Created by robert on 15.06.14.
 */
public abstract class AbstractYasmeActivity  extends Activity implements Toastable {
    public final static String USER_ID = "net.yasme.android.USER_ID";
    public final static String USER_NAME = "net.yasme.android.USER_NAME";
    public final static String USER_MAIL = "net.yasme.android.USER_MAIL";
    public final static String USER_PW = "net.yasme.android.USER_PW";
    public final static String DEVICE_ID = "net.yasme.android.DEVICE_ID";

    public final static String CHAT_ID = "net.yasme.android.CHAT_ID";
    public final static String LAST_MESSAGE_ID = "net.yasme.android.LAST_MESSAGE_ID";

    public final static String ACCESSTOKEN = "net.yasme.android.ACCESSTOKEN";
    public final static String SIGN_IN = "net.yasme.android.SIGN_IN";

    public final static String STORAGE_PREFS = "net.yasme.android.STORAGE_PREFS";
    public final static String DEVICE_PREFS = "net.yasme.android.STORAGE_PREFS";
    public final static String PUSH_PREFS = "net.yasme.android.STORAGE_PREFS";


    //GCM Properties
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public static final String SENDER_ID = "688782154540"; //"104759172131";
    public static final  int   PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String TAG = "YasmeGCM";

    protected User selfUser;
    protected String accessToken;
    protected boolean mSignedIn;



   // protected SharedPreferences storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!ConnectionTask.isInitialized()) {
	    String server = getResources().getString(R.string.server_host);
	    if (BuildConfig.DEBUG) {
		    server = getResources().getString(R.string.server_host_debug);
	    }
	    Log.d(getClass().getSimpleName(), "YASME-Server: " + server);
            ConnectionTask.initParams(getResources().getString(R.string.server_scheme),
                    server,
                    getResources().getString(R.string.server_port));
        }

        SharedPreferences storage = getSharedPreferences(STORAGE_PREFS, MODE_PRIVATE);
        Long userId = storage.getLong(USER_ID, 0);
        String userName = storage.getString(USER_NAME, "dummy"); //TODO: evtl. anderen dummy namen
        String userMail = storage.getString(USER_MAIL, "@yasme.net");
        String userPw = storage.getString(USER_PW, "password");
        mSignedIn = storage.getBoolean(SIGN_IN, false);

        accessToken = storage.getString(ACCESSTOKEN, null);

        selfUser = new User();
        selfUser.setId(userId);
        selfUser.setName(userName);
        selfUser.setEmail(userMail);
        selfUser.setPw(userPw);

        //Initialize database (once in application)
        if(!DatabaseManager.INSTANCE.isInitialized()) {
            DatabaseManager.INSTANCE.init(this, storage, userId);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Toaster.getInstance().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Toaster.getInstance().remove(this);
    }

    public boolean getSignedInFlag() {
        return mSignedIn;
    }

    public void setSignedInFlag(boolean newSignedIn) {
        mSignedIn = newSignedIn;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Intent intent;
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(this) != null) {
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;
            case R.id.action_settings:
                // TODO: Settings
                return true;
            case R.id.action_chats:
                intent = new Intent(this, ChatListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.action_add_chat:
                intent = new Intent(this, InviteToChatActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            case R.id.action_contacts:
                intent = new Intent(this, ContactActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
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

    public void toast(final int messageId, final int duration) {
        String text = getApplicationContext().getResources().getString(messageId);
        toast(text, duration, -1);
    }

    public void toast(final String text, final int duration) {
        toast(text, duration, -1);
    }

    public void toast(final String text, final int duration, int gravity) {
        final int toastGravity = (-1 == gravity) ? Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL : gravity;
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                toast.setGravity(toastGravity, 0, 0);
                Log.d(getClass().getSimpleName(), "Toast: " + text);
                toast.show();
            }
        });
    }
}
