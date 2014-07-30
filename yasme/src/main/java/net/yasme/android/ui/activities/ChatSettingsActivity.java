package net.yasme.android.ui.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import net.yasme.android.R;
import net.yasme.android.entities.Chat;
import net.yasme.android.ui.AbstractYasmeActivity;
import net.yasme.android.ui.fragments.ChatSettingsFragment;

/**
 * Created by robert on 28.07.14.
 */
public class ChatSettingsActivity extends AbstractYasmeActivity {
    private Chat chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_with_single_fragment);
        chat = (Chat) getIntent().getSerializableExtra("chat");

        if (savedInstanceState == null) {
            Bundle args = new Bundle();
            args.putSerializable("chat", chat);
            ChatSettingsFragment csf = new ChatSettingsFragment();
            csf.setArguments(args);
            getFragmentManager().beginTransaction()
                    .add(R.id.singleFragmentContainer, csf).commit();
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
        return super.onOptionsItemSelected(item);
    }
}
