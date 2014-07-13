package net.yasme.android.ui.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.server.CreateChatTask;
import net.yasme.android.asyncTasks.server.GetAllUsersTask;
import net.yasme.android.controller.FragmentObservable;
import net.yasme.android.controller.NotifiableFragment;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.entities.User;
import net.yasme.android.ui.AbstractYasmeActivity;
import net.yasme.android.ui.activities.ChatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bene on 22.06.14.
 */
public class InviteToChatFragment
        extends Fragment
        implements View.OnClickListener, NotifiableFragment<Long> {

    private AbstractYasmeActivity activity;
    private List<User> users;
    private ListView chatPartners;
    private Button startChat;
    private ArrayAdapter<String> adapter;

    public InviteToChatFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Register at observer
        Log.d(this.getClass().getSimpleName(), "Try to get ChatListObservableInstance");
        FragmentObservable<InviteToChatFragment, Long> obs =
                ObservableRegistry.getObservable(InviteToChatFragment.class);
        Log.d(this.getClass().getSimpleName(), "... successful");
        if (!obs.isRegistered(this)) {
            obs.register(this);
        }

        activity = (AbstractYasmeActivity) getActivity();
        findViewsById();
        new GetAllUsersTask(this).execute();
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


    /**
     * Will be called by the GetAllUsersTask after the list of users has been retrieved
     *
     * @param users list
     */
    public void updateChatPartnersList(List<User> users) {
        if (null == chatPartners || null == startChat) {
            findViewsById();
        }

        User self = activity.getSelfUser();
        this.users = users;

        // Exclude self
        String[] userNames = new String[users.size() - 1];

        int i = 0;
        for (int cur = 0; cur < users.size(); cur++) {
            User user = users.get(cur);

            // Skip self
            if (user.getId() == self.getId()) {
                continue;
            }

            userNames[i++] = user.getName();
        }

        adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_multiple_choice, userNames);
        chatPartners.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        chatPartners.setAdapter(adapter);

        startChat.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        SparseBooleanArray checked = chatPartners.getCheckedItemPositions();
        ArrayList<User> selectedUsers = new ArrayList<>();
        ArrayList<String> selectedUserNames = new ArrayList<>();

        if (checked.size() == 0) {
            Toast.makeText(activity, getString(R.string.toast_no_selection), Toast.LENGTH_LONG).show();
            return;
        }

        for (int i = 0; i < checked.size(); i++) {
            // Item position in adapter
            int position = checked.keyAt(i);
            if (checked.valueAt(i)) {
                selectedUsers.add(users.get(position));
                selectedUserNames.add(users.get(position).getName());
            }
        }

        new CreateChatTask(activity.getSelfUser(), selectedUsers).execute();
        return;
    }


    public void startChat(long chatId) {
        Log.d(this.getClass().getSimpleName(), "Start chat: " + chatId);
        Intent intent = new Intent(activity, ChatActivity.class);
        intent.putExtra(activity.USER_MAIL, activity.getUserMail());
        intent.putExtra(activity.USER_ID, activity.getUserId());
        intent.putExtra(activity.CHAT_ID, chatId);
        intent.putExtra(activity.USER_NAME, activity.getSelfUser().getName());
        startActivity(intent);
    }

    @Override
    public void notifyFragment(Long chatId) {
        Log.d(super.getClass().getSimpleName(), "I have been notified. Yeeha!");
        startChat(chatId);
    }

    @Override
    public void onStart() {
        super.onStart();
        //Register at observer
        Log.d(this.getClass().getSimpleName(), "Try to get ChatListObservableInstance");
        FragmentObservable<InviteToChatFragment, Long> obs =
                ObservableRegistry.getObservable(InviteToChatFragment.class);
        Log.d(this.getClass().getSimpleName(), "... successful");
        if (!obs.isRegistered(this)) {
            obs.register(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //De-Register at observer
        FragmentObservable<InviteToChatFragment, Long> obs =
                ObservableRegistry.getObservable(InviteToChatFragment.class);
        Log.d(this.getClass().getSimpleName(), "Remove from observer");
        obs.remove(this);
    }
}