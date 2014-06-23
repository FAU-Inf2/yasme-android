package net.yasme.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import net.yasme.android.R;

public class InviteToChatActivity extends AbstractYasmeActivity { // implements OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_to_chat);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new InviteToChatFragment()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chatlist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }


/*

    private long userId;
    private String accessToken;

    private List<User> users;
    private ListView chatPartners;
    private Button startChat;
    private ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_to_chat);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }

        findViewsById();

        SharedPreferences storage = getSharedPreferences(STORAGE_PREFS, 0);
        userId = storage.getLong(USER_ID, 0);
        accessToken = storage.getString(ACCESSTOKEN, null);

        new GetAllUsersTask(getApplicationContext(), this).execute(Long.toString(userId), accessToken);
    }


    private void findViewsById() {
        chatPartners = (ListView) findViewById(R.id.inviteToChat_usersList);
        startChat = (Button) findViewById(R.id.inviteToChat_startChat);
    }

    **
     * Will be called by the GetAllUsersTask after the list of users has been retrieved
     * @param users list

    public void updateChatPartnersList(List<User> users) {
        if (null == chatPartners || null == startChat) {
            findViewsById();
        }

        this.users = users;
        String[] userNames = new String[users.size()];
        for (int i=0; i<users.size(); i++) {
            userNames[i] = users.get(i).getName();
        }
        arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_multiple_choice, userNames);
        chatPartners.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        chatPartners.setAdapter(arrayAdapter);

        startChat.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        SparseBooleanArray checked = chatPartners.getCheckedItemPositions();
        ArrayList<Long> selectedItems = new ArrayList<>();
        ArrayList<String> selectedUsers = new ArrayList<>();
        for (int i = 0; i < checked.size(); i++) {
            // Item position in adapter
            int position = checked.keyAt(i);
            // Add sport if it is checked i.e.) == TRUE!
            if (checked.valueAt(i)) {
                selectedItems.add(users.get(position).getId());
                selectedUsers.add(users.get(position).getName());
            }
        }

        Toast.makeText(this, "Selected: " + selectedUsers.toString(), Toast.LENGTH_LONG).show();
        return;

        // Start new start and activity

        //
        String[] outputStrArr = new String[selectedItems.size()];

        for (int i = 0; i < selectedItems.size(); i++) {
            outputStrArr[i] = selectedItems.get(i);
        }

        Intent intent = new Intent(getApplicationContext(),
                ResultActivity.class);

        // Create a bundle object
        Bundle b = new Bundle();
        b.putStringArray("selectedItems", outputStrArr);

        // Add the bundle to the intent.
        intent.putExtras(b);

        // start the ResultActivity
        startActivity(intent);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.invite_to_chat, menu);
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

    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_invite_to_chat, container, false);
            return rootView;
        }
    }*/
}
