package net.yasme.android;

import java.util.ArrayList;
import java.util.Iterator;

import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Id;
import net.yasme.android.entities.Message;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class YasmeChat extends Activity {
	public final static String USER_NAME = "net.yasme.andriod.USER_NAME";
	public final static String USER_ID = "net.yasme.andriod.USER_ID";
	public final static String CHAT_ID = "net.yasme.andriod.CHAT_ID";


	private EditText EditMessage;
	private TextView status;
	private Chat chat;
	private String user_name;
	private Id user_id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		Intent intent = getIntent();
		user_name = intent.getStringExtra(USER_NAME);
		
		String user_string = intent.getStringExtra(USER_ID);
		user_id = new Id(Long.parseLong(user_string));
		String url = getResources().getString(R.string.server_url);
		int chat_int = intent.getIntExtra(CHAT_ID, 1);
		Id chat_id = new Id(chat_int);
		if(false) {
			//TODO: existierenden Chat verwenden
		} else {
			chat = new Chat(chat_id, user_id, url, this);
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
		status.setText("Eingeloggt: " + user_name);
	}

	public void send(View view) {
		String msg = EditMessage.getText().toString();

		if (msg.isEmpty()) {
			status.setText("Nichts eingegeben");
			return;
		}
				
		chat.send(msg);
		EditMessage.setText("");
		msg = null;
	}

	public void update(View view) {
		status.setText("GET messages");
		chat.update();
		status.setText("GET messages done");
	}

	public void updateViews(ArrayList<Message> messages) {
		Iterator<Message> iterator = messages.iterator();
		Message msg = iterator.next();
		for (int i = 0; i < messages.size(); i++) {
			TextView textView = new TextView((getApplicationContext()));
			
			
			LinearLayout layout = (LinearLayout) findViewById(R.id.scrollLayout);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);

			LinearLayout row = new LinearLayout(getApplicationContext());
			row.setLayoutParams(new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT));
	
			textView.setText(msg.getSender().getName() + ": " + msg.getMessage());
			
			if(msg.getSender().getId().getId() == user_id.getId()) {
				textView.setGravity(Gravity.RIGHT);
				row.setGravity(Gravity.RIGHT);
			}
			row.addView(textView);
			layout.addView(row, layoutParams);

			if (iterator.hasNext()) {
				msg = iterator.next();
			}
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
