package net.yasme.android.ui;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.GetChatDataTask;
import net.yasme.android.asyncTasks.GetProfileDataTask;
import net.yasme.android.entities.User;
import net.yasme.android.storage.DatabaseManager;

public class ChatListActivity extends AbstractYasmeActivity {

	private String userMail;
	private long userId;
    private String accessToken;
    private User self;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

		SharedPreferences storage = getSharedPreferences(STORAGE_PREFS, 0);
		userMail = storage.getString(USER_MAIL, "anonym@yasme.net");
		userId = storage.getLong(USER_ID, 0);
        accessToken = storage.getString(ACCESSTOKEN, null);

        //Initialize database (once in application)
        if(!DatabaseManager.isInitialized()) {
            DatabaseManager.init(this, userId, accessToken);
        }
        self = new User();
        self.setEmail(userMail);
        self.setId(userId);

        new GetChatDataTask(getApplicationContext(), this).execute();
        new GetProfileDataTask(getApplicationContext(), this)
                .execute(Long.toString(userId), accessToken, userMail);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
        if (id == R.id.action_chat) {
            Intent intent = new Intent(this, InviteToChatActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_contacts){
            Intent intent = new Intent(this, ContactActivity.class);
            startActivity(intent);
            return true;
        }

		return super.onOptionsItemSelected(item);
	}

    public User getSelf() {
        return self;
    }

	public void showStandardChat() {
        showChat(1);
	}


    public void showChat(long chatId) {
        System.out.println("ShowChat: " + chatId);
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(USER_MAIL, userMail);
        intent.putExtra(USER_ID, userId);
        intent.putExtra(CHAT_ID, chatId);
        intent.putExtra(USER_NAME, self.getName());
        startActivity(intent);
    }

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}
}

