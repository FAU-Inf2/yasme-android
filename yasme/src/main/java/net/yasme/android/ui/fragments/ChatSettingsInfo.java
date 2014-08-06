package net.yasme.android.ui.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.server.ChangeChatProperties;
import net.yasme.android.asyncTasks.server.LeaveChatTask;
import net.yasme.android.contacts.ContactListContent;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.User;

/**
 * Created by robert on 03.08.14.
 */
public class ChatSettingsInfo extends Fragment {

    protected final ContactListContent participantsContent = new ContactListContent();
    protected SimpleAdapter mAdapter = null;
    private View chatInfo;
    private Chat chat;

    public ChatSettingsInfo() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        chat = (Chat) bundle.getSerializable("chat");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat_settings_info, container, false);

        Button changeName = (Button) rootView.findViewById(R.id.change_name);
        Button changeStatus = (Button) rootView.findViewById(R.id.change_status);
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
        changeStatus.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(this.getClass().getSimpleName(), "changeStatus-Button pushed");
                        changeStatus();
                    }
                }
        );
        leaveChat.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(this.getClass().getSimpleName(), "leaveChat-Button pushed");
                        new LeaveChatTask(chat, getActivity()).onPreExecute();
                    }
                }
        );
        chatInfo = rootView.findViewById(R.id.chat_settings_info);
        fillInfoView();
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

    public void fillInfoView() {
        TextView name = (TextView) chatInfo.findViewById(R.id.chat_info_name);
        TextView status = (TextView) chatInfo.findViewById(R.id.chat_info_status);
        TextView number = (TextView) chatInfo.findViewById(R.id.chat_info_number_participants);
        ListView participants = (ListView) chatInfo.findViewById(R.id.chat_info_participants);

        name.setText(chat.getName());
        status.setText(chat.getStatus());
        number.setText(" " + chat.getNumberOfParticipants());

        mAdapter = new SimpleAdapter(getActivity(), participantsContent.getMap(),
                android.R.layout.simple_list_item_2, new String[]{"name", "mail"},
                new int[]{android.R.id.text1, android.R.id.text2});
        participants.setAdapter(mAdapter);

        participantsContent.clearItems();

        for (User u : chat.getParticipants()) {
            participantsContent.addItem(new ContactListContent.
                    ContactListItem(String.valueOf(u.getId()), u.getName(), u.getEmail(), u));
        }
        mAdapter.notifyDataSetChanged();
    }
}
