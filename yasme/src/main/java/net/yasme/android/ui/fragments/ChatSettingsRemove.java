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
import net.yasme.android.contacts.ContactListContent;
import net.yasme.android.entities.User;

/**
 * Created by robert on 03.08.14.
 */
public class ChatSettingsRemove extends ChatSettingsFragment {

    ChatSettingsFragment csf = new ChatSettingsFragment();
    private View participants;

    public ChatSettingsRemove() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat_settings_remove, container, false);

        Button deleteUser = (Button) rootView.findViewById(R.id.delete_user);

        deleteUser.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(this.getClass().getSimpleName(), "deleteUser-Button pushed");
                        deleteUser();
                    }
                }
        );

        participants = rootView.findViewById(R.id.chat_settings_participants);
        return rootView;
    }

    private void deleteUser() {
        final SimpleAdapter mDelAdapter;
        final ContactListContent participantsContent = new ContactListContent();
        mDelAdapter = new SimpleAdapter(getActivity(), participantsContent.getMap(),
                android.R.layout.simple_list_item_2, new String[]{"name", "mail"},
                new int[]{android.R.id.text1, android.R.id.text2});

        // Set the adapter
        AbsListView list = (AbsListView) participants.findViewById(android.R.id.list);
        list.setAdapter(mDelAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        list.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        User user = participantsContent.items.get(position).user;
                        showAlertDialog(getString(R.string.alert_delete_user),
                                user.getName() + " " + getString(R.string.alert_delete_user_message),
                                chat, user.getId(), 0L);
                    }
                }
        );

        for (User u : chat.getParticipants()) {
            participantsContent.addItem(new ContactListContent.
                    ContactListItem(String.valueOf(u.getId()), u.getName(), u.getEmail(), u));
        }
        mDelAdapter.notifyDataSetChanged();
    }
}
