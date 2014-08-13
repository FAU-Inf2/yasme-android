package de.fau.cs.mad.yasme.android.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import de.fau.cs.mad.yasme.android.controller.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.connection.AuthorizationTask;
import de.fau.cs.mad.yasme.android.connection.ssl.HttpClient;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;
import de.fau.cs.mad.yasme.android.ui.fragments.ChatListFragment;

public class ChatListActivity extends AbstractYasmeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //progress bar in actionbar
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_with_single_fragment);

        if (HttpClient.context == null) {
            //TODO: temporäre Lösung:
            HttpClient.context = this.getApplicationContext();
        }

        SharedPreferences devicePrefs = getSharedPreferences(DEVICE_PREFS, MODE_PRIVATE);

//        if (!getSignedInFlag()) {
//            Log.i(this.getClass().getSimpleName(), "Not logged in, starting login activity");
//            Intent intent = new Intent(this, LoginActivity.class);
////						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
//			finish();
//            return;
//        }

        // Make sure that the device has been registered. Otherwise several other tasks will fail
        long deviceId = DatabaseManager.INSTANCE.getDeviceId();
        if (deviceId <= 0) {
            Log.e(this.getClass().getSimpleName(), "Device id should not be <= 0 after login. Looks like the device registration failed but no one was notified about that");
        }

        //else {
            //Initialize database (once in application)
        //    if (!DatabaseManager.INSTANCE.isDBInitialized()) {
        //        long userId =
        //                getSharedPreferences(AbstractYasmeActivity.STORAGE_PREFS, MODE_PRIVATE)
        //                        .getLong(AbstractYasmeActivity.USER_ID, 0);
        //        DatabaseManager.INSTANCE.initDB(this, userId);
        //    }
        //}

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.singleFragmentContainer, new ChatListFragment()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chatlist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_add_chat) {
            Intent intent = new Intent(this, InviteToChatActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.sign_out) {
            new LogoutTask().execute();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void startLoginActivity() {
        //setSignedInFlag(false);
        DatabaseManager.INSTANCE.setAccessToken(null);
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private class LogoutTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                AuthorizationTask.getInstance().logoutUser();
            } catch (RestServiceException e) {
                Log.e(this.getClass().getSimpleName(), e.getMessage());
                Log.i(this.getClass().getSimpleName(), "SignOut nicht erfolgreich");
                return false;
            }
            return true;
        }

        protected void onPostExecute(Boolean success) {
            if (!success) {
                return;
            }
            startLoginActivity();
        }
    }
}

