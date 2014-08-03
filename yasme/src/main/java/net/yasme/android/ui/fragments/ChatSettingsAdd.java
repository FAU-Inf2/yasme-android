package net.yasme.android.ui.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.server.ChangeUserTask;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by robert on 03.08.14.
 */
public class ChatSettingsAdd extends InviteToChatFragment {
    private Chat chat;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        chat = (Chat) bundle.getSerializable("chat");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_invite_to_chat, container, false);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        adaptToSettings();
    }

    private void adaptToSettings() {
        startChat.setText(getString(R.string.add_user));
        startChat.setOnClickListener(this);
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
