package net.yasme.android.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.YasmeDeviceRegistrationTask;
import net.yasme.android.asyncTasks.UserLoginTask;
import net.yasme.android.asyncTasks.UserRegistrationTask;
import net.yasme.android.connection.ssl.HttpClient;
import net.yasme.android.gcm.CloudMessaging;
import net.yasme.android.storage.DatabaseManager;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends AbstractYasmeActivity {

    private CloudMessaging cloudMessaging = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //TODO: temporäre Lösung:
        HttpClient.context = this.getApplicationContext();

        //GCM Begin
        cloudMessaging = CloudMessaging.getInstance(this);

        if (cloudMessaging.checkPlayServices()) {
            String regid = cloudMessaging.getRegistrationId();
            System.out.println("[DEBUG] Empty?" + regid.isEmpty());
            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i(AbstractYasmeActivity.TAG, "No valid Google Play Services APK found.");
        }
        //GCM End
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }





    @Override
    protected void onResume() {
        super.onResume();
        cloudMessaging.checkPlayServices();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void registerInBackground() {

        new AsyncTask<Void, Void, String>() {

            protected String doInBackground(Void[] params) {
                return cloudMessaging.registerInBackground();
            }

            protected void onPostExecute(String msg) {
                //Zu diesem Zeitpunkt ist die RegId bereits als SharedPref.
                // in AbstractYasmeActivity.PROPERTY_REG_ID abgelegt.
                System.out.println(msg);

            }

        }.execute(null, null, null);
    }
}