package de.fau.cs.mad.yasme.android.ui.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import de.fau.cs.mad.yasme.android.controller.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.asyncTasks.database.GetTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.ChangeChatProperties;
import de.fau.cs.mad.yasme.android.asyncTasks.server.LeaveChatTask;
import de.fau.cs.mad.yasme.android.contacts.ContactListContent;
import de.fau.cs.mad.yasme.android.controller.FragmentObservable;
import de.fau.cs.mad.yasme.android.controller.NotifiableFragment;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.entities.Chat;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.dao.ChatDAO;
import de.fau.cs.mad.yasme.android.ui.activities.ChatSettingsActivity;

/**
 * Created by robert on 03.08.14.
 */
public class ChatSettingsInfo extends Fragment implements NotifiableFragment<Chat> {

    protected final ContactListContent participantsContent = new ContactListContent();
    protected SimpleAdapter mAdapter = null;
    private View chatInfo;
    private Chat chat;

    public ChatSettingsInfo() {}

    @Override
    public void onResume() {
        super.onResume();
        if(mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null == chat) {
            Bundle bundle = getArguments();
            long chatId = bundle.getLong(ChatSettingsActivity.CHAT_ID);
            // Make sure that fragment is registered. Registering twice won't cause any issues
            FragmentObservable<ChatSettingsInfo, Chat> obs = ObservableRegistry.getObservable(ChatSettingsInfo.class);
            obs.register(this);

            // load chat from database
            if (chatId <= 0) {
                throw new IllegalArgumentException("chatId <= 0");
            }



            ChatDAO chatDAO = DatabaseManager.INSTANCE.getChatDAO();
            new GetTask<>(chatDAO, chatId, this.getClass()).execute();
        }

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
                        //LeaveChatTask task = new LeaveChatTask(chat);
                        //LeaveChatTask.preExecute(getActivity(), task);

                        Context mContext = DatabaseManager.INSTANCE.getContext();
                        AlertDialog alert = new AlertDialog.Builder(mContext).create();
                        alert.setTitle(mContext.getString(R.string.alert_leave));
                        alert.setMessage(mContext.getString(R.string.alert_leave_message));

                        alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        // This can fail with IllegalStateException: the task has already been executed (a task can be executed only once)
                                        new LeaveChatTask(chat).execute();
                                        dialog.dismiss();
                                    }
                                });

                        alert.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                        alert.show();
                    }
                }
        );
        chatInfo = rootView.findViewById(R.id.chat_settings_info);
        if (null != chat){
            fillInfoView();
        }
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        //Register at observer
        FragmentObservable<ChatSettingsInfo, Chat> obs = ObservableRegistry.getObservable(ChatSettingsInfo.class);
        obs.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        FragmentObservable<ChatSettingsInfo, Chat> obs = ObservableRegistry.getObservable(ChatSettingsInfo.class);
        obs.remove(this);
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
                        String newName = chatName.getText().toString();
                        chat.setName(newName);
                        new ChangeChatProperties(chat, ChatSettingsInfo.class).execute();
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
                        chat.setStatus(inputStatus);
                        new ChangeChatProperties(chat, ChatSettingsInfo.class).execute();
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

    @Override
    public void notifyFragment(Chat chat) {
        if (null == chat) {
            Log.e(this.getClass().getSimpleName(), " was waiting for a chat object but turned out it was null");
        } else {
            this.chat = chat;
            fillInfoView();
        }
    }
}
