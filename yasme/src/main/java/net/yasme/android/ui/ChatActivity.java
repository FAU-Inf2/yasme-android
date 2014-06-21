package net.yasme.android.ui;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
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
import android.widget.Toast;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.GetMessageTask;
import net.yasme.android.asyncTasks.GetMessageTaskInChat;
import net.yasme.android.asyncTasks.SendMessageTask;
import net.yasme.android.connection.ConnectionTask;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Message;
import net.yasme.android.entities.User;
import net.yasme.android.storage.DatabaseManager;

import java.util.ArrayList;

public class ChatActivity extends AbstractYasmeActivity {

    SharedPreferences storage;

    private EditText editMessage;
	private TextView status;

    public String accessToken;

    private Chat chat;
    private User self;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

        getActionBar().setDisplayHomeAsUpEnabled(true);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}



		Intent intent = getIntent();
		String userMail = intent.getStringExtra(USER_MAIL);
        String userName = intent.getStringExtra(USER_NAME);
		long userId = intent.getLongExtra(USER_ID, 0);
		long chatId = intent.getLongExtra(CHAT_ID, 1);

        self = new User(userName, userMail, userId);

        storage = getSharedPreferences(STORAGE_PREFS, 0);
        accessToken = storage.getString(ACCESSTOKEN, null);

        //trying to get chat with chatId from local DB
        try {
            chat = DatabaseManager.getInstance().getChat(chatId);
        } catch (NullPointerException e) {
            chat = null;
            System.out.println("[Debug] Chat aus DB holen failed");
            Toast.makeText(getApplicationContext(),
                    "[Debug] Chat aus DB holen failed", Toast.LENGTH_SHORT).show();
        }
        if(chat == null) {
            chat = new Chat(chatId, new User(userName, userMail, userId), this);
        }
	}

	@Override
	protected void onStart() {
		super.onStart();
		initializeViews();
        updateViews(chat.getMessages());
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
		editMessage = (EditText) findViewById(R.id.text_message);
		status = (TextView) findViewById(R.id.text_status);
		status.setText("Eingeloggt: " + self.getName());
	}

	public void send(View view) {
		String msg = editMessage.getText().toString();

		if (msg.isEmpty()) {
			status.setText("Nichts eingegeben");
			return;
		}

        new SendMessageTask(getApplicationContext(), this, chat.getEncryption())
                .execute(msg, self.getName(), self.getEmail(), Long.toString(self.getId()),
                        Long.toString(chat.getId()), accessToken);
		editMessage.setText("");
	}

    public void asyncUpdate() {
        status.setText("GET messages");
        new GetMessageTask(getApplicationContext(), storage)
                .execute(Long.toString(self.getId()), accessToken);
        status.setText("GET messages done");
    }

	public void update(View view) {
		status.setText("GET messages");
        new GetMessageTaskInChat(getApplicationContext(), this, chat.getEncryption(), storage)
                .execute(Long.toString(self.getId()), accessToken);
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
		int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(this) != null) {
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
			View rootView = inflater.inflate(R.layout.fragment_chat, container,
					false);
			return rootView;
		}
	}
}
