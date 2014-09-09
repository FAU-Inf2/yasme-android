package de.fau.cs.mad.yasme.android.ui.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.asyncTasks.database.GetTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.ChangeChatProperties;
import de.fau.cs.mad.yasme.android.asyncTasks.server.ChangeOwnerAndLeaveTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.LeaveChatTask;
import de.fau.cs.mad.yasme.android.controller.FragmentObservable;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.NotifiableFragment;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.entities.Chat;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.dao.ChatDAO;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;
import de.fau.cs.mad.yasme.android.ui.UserAdapter;
import de.fau.cs.mad.yasme.android.ui.activities.ChatSettingsActivity;

/**
 * Created by Robert Meissner <robert.meissner@studium.fau.de> on 03.08.14.
 */
public class ChatSettingsInfo extends Fragment implements NotifiableFragment<Chat> {
    private List<User> users;
    private UserAdapter mAdapter = null;
    private View chatInfo;
    private Chat chat;
    private Button changeName, changeStatus, leaveChat;

    public ChatSettingsInfo() {
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(this.getClass().getSimpleName(),"onStart");
        //Register at observer
        FragmentObservable<ChatSettingsInfo, Chat> obs = 
            ObservableRegistry.getObservable(ChatSettingsInfo.class);
        obs.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(this.getClass().getSimpleName(),"onStop");
        FragmentObservable<ChatSettingsInfo, Chat> obs = 
            ObservableRegistry.getObservable(ChatSettingsInfo.class);
        obs.remove(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(this.getClass().getSimpleName(),"onResume");
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreate(Bundle savedStateInstance) {
        super.onCreate(savedStateInstance);
        Log.d(this.getClass().getSimpleName(),"onResume");
        users = new ArrayList<User>();
        mAdapter = new UserAdapter(getActivity(), R.layout.user_item, users);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(this.getClass().getSimpleName(),"onCreateView");
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

        changeName = (Button) rootView.findViewById(R.id.change_name);
        changeStatus = (Button) rootView.findViewById(R.id.change_status);
        leaveChat = (Button) rootView.findViewById(R.id.leave_chat);
        chatInfo = rootView.findViewById(R.id.chat_settings_info);

        if (null != chat) {
            fillInfoView();
        }
        users = new ArrayList<User>();
        return rootView;
    }

    @Override
    public void notifyFragment(Chat chat) {
        Log.d(this.getClass().getSimpleName(),"NOTIFICATION");
        if (null == chat) {
            throw new IllegalArgumentException("chat is null in "+this.getClass().getSimpleName());
        }
        this.chat = chat;
        fillInfoView();
    }

    private void fillInfoView() {
        TextView name = (TextView) chatInfo.findViewById(R.id.chat_info_name);
        TextView status = (TextView) chatInfo.findViewById(R.id.chat_info_status);
        TextView number = (TextView) chatInfo.findViewById(R.id.chat_info_number_participants);
        ListView participants = (ListView) chatInfo.findViewById(R.id.chat_info_participants);
        Log.d(this.getClass().getSimpleName(),"Participants: " + participants);
        changeName.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    changeName();
                }
            }
        );
        changeStatus.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    changeStatus();
                }
            }
        );
        leaveChat.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handleLeaveChat(chat);
                }
            }
        );

        name.setText(chat.getName());
        status.setText(chat.getStatus());
        number.setText(" " + chat.getNumberOfParticipants());

        if(mAdapter==null) mAdapter = new UserAdapter(getActivity(), R.layout.user_item, users);
        participants.setAdapter(mAdapter);
        mAdapter.clear();
        mAdapter.addAll(chat.getParticipants());
        mAdapter.notifyDataSetChanged();
    }

    private void changeName() {
        AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(getString(R.string.change_name));

        final EditText chatName = new EditText(activity);
        chatName.setInputType(InputType.TYPE_CLASS_TEXT);
        chatName.setHint(R.string.change_name_hint);

        alert.setView(chatName);

        // "OK" button
        alert.setPositiveButton(R.string.OK,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Grab the EditText's input
                    String newName = chatName.getText().toString();
                    chat.setName(newName,true);
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
        AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(getString(R.string.change_status));

        final EditText chatName = new EditText(activity);
        chatName.setInputType(InputType.TYPE_CLASS_TEXT);
        chatName.setHint(R.string.change_status_hint);

        alert.setView(chatName);

        // "OK" button
        alert.setPositiveButton(R.string.OK,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Grab the EditText's input
                    String inputStatus = chatName.getText().toString();
                    chat.setStatus(inputStatus,true);
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

    private void handleLeaveChat(final Chat chat) {
        boolean isOwner = (chat.getOwner().getId() == DatabaseManager.INSTANCE.getUserId());
        AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
        if (isOwner) {
            AlertDialog.Builder alert = new AlertDialog.Builder(activity);
            alert.setTitle(activity.getString(R.string.alert_owner));

            LinearLayout layout = new LinearLayout(activity);
            layout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            );

            TextView text = new TextView(activity);
            text.setText(activity.getString(R.string.alert_owner_message));

            final ListView list = new ListView(activity);
            list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            List<String> participantNames = new ArrayList<>();
            for (User u : chat.getParticipants()) {
                if (u.getId() == DatabaseManager.INSTANCE.getUserId()) {
                    continue;
                }
                participantNames.add(u.getName());
            }
            final ArrayAdapter<List<User>> adapter = new ArrayAdapter<List<User>>(
                activity,
                android.R.layout.simple_list_item_single_choice,
                (List) participantNames
            );
            list.setAdapter(adapter);

            layout.addView(text, layoutParams);
            layout.addView(list, layoutParams);
            alert.setView(layout);

            alert.setPositiveButton(R.string.change_and_leave_chat,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        int position = list.getCheckedItemPosition();
                        if (position != AdapterView.INVALID_POSITION) {
                            Long newUserId = chat.getParticipants().get(position).getId();
                            new ChangeOwnerAndLeaveTask(chat).execute(newUserId);
                        }
                    }
                }
            );
            alert.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                }
            );
            alert.show();
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(activity);
            alert.setTitle(activity.getString(R.string.alert_leave));
            alert.setMessage(activity.getString(R.string.alert_leave_message));

            alert.setPositiveButton(R.string.leave_chat,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // This can fail with IllegalStateException: the task has already been executed (a task can be executed only once)
                            new LeaveChatTask(chat).execute();
                            dialog.dismiss();
                        }
                    });
            alert.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
            alert.show();
        }
    }
}
