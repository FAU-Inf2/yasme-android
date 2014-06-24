package net.yasme.android.ui;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
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
import net.yasme.android.encryption.MessageEncryption;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Message;
import net.yasme.android.entities.User;
import net.yasme.android.storage.DatabaseManager;

import java.util.ArrayList;

public class ChatActivity extends AbstractYasmeActivity {

    SharedPreferences storage;

    private EditText editMessage;
	private TextView status;

    private Chat chat;

    MessageEncryption aes;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_with_single_fragment);

        getActionBar().setDisplayHomeAsUpEnabled(true);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.singleFragmentContainer, new ChatFragment()).commit();
		}
        chat.setParticipants(DatabaseManager.getInstance().getParticipantsFromDB(chatId));
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
        return super.onOptionsItemSelected(item);
	}
}
