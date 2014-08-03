package net.yasme.android.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SimpleAdapter;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.database.GetContactsTask;
import net.yasme.android.contacts.ContactListContent;
import net.yasme.android.controller.FragmentObservable;
import net.yasme.android.controller.NotifiableFragment;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.entities.User;

/**
 * Created by robert on 03.08.14.
 */
public class ChatSettingsAdd
        extends ChatSettingsFragment

        implements NotifiableFragment<InviteToChatFragment.AllUsersFetchedParam> {

    private SimpleAdapter mAddAdapter;

    private View contacts;

    public ChatSettingsAdd() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat_settings_add, container, false);

        Button addUser = (Button) rootView.findViewById(R.id.add_user);

        addUser.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(this.getClass().getSimpleName(), "addUser-Button pushed");
                        addUser();
                    }
                }
        );
        contacts = rootView.findViewById(R.id.chat_settings_contacts);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FragmentObservable<ChatSettingsAdd, InviteToChatFragment.AllUsersFetchedParam> obs =
                ObservableRegistry.getObservable(ChatSettingsFragment.class);
        obs.register(this);
    }

    @Override
    public void onStop() {
        FragmentObservable<ChatSettingsAdd, InviteToChatFragment.AllUsersFetchedParam> obs =
                ObservableRegistry.getObservable(ChatSettingsFragment.class);
        obs.remove(this);
        super.onStop();
    }

    private void addUser() {
        mAddAdapter = new SimpleAdapter(getActivity(), addParticipantsContent.getMap(),
                android.R.layout.simple_list_item_2, new String[]{"name", "mail"},
                new int[]{android.R.id.text1, android.R.id.text2});

        // Set the adapter
        AbsListView list = (AbsListView) contacts.findViewById(R.id.search_listView);
        list.setAdapter(mAddAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        list.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        User user = addParticipantsContent.items.get(position).user;
                        showAlertDialog(getString(R.string.alert_add_user),
                                user.getName() + " " + getString(R.string.alert_add_user_message),
                                chat, user.getId(), 1L);
                    }
                }
        );

        new GetContactsTask().execute();
    }

    @Override
    public void notifyFragment(InviteToChatFragment.AllUsersFetchedParam value) {
        addParticipantsContent.clearItems();
        for (User u : value.getAllUsers()) {
            boolean isParticipant = false;
            for(User p : chat.getParticipants()) {
                if(u.getId() == p.getId()) {
                    isParticipant = true;
                }
            }
            if(!isParticipant) {
                addParticipantsContent.addItem(new ContactListContent.
                        ContactListItem(String.valueOf(u.getId()), u.getName(), u.getEmail(), u));
            }
        }
        mAddAdapter.notifyDataSetChanged();
    }
}
