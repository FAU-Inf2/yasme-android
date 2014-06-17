package net.yasme.android;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.yasme.android.connection.ChatTask;
import net.yasme.android.connection.ConnectionTask;
import net.yasme.android.connection.UserTask;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.DatabaseManager;

import java.util.ArrayList;

public class YasmeHome extends Activity {

	private String userMail;
	private long userId;
    private String accessToken;
    private User selfProfile;
    private User self;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

        if (!ConnectionTask.isInitialized()) {
            ConnectionTask.initParams(getResources().getString(R.string.server_scheme),getResources().getString(R.string.server_host),getResources().getString(R.string.server_port));
        }

		SharedPreferences storage = getSharedPreferences(Constants.STORAGE_PREFS, 0);
		userMail = storage.getString(Constants.USER_MAIL, "anonym@yasme.net");
		userId = storage.getLong(Constants.USER_ID, 0);
        accessToken = storage.getString(Constants.ACCESSTOKEN, null);

        //Initialize database (once in application)
        if(!DatabaseManager.isInitialized()) {
            DatabaseManager.init(this, userId, accessToken);
        }
        self = new User();
        self.setEmail(userMail);
        self.setId(userId);

        show_chatrooms();
        new GetProfileDataTask().execute(Long.toString(userId), accessToken);
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
            showStandardChat();
            return true;
        }
		return super.onOptionsItemSelected(item);
	}


	public void showStandardChat() {
		Intent intent = new Intent(this, YasmeChat.class);
		intent.putExtra(Constants.USER_MAIL, userMail);
		intent.putExtra(Constants.USER_ID, userId);
        intent.putExtra(Constants.CHAT_ID, (long)1);
        intent.putExtra(Constants.USER_NAME, self.getName());
		startActivity(intent);
	}


    public void showChat(long chatId) {
        Intent intent = new Intent(this, YasmeChat.class);
        intent.putExtra(Constants.USER_MAIL, userMail);
        intent.putExtra(Constants.USER_ID, userId);
        intent.putExtra(Constants.CHAT_ID, chatId);
        intent.putExtra(Constants.USER_NAME, self.getName());
        startActivity(intent);
    }


    public void show_chatrooms() {

        new GetChatDataTask().execute();
       // LinearLayout table = (LinearLayout) findViewById(R.id.chatroom_list);

    }

    View.OnClickListener chatClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //TODO: get chat_id from table row, maybe with Adapter class
            long chat_id = 0;
            showChat(chat_id);
        }
    };

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

    public class GetProfileDataTask extends AsyncTask<String, Void, Boolean> {

        protected Boolean doInBackground(String... params) {
            long user_id = Long.parseLong(params[0]);
            String accessToken = params[1];
            try {
                selfProfile = UserTask.getInstance().getUserData(user_id, accessToken);
            } catch (RestServiceException e) {
                System.out.println(e.getMessage());
                return false;
            }
            return selfProfile != null;
        }

        protected void onPostExecute(final Boolean success) {
            if(!success) {
                return;
            }
            self.setName(selfProfile.getName());
            TextView profileInfo = (TextView) findViewById(R.id.profileInfo);
            profileInfo.setText(selfProfile.getName() + ": " + userMail);
        }
    }

    public class GetChatDataTask extends AsyncTask<String, Void, Boolean> {
        //ChatTask chatTask;
        ArrayList<Chat> chatrooms = null;
        protected Boolean doInBackground(String... params) {
            //chatrooms = DatabaseManager.getInstance().getAllChats();
            return chatrooms != null;
        }

        protected void onPostExecute(final Boolean success) {
            if(!success) {
                //TODO: Debug
                System.out.println("Fehler bei Datenbankzugriff");
                return;
            }
            LinearLayout table = (LinearLayout) findViewById(R.id.chatroom_list);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);

            for (Chat chat : chatrooms) {
                TextView name = new TextView((getApplicationContext()));
                TextView status = new TextView((getApplicationContext()));

                RelativeLayout row = new RelativeLayout(getApplicationContext());
                row.setLayoutParams(new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT));

                name.setText(chat.getName());
                status.setText(chat.getStatus());

                row.setOnClickListener(chatClickListener);

                row.addView(name);
                row.addView(status);
                table.addView(row, layoutParams);
            }
        }
    }
}
