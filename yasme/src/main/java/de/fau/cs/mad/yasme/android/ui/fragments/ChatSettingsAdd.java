package de.fau.cs.mad.yasme.android.ui.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.asyncTasks.server.ChangeUserTask;
import de.fau.cs.mad.yasme.android.controller.FragmentObservable;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.entities.Chat;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.dao.ChatDAO;
import de.fau.cs.mad.yasme.android.ui.activities.ChatSettingsActivity;

/**
 * Created by Robert Meissner <robert.meissner@studium.fau.de> on 03.08.14.
 * Modified by Tim Nisslbeck <hu78sapy@stud.cs.fau.de>
 */
public class ChatSettingsAdd extends InviteToChatFragment {
    private Chat chat;
    private int index = -1;

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (chat == null) {
            Bundle bundle = getArguments();
            long chatId = bundle.getLong(ChatSettingsActivity.CHAT_ID);
            // load chat from database
            if (chatId <= 0) {
                throw new IllegalArgumentException("chatId <= 0");
            }

            // maybe we can only copy the code from InviteToChatFragment?
            ChatDAO chatDAO = DatabaseManager.INSTANCE.getChatDAO();
            chat = chatDAO.get(chatId);
            if (null == chat) {
                throw new ExceptionInInitializerError("Chat could not be loaded from database");
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        //Register at observer
        FragmentObservable<ChatSettingsAdd, InviteToChatParam> obs =
                ObservableRegistry.getObservable(this.getClass());
        obs.register(this);
        adaptToSettingsFragment();
    }

    @Override
    public void onStop() {
        super.onStop();
        //De-Register at observer
        FragmentObservable<ChatSettingsAdd, InviteToChatParam> obs =
                ObservableRegistry.getObservable(this.getClass());
        obs.remove(this);
    }

    private void adaptToSettingsFragment() {
        startChat.setText(getString(R.string.add_user));
        startChat.setOnClickListener(this);
        emptyContactsNotice.setText(getString(R.string.contact_list_empty));
    }

    @Override
    public void onClick(View view) {
        List<User> usersToAdd = new ArrayList<>();
        SparseBooleanArray checked = adapter.getSelectedContacts();
        Log.d(this.getClass().getSimpleName(), checked.size() + "");

        if (checked.size() == 0) {
            Toast.makeText(getActivity(), getString(R.string.toast_no_selection),
                    Toast.LENGTH_LONG).show();
            return;
        }
        for (int i = 0; i < checked.size(); i++) {
            // Get the item position (index) in adapter and show an alert dialog box
            index = checked.keyAt(i);

            // If the box is not set/checked (true), just skip
            if (!checked.valueAt(i)) {
                continue;
            }
            usersToAdd.add(users.get(index));

            Log.d(this.getClass().getSimpleName(),
                i + " " + index + " " + checked.valueAt(i) + " " + users.get(index).getName() );
        }
        String messageBody = "";
        for (User u : usersToAdd) {
            messageBody += "- " + u.getName() + "\n";
        }
        showAlertDialog(
                getString(R.string.alert_add_user),
                getString(R.string.alert_add_user_message) + "\n" + messageBody,
                //users.get(index).getName() + " " + getString(R.string.alert_add_user_message),
                usersToAdd, 1L);
    }

    public void updateChatPartnersList(List<User> contacts) {
        if (chat == null) {
            throw new IllegalArgumentException("Chat in updateChatPartnersList was null");
        }
        List<User> filteredUsers = new ArrayList<>();
        for (User u : contacts) {
            boolean isParticipant = false;
            for (User p : chat.getParticipants()) {
                if (u.getId() == p.getId()) {
                    isParticipant = true;
                    break;
                }
            }
            if (!isParticipant) {
                filteredUsers.add(u);
            }
        }
        super.updateChatPartnersList(filteredUsers);
        startChat.setOnClickListener(this);
    }

    private void showAlertDialog
            (String title, String message, final List<User> usersToAdd, final Long rest) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setPositiveButton(
                R.string.OK,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        for (User user : usersToAdd) {
                            chat.addParticipant(user);
                            //TODO wieder einkommentieren
                            /*Log.d(this.getClass().getSimpleName(),
                                "Status Name : [" + chat.getStatusChanged() +
                                "] [" + chat.getNameChanged() + "]");
                            if (!chat.getStatusChanged()) {
                                chat.setStatus(chat.getStatus(), false);
                            }
                            if (!chat.getNameChanged()) {
                                chat.setName(chat.getName(), false);
                            }*/
                            new ChangeUserTask(chat).execute(user.getId(), rest);
                        }
                        //users.addAll(remainingUsers);
                        users.removeAll(usersToAdd);
                        updateChatPartnersList(users);
                    }
                }
        );
        alert.setNegativeButton(
                R.string.cancel,
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
        if (inviteToChatParam instanceof AllUsersFetchedParam) {
            Log.d(this.getClass().getSimpleName(),"notify1");
            updateChatPartnersList(((AllUsersFetchedParam) inviteToChatParam).getAllUsers());
            return;
        }
        if (inviteToChatParam instanceof GetChatParam) {
            Log.d(this.getClass().getSimpleName(),"notify2");
            super.notifyFragment(inviteToChatParam);
            return;
        }
        Log.d(this.getClass().getSimpleName(),"notify3");
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
