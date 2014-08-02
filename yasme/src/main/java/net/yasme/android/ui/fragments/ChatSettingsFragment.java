package net.yasme.android.ui.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.database.GetContactsTask;
import net.yasme.android.asyncTasks.server.ChangeChatProperties;
import net.yasme.android.asyncTasks.server.ChangeUserTask;
import net.yasme.android.asyncTasks.server.LeaveChatTask;
import net.yasme.android.contacts.ContactListContent;
import net.yasme.android.controller.FragmentObservable;
import net.yasme.android.controller.NotifiableFragment;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.User;
import net.yasme.android.ui.AbstractYasmeActivity;

/**
 * Created by robert on 28.07.14.
 */
public class ChatSettingsFragment
        extends Fragment
        implements NotifiableFragment<InviteToChatFragment.AllUsersFetchedParam>{
    private final ContactListContent addParticipantsContent = new ContactListContent();
    private Chat chat;
    private View chatInfo;
    private View participants;
    private View searchUser;
    private SimpleAdapter mAddAdapter;
    private boolean isOwner = false;

    public ChatSettingsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
        FragmentObservable<ChatSettingsFragment, InviteToChatFragment.AllUsersFetchedParam> obs =
                ObservableRegistry.getObservable(ChatSettingsFragment.class);
        obs.register(this);
    }

    @Override
    public void onStop() {
        FragmentObservable<ChatSettingsFragment, InviteToChatFragment.AllUsersFetchedParam> obs =
                ObservableRegistry.getObservable(ChatSettingsFragment.class);
        obs.remove(this);
        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat_settings, container, false);
        Bundle bundle = getArguments();
        chat = (Chat) bundle.getSerializable("chat");

        Button changeName = (Button) rootView.findViewById(R.id.change_name);
        Button changeStatus = (Button) rootView.findViewById(R.id.change_status);
        Button addUser = (Button) rootView.findViewById(R.id.add_user);
        Button deleteUser = (Button) rootView.findViewById(R.id.delete_user);
        Button leaveChat = (Button) rootView.findViewById(R.id.leave_chat);


        changeName.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(this.getClass().getSimpleName(), "changeName-Button pushed");
                        changeName();
                    }
                }
        );
        addUser.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(this.getClass().getSimpleName(), "addUser-Button pushed");
                        addUser();
                    }
                }
        );
        deleteUser.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(this.getClass().getSimpleName(), "deleteUser-Button pushed");
                        deleteUser();
                    }
                }
        );
        changeStatus.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(this.getClass().getSimpleName(), "changeStatus-Button pushed");
                        changeStatus();
                    }
                }
        );
        leaveChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Log.d(this.getClass().getSimpleName(), "leaveChat-Button pushed");
                    new LeaveChatTask().execute(chat.getId());
                }
        }
        );

        if(chat.getOwner().getId() != getActivity().
                getSharedPreferences(AbstractYasmeActivity.STORAGE_PREFS,
                        AbstractYasmeActivity.MODE_PRIVATE).
                getLong(AbstractYasmeActivity.USER_ID, 0L))
        {
            isOwner = true;
            deleteUser.setVisibility(View.GONE);
        }

        chatInfo = rootView.findViewById(R.id.chat_info);
        chatInfo.setVisibility(View.VISIBLE);
        participants = rootView.findViewById(R.id.participants);
        searchUser = rootView.findViewById(R.id.search_user);

        return rootView;
    }

    private void changeName() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(getString(R.string.change_name));

        final EditText chatName = new EditText(getActivity());
        chatName.setInputType(InputType.TYPE_CLASS_TEXT);
        chatName.setHint(R.string.change_name_hint);

        alert.setView(chatName);

        // "OK" button
        alert.setPositiveButton(R.string.OK,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Grab the EditText's input
                        String inputName = chatName.getText().toString();
                        Chat newChat = chat;
                        newChat.setName(inputName);
                        new ChangeChatProperties(newChat).execute();
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


    private void addUser() {
        chatInfo.setVisibility(View.INVISIBLE);
        participants.setVisibility(View.INVISIBLE);
        searchUser.setVisibility(View.VISIBLE);

        mAddAdapter = new SimpleAdapter(getActivity(), addParticipantsContent.getMap(),
                android.R.layout.simple_list_item_2, new String[]{"name", "mail"},
                new int[]{android.R.id.text1, android.R.id.text2});

        // Set the adapter
        AbsListView list = (AbsListView) searchUser.findViewById(R.id.search_listView);
        list.setAdapter(mAddAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        list.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        User user = addParticipantsContent.items.get(position).user;
                        showAlertDialog(getString(R.string.alert_add_user),
                                user.getName() + getString(R.string.alert_add_user_message),
                                new ChangeUserTask(chat), user.getId(), 1L);
                    }
                }
        );

        new GetContactsTask().execute();
    }


    private void deleteUser() {
        chatInfo.setVisibility(View.INVISIBLE);
        searchUser.setVisibility(View.INVISIBLE);
        participants.setVisibility(View.VISIBLE);

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
                                user.getName() + getString(R.string.alert_delete_user_message),
                                new ChangeUserTask(chat), user.getId(), 0L);
                    }
                }
        );

        for (User u : chat.getParticipants()) {
            participantsContent.addItem(new ContactListContent.
                    ContactListItem(String.valueOf(u.getId()), u.getName(), u.getEmail(), u));
        }
        mDelAdapter.notifyDataSetChanged();
    }


    private void changeStatus() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(getString(R.string.change_status));

        final EditText chatName = new EditText(getActivity());
        chatName.setInputType(InputType.TYPE_CLASS_TEXT);
        chatName.setHint(R.string.change_status_hint);

        alert.setView(chatName);

        // "OK" button
        alert.setPositiveButton(R.string.OK,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Grab the EditText's input
                        String inputStatus = chatName.getText().toString();
                        Chat newChat = chat;
                        newChat.setStatus(inputStatus);
                        new ChangeChatProperties(newChat).execute();
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

    @Override
    public void notifyFragment(InviteToChatFragment.AllUsersFetchedParam value) {
        addParticipantsContent.clearItems();
        for (User u : value.getAllUsers()) {
            if(!chat.getParticipants().contains(u)) {
                addParticipantsContent.addItem(new ContactListContent.
                        ContactListItem(String.valueOf(u.getId()), u.getName(), u.getEmail(), u));
            }
        }
        mAddAdapter.notifyDataSetChanged();
    }

    private void showAlertDialog(String title, String message, final AsyncTask task,
                                 final Long userId, final Long rest) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(title);

        TextView text = new TextView(getActivity());
        text.setText(message);

        alert.setView(text);

        // "OK" button to save the values
        alert.setPositiveButton(R.string.registration_button_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        task.execute(userId, rest);
                    }
                }
        );
        // "Cancel" button
        alert.setNegativeButton(R.string.registration_button_cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                }
        );
        alert.show();
    }
}
