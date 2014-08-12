package de.fau.cs.mad.yasme.android.ui.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.asyncTasks.database.GetContactsTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.ChangeUserTask;
import de.fau.cs.mad.yasme.android.controller.FragmentObservable;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.entities.Chat;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.dao.ChatDAO;
import de.fau.cs.mad.yasme.android.ui.activities.ChatSettingsActivity;

/**
 * Created by robert on 03.08.14.
 */
public class ChatSettingsAdd extends InviteToChatFragment {
    private Chat chat;

    @Override
    public void onResume() {
        super.onResume();
        if(adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(chat == null) {
            Bundle bundle = getArguments();
            long chatId = bundle.getLong(ChatSettingsActivity.CHAT_ID);
            // load chat from database
            if (chatId <= 0) {
                throw new IllegalArgumentException("chatId <= 0");
            }

            // TODO Use AsyncTask. Will be complicated as this class extends InviteToChatFragment and thus implements notifiable not with a chat object as parameter
            // maybe we can only copy the code from InviteToChatFragment?
            ChatDAO chatDAO = DatabaseManager.INSTANCE.getChatDAO();
            chat = chatDAO.get(chatId);
            if (null == chat) {
                Log.e(this.getClass().getSimpleName(), "chat could not be loaded from database");
                throw new ExceptionInInitializerError("Chat could not be loaded from database");
            }
        }
        new GetContactsTask(this.getClass()).execute();

        View rootView = inflater.inflate(R.layout.fragment_invite_to_chat, container, false);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        //Register at observer
        Log.d(this.getClass().getSimpleName(), "Try to get ChatListObservableInstance");
        FragmentObservable<ChatSettingsAdd, InviteToChatParam> obs =
                ObservableRegistry.getObservable(ChatSettingsAdd.class);
        Log.d(this.getClass().getSimpleName(), "... successful");
        obs.register(this);

        findViewsById();
        //startChat.setVisibility(View.INVISIBLE);    // button

        adaptToSettingsFragment();
    }

    @Override
    public void onStop() {
        super.onStop();
        //De-Register at observer
        FragmentObservable<ChatSettingsAdd, InviteToChatParam> obs =
                ObservableRegistry.getObservable(ChatSettingsAdd.class);
        Log.d(this.getClass().getSimpleName(), "Remove from observer");
        obs.remove(this);
    }

    private void adaptToSettingsFragment() {
        startChat.setText(getString(R.string.add_user));
        startChat.setOnClickListener(this);
        emptyContactsNotice.setText(getString(R.string.contact_list_empty));
    }

    @Override
    public void onClick(View view) {
        Log.d(this.getClass().getSimpleName(), "addUser-Button pushed");
        SparseBooleanArray checked = chatPartners.getCheckedItemPositions();

        if (checked.size() == 0) {
            Toast.makeText(getActivity(), getString(R.string.toast_no_selection),
                    Toast.LENGTH_LONG).show();
            return;
        }

        for (int i = 0; i < checked.size(); i++) {
            // Item position in adapter
            if (checked.valueAt(i)) {
                showAlertDialog(getString(R.string.alert_add_user),
                        users.get(i).getName() + " " + getString(R.string.alert_add_user_message),
                        chat, users.get(i).getId(), 1L);
            }
        }
    }

    public void updateChatPartnersList(List<User> allUsers) {
        if(chat == null) {
            //TODO: Chat noch nich aus DB geladen - evtl.warten??
        }
        List<User> filteredUsers = new ArrayList<>();
        for (User u : allUsers) {
            boolean isParticipant = false;
            for(User p : chat.getParticipants()) {
                if(u.getId() == p.getId()) {
                    isParticipant = true;
                }
            }
            if(!isParticipant) {
                filteredUsers.add(u);
            }
        }
        super.updateChatPartnersList(filteredUsers);
    }

    public void showAlertDialog(String title, String message, final Chat chat,
                                final Long userId, final Long rest) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(title);

        TextView text = new TextView(getActivity());
        text.setText(message);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        LinearLayout layout = new LinearLayout(getActivity());
        layout.addView(text, params);
        alert.setView(layout);

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

    @Override
    public void notifyFragment(InviteToChatParam inviteToChatParam) {
        //startChat.setVisibility(View.VISIBLE);
        if(inviteToChatParam instanceof AllUsersFetchedParam) {
            updateChatPartnersList(((AllUsersFetchedParam) inviteToChatParam).getAllUsers());
            return;
        }
        if(inviteToChatParam instanceof GetChatParam) {
            chat = ((GetChatParam) inviteToChatParam).getChat();
            return;
        }
        super.notifyFragment(inviteToChatParam);
    }

    public static class GetChatParam extends InviteToChatParam {
        private Chat chat;

        public GetChatParam(Chat chat) {
            this.chat = chat;
        }

        public Chat getChat() {
            return chat;
        }
    }
}
