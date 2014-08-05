package net.yasme.android.ui.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.server.ChangeUserTask;
import net.yasme.android.contacts.ContactListContent;
import net.yasme.android.controller.Toaster;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.User;
import net.yasme.android.storage.DatabaseManager;

/**
 * Created by robert on 03.08.14.
 */
public class ChatSettingsRemove extends Fragment {

    final ContactListContent participantsContent = new ContactListContent();
    private View participants;
    private AbsListView list;
    private Chat chat;

    public ChatSettingsRemove() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        chat = (Chat) bundle.getSerializable("chat");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat_settings_remove, container, false);

        participants = rootView.findViewById(R.id.chat_settings_participants);

        final SimpleAdapter mDelAdapter;
        mDelAdapter = new SimpleAdapter(getActivity(), participantsContent.getMap(),
                android.R.layout.simple_list_item_2, new String[]{"name", "mail"},
                new int[]{android.R.id.text1, android.R.id.text2});

        // Set the adapter
        list = (AbsListView) participants.findViewById(android.R.id.list);
        list.setAdapter(mDelAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        list.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        User user = participantsContent.items.get(position).user;
                        if(chat.getOwner().getId() == DatabaseManager.INSTANCE.getUserId()) {
                            showAlertDialog(getString(R.string.alert_delete_user),
                                    user.getName() + " " + getString(R.string.alert_delete_user_message),
                                    chat, user.getId(), 0L);
                        } else {
                            Toaster.getInstance().toast(R.string.alert_not_owner, Toast.LENGTH_LONG);
                        }
                    }
                }
        );

        participantsContent.clearItems();

        for (User u : chat.getParticipants()) {
            if(u.getId() == DatabaseManager.INSTANCE.getUserId()) {
                continue;
            }
            participantsContent.addItem(new ContactListContent.
                    ContactListItem(String.valueOf(u.getId()), u.getName(), u.getEmail(), u));
        }
        mDelAdapter.notifyDataSetChanged();

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
