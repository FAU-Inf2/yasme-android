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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by bene on 22.06.14.
 */
public class InviteToChatFragment
        extends Fragment
        implements View.OnClickListener, NotifiableFragment<InviteToChatFragment.InviteToChatParam> {

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
        FragmentObservable<InviteToChatFragment, InviteToChatParam> obs =
                ObservableRegistry.getObservable(InviteToChatFragment.class);
        Log.d(this.getClass().getSimpleName(), "... successful");
        obs.register(this);


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
     * @param allUsers list
     */
    public void updateChatPartnersList(List<User> allUsers) {
        if (null == chatPartners || null == startChat) {
            findViewsById();
        }

        User self = activity.getSelfUser();

        // Exclude self
        User myself = null;
        String[] userNames = new String[allUsers.size() - 1];

        int i = 0;
        for (int cur = 0; cur < allUsers.size(); cur++) {
            User user = allUsers.get(cur);

            // Skip self
            if (user.getId() == self.getId()) {
                myself = user;
                continue;
            }

            userNames[i++] = user.getName();
        }
        allUsers.remove(myself);
        this.users = allUsers;  // without myself

        adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_multiple_choice, userNames);
        chatPartners.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        chatPartners.setAdapter(adapter);

        startChat.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        SparseBooleanArray checked = chatPartners.getCheckedItemPositions();
        Set<User> selectedUsers = new HashSet<>();
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

        // Add self to participants list. Since we're working on a set, it doesn't matter in case the set already contains self
        selectedUsers.add(activity.getSelfUser());
        new CreateChatTask(activity.getSelfUser(), selectedUsers).execute();
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
    public void notifyFragment(InviteToChatParam inviteToChatParam) {
        Log.d(super.getClass().getSimpleName(), "I have been notified. Yeeha!");
        if (inviteToChatParam instanceof ChatRegisteredParam) {
            ChatRegisteredParam param = (ChatRegisteredParam) inviteToChatParam;
            startChat(param.getChatId());
        } else if (inviteToChatParam instanceof ContactsFetchedParam) {
            ContactsFetchedParam contactsFetchedParam = (ContactsFetchedParam) inviteToChatParam;
            updateChatPartnersList(contactsFetchedParam.getContacts());
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        //Register at observer
        Log.d(this.getClass().getSimpleName(), "Try to get ChatListObservableInstance");
        FragmentObservable<InviteToChatFragment, InviteToChatParam> obs =
                ObservableRegistry.getObservable(InviteToChatFragment.class);
        Log.d(this.getClass().getSimpleName(), "... successful");
        obs.register(this);

    }

    @Override
    public void onStop() {
        super.onStop();
        //De-Register at observer
        FragmentObservable<InviteToChatFragment, InviteToChatParam> obs =
                ObservableRegistry.getObservable(InviteToChatFragment.class);
        Log.d(this.getClass().getSimpleName(), "Remove from observer");
        obs.remove(this);
    }



    public static abstract class InviteToChatParam {
        protected Boolean success;

        public Boolean getSuccess() {
            return success;
        }
    }

    public static class ChatRegisteredParam extends InviteToChatParam {
        private Long chatId;

        public ChatRegisteredParam(Boolean success, Long chatId) {
            this.success = success;
            this.chatId = chatId;
        }

        public Long getChatId() {
            return this.chatId;
        }
    }

    public static class ContactsFetchedParam extends InviteToChatParam {
        private List<User> contacts;

        public ContactsFetchedParam(Boolean success, List<User> contacts) {
            this.success = success;
            this.contacts = contacts;
        }

        public List<User> getContacts() {
            return contacts;
        }
    }
}
