package net.yasme.android.ui;

import android.app.Fragment;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.GetAllUsersTask;
import net.yasme.android.entities.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bene on 22.06.14.
 */
public class InviteToChatFragment extends Fragment implements View.OnClickListener {

    private AbstractYasmeActivity activity;
    private List<User> users;
    private ListView chatPartners;
    private Button startChat;
    private ArrayAdapter<String> adapter;

    public InviteToChatFragment() { }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (AbstractYasmeActivity) getActivity();
        findViewsById();
        new GetAllUsersTask(activity.getApplicationContext(), this).execute(Long.toString(activity.getUserId()), activity.getAccessToken());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_invite_to_chat, container, false);
        return rootView;
    }

    private void findViewsById() {
        chatPartners = (ListView) activity.findViewById(R.id.inviteToChat_usersList);
        startChat = (Button) activity.findViewById(R.id.inviteToChat_startChat);
    }


    /** Will be called by the GetAllUsersTask after the list of users has been retrieved
    * @param users list
    */
    public void updateChatPartnersList(List<User> users) {
        if (null == chatPartners || null == startChat) {
            findViewsById();
        }

        this.users = users;
        String[] userNames = new String[users.size()];
        for (int i=0; i<users.size(); i++) {
            userNames[i] = users.get(i).getName();
        }
        adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_multiple_choice, userNames);
        chatPartners.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        chatPartners.setAdapter(adapter);

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

        Toast.makeText(activity, "Selected: " + selectedUsers.toString(), Toast.LENGTH_LONG).show();
        return;
    }
}
