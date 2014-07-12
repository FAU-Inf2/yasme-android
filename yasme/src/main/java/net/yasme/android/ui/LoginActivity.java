package net.yasme.android.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import net.yasme.android.R;
import net.yasme.android.connection.ssl.HttpClient;
import net.yasme.android.gcm.CloudMessaging;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends AbstractYasmeActivity {

    private CloudMessaging cloudMessaging = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_single_fragment);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.singleFragmentContainer, new LoginFragment()).commit();
        }

        //TODO: temporäre Lösung: - moved to onCreate in ChatListActivity
        //HttpClient.context = this.getApplicationContext();

        //GCM Begin
        cloudMessaging = CloudMessaging.getInstance(this);

        if (cloudMessaging.checkPlayServices()) {
            String regid = cloudMessaging.getRegistrationId();
            Log.d(this.getClass().getSimpleName(),"Empty?" + regid.isEmpty());
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
                Log.d(this.getClass().getSimpleName(),msg);

            }

        }.execute(null, null, null);
    }
}