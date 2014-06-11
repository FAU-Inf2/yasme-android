package net.yasme.android;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.yasme.android.connection.ChatTask;
import net.yasme.android.connection.UserTask;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;

public class YasmeHome extends Activity {
	public final static String USER_MAIL = "net.yasme.andriod.USER_MAIL";
	public final static String USER_ID = "net.yasme.andriod.USER_ID";
	public final static String STORAGE_PREFS = "net.yasme.andriod.STORAGE_PREFS";

	String user_mail;
	long user_id;
    String url;

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
        String accessToken = storage.getString("accessToken", null);
        url = getResources().getString(R.string.server_url);

        //show_chatrooms();

        User user = null;
        try {
            user = new UserTask(url).getUserData(user_id, accessToken);
        } catch (RestServiceException e) {
            e.printStackTrace();
        }
        TextView profileInfo = (TextView) findViewById(R.id.profileInfo);
        profileInfo.setText(user.getName() + ": " + user_mail);
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
		return super.onOptionsItemSelected(item);
	}

	public void showChat(View view) {
		// BZZZTT!!1!
		// findViewById(R.id.button1).performHapticFeedback(2);
		Intent intent = new Intent(this, YasmeChat.class);
		intent.putExtra(USER_MAIL, user_mail);
		intent.putExtra(USER_ID, user_id);
		startActivity(intent);
	}


    public void show_chatrooms() {
        LinearLayout table = (LinearLayout) findViewById(R.id.chatroom_list);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        ChatTask chatTask = ChatTask.getInstance();

        for (int i = 2; i < 16; i++) {
            TextView name = new TextView((getApplicationContext()));
            TextView status = new TextView((getApplicationContext()));

            RelativeLayout row = new RelativeLayout(getApplicationContext());
            row.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT));

            Chat chat = null;

            try {
                //TODO: AccessToken auslesen und als String sendMessage übergeben
                //Current: Default Value 0
                chat = chatTask.getInfoOfChat(i,user_id,"0");
            } catch (RestServiceException e) {
                Toast.makeText(getApplicationContext(), "Rest error", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            name.setText(chat.getName());
            status.setText(chat.getStatus());

            row.addView(name);
            row.addView(status);
            table.addView(row, layoutParams);
        }
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
