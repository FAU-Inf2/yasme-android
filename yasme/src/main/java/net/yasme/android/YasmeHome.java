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
import net.yasme.android.connection.UserTask;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.DatabaseManager;

import java.util.ArrayList;

public class YasmeHome extends Activity {
	public final static String USER_MAIL = "net.yasme.andriod.USER_MAIL";
	public final static String USER_ID = "net.yasme.andriod.USER_ID";
    public final static String CHAT_ID = "net.yasme.andriod.CHAT_ID";
    public final static String STORAGE_PREFS = "net.yasme.andriod.STORAGE_PREFS";

	private String user_mail;
	private long user_id;
    private String url;
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
		user_mail = storage.getString(USER_MAIL, "anonym@yasme.net");
		user_id = storage.getLong(USER_ID, 0);
        accessToken = storage.getString("accessToken", null);
        url = getResources().getString(R.string.server_url);

        //Initialize database (once in application)
        DatabaseManager.init(this);

        show_chatrooms();
        new GetProfileDataTask().execute(Long.toString(user_id), accessToken);
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
		// BZZZTT!!1!
		// findViewById(R.id.button1).performHapticFeedback(2);
		Intent intent = new Intent(this, YasmeChat.class);
		intent.putExtra(USER_MAIL, user_mail);
		intent.putExtra(USER_ID, user_id);
        intent.putExtra(CHAT_ID, (long)0);
		startActivity(intent);
	}


    public void showChat(long chat_id) {
        Intent intent = new Intent(this, YasmeChat.class);
        intent.putExtra(USER_MAIL, user_mail);
        intent.putExtra(USER_ID, user_id);
        intent.putExtra(CHAT_ID, chat_id);
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
                self = UserTask.getInstance().getUserData(user_id, accessToken);
            } catch (RestServiceException e) {
                System.out.println(e.getMessage());
                return false;
            }
            return self != null;
        }

        protected void onPostExecute(final Boolean success) {
            if(!success) {
                return;
            }
            TextView profileInfo = (TextView) findViewById(R.id.profileInfo);
            profileInfo.setText(self.getName() + ": " + user_mail);
        }
    }

    public class GetChatDataTask extends AsyncTask<String, Void, Boolean> {
        ChatTask chatTask;
        ArrayList<Chat> chatrooms;
        protected Boolean doInBackground(String... params) {
            chatTask = ChatTask.getInstance();
            int numberOfChats = 16;
            //TODO: print chats dynamic
            for (int i = 2; i < numberOfChats; i++) {
                Chat chat;

                try {
                    chat = chatTask.getInfoOfChat(i ,user_id, accessToken);
                } catch (RestServiceException e) {
                    System.out.println(e.getMessage());
                    return false;
                }
                chatrooms.add(chat);
            }
            return chatrooms != null;
        }

        protected void onPostExecute(final Boolean success) {
            if(!success) {
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
