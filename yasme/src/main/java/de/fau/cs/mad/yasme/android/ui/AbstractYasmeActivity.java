package de.fau.cs.mad.yasme.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NavUtils;
import de.fau.cs.mad.yasme.android.controller.Log;
import android.view.MenuItem;
import android.widget.Toast;

import de.fau.cs.mad.yasme.android.BuildConfig;
import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.connection.ConnectionTask;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.controller.Toastable;
import de.fau.cs.mad.yasme.android.controller.Toaster;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.ui.activities.ChatListActivity;
import de.fau.cs.mad.yasme.android.ui.activities.ContactActivity;
import de.fau.cs.mad.yasme.android.ui.activities.InviteToChatActivity;
import de.fau.cs.mad.yasme.android.ui.activities.LoginActivity;


/**
 * Created by Robert Meissner <robert.meissner@studium.fau.de> on 15.06.14.
 */
public abstract class AbstractYasmeActivity  extends Activity implements Toastable {
    public final static String USER_ID = "de.fau.cs.mad.yasme.android.USER_ID";
    public final static String USER_NAME = "de.fau.cs.mad.yasme.android.USER_NAME";
    public final static String USER_MAIL = "de.fau.cs.mad.yasme.android.USER_MAIL";
    public final static String USER_PW = "de.fau.cs.mad.yasme.android.USER_PW";
    public final static String DEVICE_ID = "de.fau.cs.mad.yasme.android.DEVICE_ID";

    public final static String CHAT_ID = "de.fau.cs.mad.yasme.android.CHAT_ID";
    public final static String LAST_MESSAGE_ID = "de.fau.cs.mad.yasme.android.LAST_MESSAGE_ID";

    public final static String ACCESSTOKEN = "de.fau.cs.mad.yasme.android.ACCESSTOKEN";
    public final static String SIGN_IN = "de.fau.cs.mad.yasme.android.SIGN_IN";

    public final static String SERVERINFOUPDATETIME = "de.fau.cs.mad.yasme.android.SERVERINFOUPDATETIME";
    public final static String SERVERMESSAGE = "de.fau.cs.mad.yasme.android.SERVERMESSAGE";

    public final static String STORAGE_PREFS = "de.fau.cs.mad.yasme.android.STORAGE_PREFS";
    public final static String DEVICE_PREFS = "de.fau.cs.mad.yasme.android.STORAGE_PREFS";
    public final static String PUSH_PREFS = "de.fau.cs.mad.yasme.android.STORAGE_PREFS";


    //GCM Properties
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public static final String SENDER_ID = "688782154540"; //"104759172131";
    public static final  int   PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String TAG = "YasmeGCM";

    protected User selfUser;



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
                        getResources().getString(R.string.server_port),getResources().getString(R.string.language),getVersion());
        }

        SharedPreferences storage = getSharedPreferences(STORAGE_PREFS, MODE_PRIVATE);
        Long userId = storage.getLong(USER_ID, 0);
        String userName = storage.getString(USER_NAME, "dummy");
        String userMail = storage.getString(USER_MAIL, "@yasme.net");
        String userPw = storage.getString(USER_PW, "password");


        selfUser = new User();
        selfUser.setId(userId);
        selfUser.setName(userName);
        selfUser.setEmail(userMail);
        selfUser.setPw(userPw);

        //Initialize databaseManager (once in application)
        if(!DatabaseManager.INSTANCE.isInitialized()) {
            DatabaseManager.INSTANCE.init(this, storage, userId);
        }

        String accessToken = DatabaseManager.INSTANCE.getAccessToken();
        if ((accessToken == null || accessToken.length() <= 0) && !this.getClass().equals(LoginActivity.class)) {
            Log.i(this.getClass().getSimpleName(), "Not logged in, starting login activity");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Toaster.getInstance().register(this);
        stopSpinning();
        SpinnerObservable.getInstance().registerActivity(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        String accessToken = DatabaseManager.INSTANCE.getAccessToken();
        if ((accessToken == null || accessToken.length() <= 0) && !this.getClass().equals(LoginActivity.class)) {
            Log.i(this.getClass().getSimpleName(), "Not logged in, starting login activity");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Toaster.getInstance().remove(this);
        SpinnerObservable.getInstance().removeActivity(this);
    }

    public void setActionBarTitle(String title){
        getActionBar().setTitle(title);
    }

    public void setActionBarTitle(String title, String subtitle) {
        getActionBar().setTitle(title);
        getActionBar().setSubtitle(subtitle);
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
        return DatabaseManager.INSTANCE.getAccessToken();
    }

    public void toast(final int messageId, final int duration) {
        String text = getApplicationContext().getResources().getString(messageId);
        toast(text, duration, -1);
    }

    public void toast(final String text, final int duration) {
        toast(text, duration, -1);
    }

    public void toast(final String text, final int duration, final int gravity) {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                if (-1 != gravity) {
                    toast.setGravity(gravity, 0, 0);
                }
                // otherwise use default position
                Log.d(getClass().getSimpleName(), "Toast: " + text);
                toast.show();
            }
        });
    }

    public void startSpinning() {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                setProgressBarIndeterminateVisibility(true);
            }
        });
    }

    public void stopSpinning() {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                setProgressBarIndeterminateVisibility(false);
            }
        });
    }

    public int getVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(),e.getMessage());
            return 0;
        }
    }

}
