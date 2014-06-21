package net.yasme.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import net.yasme.android.R;

public class ChatListActivity extends AbstractYasmeActivity {



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chatlist);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new ChatListFragment()).commit();
		}

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






}

