package net.yasme.android;

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

import net.yasme.android.asyncTasks.GetMessageTask;
import net.yasme.android.asyncTasks.SendMessageTask;
import net.yasme.android.connection.ConnectionTask;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Message;
import net.yasme.android.entities.User;
import net.yasme.android.storage.DatabaseManager;

import java.util.ArrayList;

public class YasmeChat extends Activity {

    SendMessageTask sendTask;
    GetMessageTask getTask;

    private EditText EditMessage;
	private TextView status;

    public String accessToken;

    private Chat chat;
    private User self;

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
		String userMail = intent.getStringExtra(Constants.USER_MAIL);
        String userName = intent.getStringExtra(Constants.USER_NAME);
		long userId = intent.getLongExtra(Constants.USER_ID, 0);
		long chatId = intent.getLongExtra(Constants.CHAT_ID, 1);

        self = new User(userName, userMail, userId);

        SharedPreferences storage = getSharedPreferences(Constants.STORAGE_PREFS, 0);
        accessToken = storage.getString(Constants.ACCESSTOKEN, null);

        //Initialize database (once in application)
        if(!DatabaseManager.isInitialized()) {
            DatabaseManager.init(this, userId, accessToken);
        }

        //trying to get chat with chatId from local DB
        try {
            chat = DatabaseManager.getInstance().getChat(chatId);
        } catch (NullPointerException e) {
            chat = null;
        }
        if(chat == null) {
            chat = new Chat(chatId, new User(userName, userMail, userId), this);
        }

        sendTask = new SendMessageTask(getApplicationContext(), this, chat.getEncryption());
        getTask = new GetMessageTask(getApplicationContext(), this, chat.getEncryption());
	}

	@Override
	protected void onStart() {
		super.onStart();
		initializeViews();
	}

	@Override
	protected void onStop() {
        DatabaseManager.getInstance().updateChat(chat);
		super.onStop();
	}

	public TextView getStatus() {
		return status;
	}

	private void initializeViews() {
		EditMessage = (EditText) findViewById(R.id.text_message);
		status = (TextView) findViewById(R.id.text_status);
		status.setText("Eingeloggt: " + self.getName());
        status.setBackgroundDrawable(getResources().getDrawable(R.drawable.chat_text_bg_other));
	}

	public void send(View view) {
		String msg = EditMessage.getText().toString();

		if (msg.isEmpty()) {
			status.setText("Nichts eingegeben");
			return;
		}

        sendTask.execute(msg, self.getName(), self.getEmail(), Long.toString(self.getId()));
		EditMessage.setText("");
	}

	public void update(View view) {
		status.setText("GET messages");
        getTask.execute(Long.toString(lastMessageId), Long.toString(self.getId()), accessToken);
		status.setText("GET messages done");
	}

	public void updateViews(ArrayList<Message> messages) {
        for (Message msg : messages) {
            TextView textView = new TextView(getApplicationContext());

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
            //textView.setPadding(10,10,10,10);

            //ViewGroup.MarginLayoutParams marginLayoutParams = new ViewGroup.MarginLayoutParams(textView.getLayoutParams());
            //marginLayoutParams.setMargins(10,10,10,10);
            //RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(marginLayoutParams);
            //LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            //params.setMargins(10,10,10,10);
            //extView.setLayoutParams(params);



            if (msg.getSender().getId() == self.getId()) {
                textView.setGravity(Gravity.RIGHT);
                row.setGravity(Gravity.RIGHT);
                textView.setBackgroundDrawable(getResources().getDrawable(R.drawable.chat_text_bg_self));
                textView.setTextColor(getResources().getColor(R.color.chat_text_color_self));
            }
            row.addView(textView);
            layout.addView(row, layoutParams);

            row.setFocusableInTouchMode(true);
            row.requestFocus();
            findViewById(R.id.text_message).requestFocus();
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
