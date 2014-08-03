package net.yasme.android.ui.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.database.GetContactsTask;
import net.yasme.android.asyncTasks.server.ChangeUserTask;
import net.yasme.android.contacts.ContactListContent;
import net.yasme.android.controller.FragmentObservable;
import net.yasme.android.controller.NotifiableFragment;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.User;

/**
 * Created by robert on 03.08.14.
 */
public class ChatSettingsAdd
        extends Fragment
        implements NotifiableFragment<InviteToChatFragment.AllUsersFetchedParam> {

    protected final ContactListContent addParticipantsContent = new ContactListContent();
    private SimpleAdapter mAddAdapter;
    private View contacts;
    private Chat chat;

    public ChatSettingsAdd() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        chat = (Chat) bundle.getSerializable("chat");
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
                ObservableRegistry.getObservable(ChatSettingsAdd.class);
        obs.register(this);
    }

    @Override
    public void onStop() {
        FragmentObservable<ChatSettingsAdd, InviteToChatFragment.AllUsersFetchedParam> obs =
                ObservableRegistry.getObservable(ChatSettingsAdd.class);
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

    public void showAlertDialog(String title, String message, final Chat chat,
                                final Long userId, final Long rest) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(title);

        TextView text = new TextView(getActivity());
        text.setText(message);

        alert.setView(text);

        // "OK" button to save the values
        alert.setPositiveButton(R.string.OK,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        new ChangeUserTask(chat).execute(userId, rest);
                    }
                }
        );
        // "Cancel" button
        alert.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                }
        );
        alert.show();
    }
}
