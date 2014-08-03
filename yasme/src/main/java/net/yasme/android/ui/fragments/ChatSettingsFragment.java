package net.yasme.android.ui.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.server.ChangeUserTask;
import net.yasme.android.contacts.ContactListContent;
import net.yasme.android.entities.Chat;
import net.yasme.android.ui.AbstractYasmeActivity;

/**
 * Created by robert on 28.07.14.
 */
public class ChatSettingsFragment extends Fragment {
    protected final ContactListContent addParticipantsContent = new ContactListContent();
    protected Chat chat;
    protected boolean isOwner = false;

    public ChatSettingsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStart() {
        Bundle bundle = getArguments();
        chat = (Chat) bundle.getSerializable("chat");

        if(chat.getOwner().getId() != getActivity().
                getSharedPreferences(AbstractYasmeActivity.STORAGE_PREFS,
                        AbstractYasmeActivity.MODE_PRIVATE).
                            getLong(AbstractYasmeActivity.USER_ID, 0L)) {
            isOwner = true;
        }
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_chat_settings, container, false);

        return rootView;
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
