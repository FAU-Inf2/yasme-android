package de.fau.cs.mad.yasme.android.ui.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.asyncTasks.database.GetContactsTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.CreateChatTask;
import de.fau.cs.mad.yasme.android.controller.FragmentObservable;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.NotifiableFragment;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;
import de.fau.cs.mad.yasme.android.ui.UserAdapter;
import de.fau.cs.mad.yasme.android.ui.activities.ChatActivity;
import de.fau.cs.mad.yasme.android.ui.activities.ContactActivity;

/**
 * Created by Benedikt Lorch <benedikt.lorch@studium.fau.de> on 22.06.14.
 */
public class InviteToChatFragment extends Fragment implements View.OnClickListener, NotifiableFragment<InviteToChatFragment.InviteToChatParam> {
    protected AbstractYasmeActivity activity;
    protected List<User> users;
    protected ListView chatPartners;
    protected Button startChat;
    protected UserAdapter adapter;
    protected TextView emptyContactsNotice;

    public InviteToChatFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Register at observer
        Log.d(this.getClass().getSimpleName(), "Try to get ChatListObservableInstance");
        FragmentObservable<InviteToChatFragment, InviteToChatParam> obs =
                ObservableRegistry.getObservable(this.getClass());
        Log.d(this.getClass().getSimpleName(), "... successful");
        obs.register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Make sure that this fragment is registered before invoking the GetContactsTask
        FragmentObservable<InviteToChatFragment, InviteToChatParam> obs =
                ObservableRegistry.getObservable(this.getClass());
        obs.register(this);
        View rootView = inflater.inflate(R.layout.fragment_invite_to_chat, container, false);

        startChat = (Button) rootView.findViewById(R.id.inviteToChat_startChat);

        emptyContactsNotice = (TextView) rootView.findViewById(R.id.empty_contacts_notice);
        emptyContactsNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ContactActivity.class);
                // Message is immaterial
                intent.putExtra(ContactActivity.SEARCH_FOR_CONTACTS, "let me search for contacts");
                startActivity(intent);
            }
        });

        chatPartners = (ListView) rootView.findViewById(R.id.inviteToChat_usersList);
        // Only show the notice when the list view is empty
        chatPartners.setEmptyView(emptyContactsNotice);
        chatPartners.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        activity = (AbstractYasmeActivity) getActivity();
        adapter = new UserAdapter(activity, R.layout.user_item_checkbox, new ArrayList<User>());
		adapter.setNotifyOnChange(true);
        chatPartners.setAdapter(adapter);

        new GetContactsTask(this.getClass()).execute();
        return rootView;
    }

    /**
     * Will be called by the GetAllUsersTask after the list of users has been retrieved
     * @param contacts list
     */
    public void updateChatPartnersList(List<User> contacts) {
        users = contacts;
        Collections.sort(users, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                //Log.d(getClass().getSimpleName(), "Compare");
                User u1 = (User) o1;
                User u2 = (User) o2;
                if (u1.getName() == null && u2.getName() == null) {
                    return 0;
                }
                if (u1.getName() == null) {
                    return -1;
                }
                if (u2.getName() == null) {
                    return 1;
                }
                return u1.getName().toLowerCase().compareTo(u2.getName().toLowerCase());
            }
        });
        adapter.clear();
        adapter.addAll(users);
        adapter.notifyDataSetChanged();
        startChat.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        SparseBooleanArray checked = adapter.getSelectedContacts();
        Set<User> selectedUsers = new HashSet<>();

        if (checked.size() == 0) {
            Toast.makeText(activity, getString(R.string.toast_no_selection), Toast.LENGTH_LONG).show();
            return;
        }

        for (int i = 0; i < checked.size(); i++) {
            if (!checked.valueAt(i)) {
                continue;
            }
            // Item position in adapter
            int position = checked.keyAt(i);
            selectedUsers.add(users.get(position));
            //selectedUserNames.add(users.get(position).getName());
        }
        new CreateChatTask(activity.getSelfUser(), selectedUsers, this.getClass()).execute();
    }


    public void startChat(long chatId) {
        AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
        Intent intent = new Intent(activity, ChatActivity.class);
        intent.putExtra(activity.USER_MAIL, activity.getUserMail());
        intent.putExtra(activity.USER_ID, activity.getUserId());
        intent.putExtra(activity.CHAT_ID, chatId);
        intent.putExtra(activity.USER_NAME, activity.getSelfUser().getName());
        startActivity(intent);
        activity.finish();
    }

    @Override
    public void notifyFragment(InviteToChatParam inviteToChatParam) {
        Log.d(super.getClass().getSimpleName(), "I have been notified. Yeeha!");
        getActivity().setProgressBarIndeterminateVisibility(false); //progress bar off

        if (inviteToChatParam instanceof ChatRegisteredParam) {
            ChatRegisteredParam param = (ChatRegisteredParam) inviteToChatParam;
            startChat(param.getChatId());
        } else if (inviteToChatParam instanceof AllUsersFetchedParam) {
            AllUsersFetchedParam contactsFetchedParam = (AllUsersFetchedParam) inviteToChatParam;
            updateChatPartnersList(contactsFetchedParam.getAllUsers());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //Register at observer
        FragmentObservable<InviteToChatFragment, InviteToChatParam> obs =
                ObservableRegistry.getObservable(this.getClass());
        obs.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        //De-Register at observer
        FragmentObservable<InviteToChatFragment, InviteToChatParam> obs =
                ObservableRegistry.getObservable(this.getClass());
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

    public static class AllUsersFetchedParam extends InviteToChatParam {
        private List<User> allUsers;

        public AllUsersFetchedParam(Boolean success, List<User> allUsers) {
            this.success = success;
            this.allUsers = allUsers;
        }

        public List<User> getAllUsers() {
            return allUsers;
        }
    }
}
