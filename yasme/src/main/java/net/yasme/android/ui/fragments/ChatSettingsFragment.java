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
import android.widget.EditText;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.server.ChangeChatStatusTask;
import net.yasme.android.entities.Chat;

/**
 * Created by robert on 28.07.14.
 */
public class ChatSettingsFragment extends Fragment{
    private Chat chat;

    public ChatSettingsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat_settings, container, false);

        rootView.findViewById(R.id.change_name).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(this.getClass().getSimpleName(), "changeName-Button pushed");
                        changeName();
                    }
                }
        );
        rootView.findViewById(R.id.change_user).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(this.getClass().getSimpleName(), "changeUser-Button pushed");
                        changeUser();
                    }
                }
        );
        rootView.findViewById(R.id.change_status).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(this.getClass().getSimpleName(), "changeStatus-Button pushed");
                        changeStatus();
                    }
                }
        );

        return rootView;
    }

    private void changeName() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(getString(R.string.change_name));

        final EditText chatName = new EditText(getActivity());
        chatName.setInputType(InputType.TYPE_CLASS_TEXT);
        chatName.setHint(R.string.change_name_hint);

        alert.setView(chatName);

        // "OK" button to save the values
        alert.setPositiveButton(R.string.registration_button_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Grab the EditText's input
                        String inputName = chatName.getText().toString();
                        Chat newChat = chat;
                        newChat.setName(inputName);
                        new ChangeChatStatusTask(newChat).execute();
                    }
                }
        );

        // "Cancel" button
        alert.setNegativeButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }
        );
        alert.show();
    }


    private void changeUser() {

    }


    private void changeStatus() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(getString(R.string.change_status));

        final EditText chatName = new EditText(getActivity());
        chatName.setInputType(InputType.TYPE_CLASS_TEXT);
        chatName.setHint(R.string.change_status_hint);

        alert.setView(chatName);

        // "OK" button to save the values
        alert.setPositiveButton(R.string.registration_button_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Grab the EditText's input
                        String inputStatus = chatName.getText().toString();
                        Chat newChat = chat;
                        newChat.setStatus(inputStatus);
                        new ChangeChatStatusTask(newChat).execute();
                    }
                }
        );

        // "Cancel" button
        alert.setNegativeButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }
        );
        alert.show();
    }
}
