package net.yasme.android;

import java.util.ArrayList;
import java.util.Iterator;

import net.yasme.android.connection.ConnectionTask;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Message;
import net.yasme.android.entities.User;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class YasmeChat extends Activity {
	public final static String USER_MAIL = "net.yasme.andriod.USER_MAIL";
    public final static String USER_NAME = "net.yasme.andriod.USER_NAME";
	public final static String USER_ID = "net.yasme.andriod.USER_ID";
	public final static String CHAT_ID = "net.yasme.andriod.CHAT_ID";
    public final static String STORAGE_PREFS = "net.yasme.andriod.STORAGE_PREFS";


    private EditText EditMessage;
	private TextView status;
	private Chat chat;
	private String userMail;
    private String userName;
    private long userId;
    public String accessToken;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

        if (!ConnectionTask.isInitialized()) {
            ConnectionTask.initParams(getResources().getString(R.string.server_scheme),getResources().getString(R.string.server_host),getResources().getString(R.string.server_port));
        }

		Intent intent = getIntent();
		userMail = intent.getStringExtra(USER_MAIL);
        userName = intent.getStringExtra(USER_NAME);
		userId = intent.getLongExtra(USER_ID, 0);
		long chat_id = intent.getLongExtra(CHAT_ID, 0);

        SharedPreferences storage = getSharedPreferences(STORAGE_PREFS, 0);
        accessToken = storage.getString("accessToken", null);

		if (false) {
			chat = new Chat(chat_id, new User(userName, userMail, userId), this);
		} else {
            chat = new Chat(chat_id, new User(userName, userMail, userId), this);
		}

	}

	@Override
	protected void onStart() {
		super.onStart();
		initializeViews();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	public TextView getStatus() {
		return status;
	}

	private void initializeViews() {
		EditMessage = (EditText) findViewById(R.id.text_message);
		status = (TextView) findViewById(R.id.text_status);
		status.setText("Eingeloggt: " + userMail);
        status.setBackgroundDrawable(getResources().getDrawable(R.drawable.chat_text_bg_other));
	}

	public void send(View view) {
		String msg = EditMessage.getText().toString();

		if (msg.isEmpty()) {
			status.setText("Nichts eingegeben");
			return;
		}

		chat.send(msg);
		EditMessage.setText("");
		EditMessage.requestFocus();
	}

	public void update(View view) {
		status.setText("GET messages");
		chat.update();
		status.setText("GET messages done");
	}

	public void updateViews(ArrayList<Message> messages) {
		//Iterator<Message> iterator = messages.iterator();
		//Message msg = iterator.next();
        for (Message msg : messages) {
            TextView textView = new TextView((getApplicationContext()));

            LinearLayout layout = (LinearLayout) findViewById(R.id.scrollLayout);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);

            RelativeLayout row = new RelativeLayout(getApplicationContext());
            row.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT));

            textView.setText(msg.getSender().getName() + ": "
                    + msg.getMessage());
            textView.setBackgroundDrawable(getResources().getDrawable(R.drawable.chat_text_bg_other));
            textView.setTextColor(getResources().getColor(R.color.chat_text_color_other));


            if (msg.getSender().getId() == userId) {
                textView.setGravity(Gravity.RIGHT);
                row.setGravity(Gravity.RIGHT);
                textView.setBackgroundDrawable(getResources().getDrawable(R.drawable.chat_text_bg_self));
                textView.setTextColor(getResources().getColor(R.color.chat_text_color_self));
            }
            row.addView(textView);
            layout.addView(row, layoutParams);

            //if (iterator.hasNext()) {
            //    msg = iterator.next();
            //} else {
                row.setFocusableInTouchMode(true);
                row.requestFocus();
            //}
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chat, menu);
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

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_chat, container,
					false);
			return rootView;
		}
	}
}
