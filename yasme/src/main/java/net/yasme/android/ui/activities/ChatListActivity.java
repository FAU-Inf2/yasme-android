package net.yasme.android.ui.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import net.yasme.android.R;
import net.yasme.android.connection.AuthorizationTask;
import net.yasme.android.connection.ConnectionTask;
import net.yasme.android.connection.ssl.HttpClient;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.ui.AbstractYasmeActivity;
import net.yasme.android.ui.fragments.ChatListFragment;

public class ChatListActivity extends AbstractYasmeActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        //progress bar
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.activity_with_single_fragment);

        if(HttpClient.context == null) {
            //TODO: temporäre Lösung:
            HttpClient.context = this.getApplicationContext();
        }

        if(!getSignedInFlag()) {
            Log.i(this.getClass().getSimpleName(), "Not logged in, starting login activity");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        }
        Log.i(this.getClass().getSimpleName(), "User is authorized");

        if(!ConnectionTask.isInitializedSession()) {
            long userId = storage.getLong(AbstractYasmeActivity.USER_ID, 0);
            String accessToken = storage.getString(AbstractYasmeActivity.ACCESSTOKEN, "");
            //initConnection Session
            //TODO: second Param should be deviceId
            ConnectionTask.initSession(userId, userId, accessToken);
        }

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
            if(!success) {
                return;
            }
            startLoginActivity();
        }
    }

    protected void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}

