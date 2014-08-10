package de.fau.cs.mad.yasme.android.ui.activities;

import android.util.Log;
import android.os.Bundle;
import android.view.Menu;
import android.view.Window;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;
import de.fau.cs.mad.yasme.android.ui.fragments.LoginFragment;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends AbstractYasmeActivity {


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //progress bar in actionbar
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_with_single_fragment);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.singleFragmentContainer, new LoginFragment()).commit();
        }
        //TODO: temporäre Lösung: - moved to onCreate in ChatListActivity
        //HttpClient.context = this.getApplicationContext();

        //GCM Begin
//        cloudMessaging = CloudMessaging.getInstance(this);
//
//        if (cloudMessaging.checkPlayServices()) {
//            String regid = cloudMessaging.getRegistrationId();
//            Log.d(this.getClass().getSimpleName(),"Google reg id is empty? " + regid.isEmpty());
//            if (regid.isEmpty()) {
//                registerInBackground();
//            }
//        } else {
//            Log.i(AbstractYasmeActivity.TAG, "No valid Google Play Services APK found.");
//        }
        //GCM End
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }


//    @Override
//    protected void onResume() {
//        super.onResume();
//        cloudMessaging.checkPlayServices();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//    }
//
//    public void registerInBackground() {
//
//        new AsyncTask<Void, Void, String>() {
//
//            protected String doInBackground(Void[] params) {
//                return cloudMessaging.registerInBackground();
//            }
//
//            protected void onPostExecute(String msg) {
//                //Zu diesem Zeitpunkt ist die RegId bereits als SharedPref.
//                // in AbstractYasmeActivity.PROPERTY_REG_ID abgelegt.
//                Log.d(this.getClass().getSimpleName(),msg);
//
//            }
//
//        }.execute(null, null, null);
//    }
}
