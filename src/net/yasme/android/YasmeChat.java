package net.yasme.android;

import java.util.ArrayList;
import java.util.Iterator;

import net.yasme.android.connection.MessageTask;
import net.yasme.android.entities.Message;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class YasmeChat extends Activity {

	private EditText EditMessage;
	private TextView status;
	private TextView chatView[];
	private String usr_name;
	private String url;

	private MessageTask messageTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		Intent intent = getIntent();
		usr_name = intent.getStringExtra(YasmeHome.USER_NAME);

	}

	@Override
	protected void onStart() {
		super.onStart();
		initializeViews();
		url = getResources().getString(R.string.server_url);
		messageTask = new MessageTask(url);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	private void initializeViews() {
		chatView = new TextView[7];
		EditMessage = (EditText) findViewById(R.id.text_message);
		status = (TextView) findViewById(R.id.text_status);
		chatView[0] = (TextView) findViewById(R.id.textView1);
		chatView[1] = (TextView) findViewById(R.id.textView2);
		chatView[2] = (TextView) findViewById(R.id.textView3);
		chatView[3] = (TextView) findViewById(R.id.textView4);
		chatView[4] = (TextView) findViewById(R.id.textView5);
		chatView[5] = (TextView) findViewById(R.id.textView6);
		chatView[6] = (TextView) findViewById(R.id.textView7);
		status.setText("Eingeloggt: " + usr_name);
	}

	public void send(View view) {
		
		String msg = EditMessage.getText().toString();

		new SendMessageTask().execute(msg, usr_name); 
	}

	private class SendMessageTask extends AsyncTask<String, Void, Boolean> {

		String msg;
		protected Boolean doInBackground(String... params) {
			// Create Message Object with params
			// messageTask.sendMessage(message); <-- REST Server Call
			// Return Value: true = success false = failed

			msg = params[0];
			if (!msg.isEmpty()) {

				// creating message object
				// TODO: get uid from usr_name, usr_name = params[1]
				long uid = 001;
				Message message = new Message(uid, 0, msg);
			
				messageTask.sendMessage(message);

				return true;
			} else {
				return false;
			}
		}

		protected void onPostExecute(Boolean result) {
			// if doInBackground returned true
			// set Message to textViews
			if(result) {
				for (int i = chatView.length - 1; i > 0; i--) {
					chatView[i].setText(chatView[i - 1].getText().toString());
				}
				chatView[0].setText(usr_name + ": " + msg);
				
				status.setText("Gesendet: " + msg);
				msg = null;
				EditMessage.setText("");
				
			} else {
				status.setText("Nichts eingegeben");
			}
		}
	}

	public void update(View view) {
		String lastMessageID = "";
		new GetMessageTask().execute(lastMessageID);
		
	}
	
	private class GetMessageTask extends AsyncTask<String, Void, Boolean> {

		ArrayList<Message> messages;
		protected Boolean doInBackground(String... params) {
			// messageTask.getMessage(String lastMessageID); <-- REST Server Call
			// Return Value: true = success false = failed
			
			messages = messageTask.getMessage(params[0]);
			if (messages.isEmpty()) {
				status.setText("Keine neuen Nachrichten");
				return false;
			}
			return true;
		}

		protected void onPostExecute(Boolean result) {
			// if doInBackground returned true
			// set Message to textViews
			
			Iterator<Message> iterator = messages.iterator();
			int size = messages.size();
			if (size >= chatView.length) {
				for (int i = chatView.length - 1; i >= 0; i--) {
					chatView[i].setText(iterator.next().getMessage());
				}
			} else {
				for (int i = chatView.length - 1; i >= size; i--) {
					chatView[i].setText(chatView[i - size].getText().toString());
				}
				for (int i = size - 1; i >= 0; i--) {
					chatView[i].setText(iterator.next().getMessage());
				}
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

	// LocalService instance
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