package net.yasme.android.ui.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.server.ChangeUserTask;
import net.yasme.android.contacts.ContactListContent;
import net.yasme.android.controller.FragmentObservable;
import net.yasme.android.controller.NotifiableFragment;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.User;
import net.yasme.android.ui.AbstractYasmeActivity;
import net.yasme.android.ui.activities.ContactActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by robert on 03.08.14.
 */
public class ChatSettingsA
        extends Fragment
        implements View.OnClickListener, NotifiableFragment<InviteToChatFragment.AllUsersFetchedParam> {

    protected final ContactListContent addParticipantsContent = new ContactListContent();
    private ArrayAdapter<String> adapter;
    private String[] userNamesArr;
    private List<User> users;
    private ListView mContactsList;
    private Chat chat;
    private Button addUser;
    private TextView emptyContactsNotice;

    public ChatSettingsA() {
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


        // Set OnItemClickListener so we can be notified on item clicks
        mContactsList.setOnItemClickListener(
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

        return rootView;
    }

    private void findViewsById() {

        if (null == addUser) {
            Button addUser = (Button) getActivity().findViewById(R.id.add_user);
        }

        if (null == emptyContactsNotice) {
            emptyContactsNotice = (TextView) getActivity().findViewById(R.id.empty_contacts_notice);
            emptyContactsNotice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), ContactActivity.class);
                    // Message is immaterial
                    intent.putExtra(ContactActivity.SEARCH_FOR_CONTACTS, "let me search for contacts");
                    startActivity(intent);
                }
            });
        }

        /*if (null == chatPartners) {
            chatPartners = (ListView) getActivity().findViewById(R.id.inviteToChat_usersList);
            // Only show the notice when the list view is empty
            chatPartners.setEmptyView(emptyContactsNotice);
        }*/
    }

    @Override
    public void onStart() {
        super.onStart();
        FragmentObservable<ChatSettingsA, InviteToChatFragment.AllUsersFetchedParam> obs =
                ObservableRegistry.getObservable(ChatSettingsA.class);
        obs.register(this);
        findViewsById();
    }

    @Override
    public void onStop() {
        FragmentObservable<ChatSettingsA, InviteToChatFragment.AllUsersFetchedParam> obs =
                ObservableRegistry.getObservable(ChatSettingsA.class);
        obs.remove(this);
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
        SparseBooleanArray checked = mContactsList.getCheckedItemPositions();
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
        adapter.notifyDataSetChanged();
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
