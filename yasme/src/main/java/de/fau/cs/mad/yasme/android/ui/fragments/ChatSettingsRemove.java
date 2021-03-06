package de.fau.cs.mad.yasme.android.ui.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.asyncTasks.database.GetTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.ChangeUserTask;
import de.fau.cs.mad.yasme.android.controller.FragmentObservable;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.NotifiableFragment;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.Toaster;
import de.fau.cs.mad.yasme.android.entities.Chat;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.dao.ChatDAO;
import de.fau.cs.mad.yasme.android.storage.dao.UserDAO;
import de.fau.cs.mad.yasme.android.ui.UserAdapter;
import de.fau.cs.mad.yasme.android.ui.activities.ChatSettingsActivity;

/**
 * Created by Robert Meissner <robert.meissner@studium.fau.de> on 03.08.14.
 */
public class ChatSettingsRemove extends Fragment implements NotifiableFragment<Chat> {
    private List<User> users;
    private UserAdapter mDelAdapter = null;
    private View chatRem;
    private Chat chat;

    public ChatSettingsRemove() {
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(this.getClass().getSimpleName(), "onStart");
        FragmentObservable<ChatSettingsRemove, Chat> obs =
                ObservableRegistry.getObservable(ChatSettingsRemove.class);
        obs.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(this.getClass().getSimpleName(), "onStop");
        FragmentObservable<ChatSettingsRemove, Chat> obs =
                ObservableRegistry.getObservable(ChatSettingsRemove.class);
        obs.remove(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(this.getClass().getSimpleName(), "onResume");
        if (mDelAdapter != null) {
            mDelAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(this.getClass().getSimpleName(), "onCreate");
        FragmentObservable<ChatSettingsRemove, Chat> obs =
                ObservableRegistry.getObservable(ChatSettingsRemove.class);
        obs.register(this);
        users = new ArrayList<User>();
        mDelAdapter = new UserAdapter(getActivity(), R.layout.user_item, users);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(this.getClass().getSimpleName(), "onCreateView");
        if (null == chat) {
            Bundle bundle = getArguments();
            long chatId = bundle.getLong(ChatSettingsActivity.CHAT_ID);
            // Make sure that fragment is registered. Registering twice won't cause any issues
            FragmentObservable<ChatSettingsRemove, Chat> obs =
                    ObservableRegistry.getObservable(ChatSettingsRemove.class);
            obs.register(this);

            // load chat from database
            if (chatId <= 0) {
                throw new IllegalArgumentException("chatId <= 0");
            }
            ChatDAO chatDAO = DatabaseManager.INSTANCE.getChatDAO();
            new GetTask<>(chatDAO, chatId, this.getClass()).execute();
        }
        View rootView = inflater.inflate(R.layout.fragment_chat_settings_remove, container, false);

        chatRem = rootView.findViewById(R.id.chat_settings_remove);

        if (null != chat) {
            fillRemView();
        }
        return rootView;
    }

    @Override
    public void notifyFragment(Chat chat) {
        Log.d(this.getClass().getSimpleName(), "NOTIFICATION");
        if (null == chat) {
            throw new IllegalArgumentException("chat is null in " + this.getClass().getSimpleName());
        }
        this.chat = chat;
        fillRemView();
    }

    private void fillRemView() {
        ListView participants = (ListView) chatRem.findViewById(R.id.chat_rem_participants);
        Log.d(this.getClass().getSimpleName(), "Participants: " + participants);
        users.clear();
        for (User u : chat.getParticipants()) {
            Log.d(this.getClass().getSimpleName(), "User: " + u.getName());
            if (u.getId() == DatabaseManager.INSTANCE.getUserId()) {
                continue;
            }
            users.add(u);
        }
        // Set the adapter
        participants.setAdapter(mDelAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        participants.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        User user = users.get(position);
                        Log.d(this.getClass().getSimpleName(),
                                " Owner: " + chat.getOwner().getId() +
                                        " OwnId: " + DatabaseManager.INSTANCE.getUserId()
                        );
                        if (!chat.isOwner(DatabaseManager.INSTANCE.getUserId())) {
                            Toaster.getInstance()
                                    .toast(R.string.alert_not_owner, Toast.LENGTH_LONG);
                            return;
                        }
                        showAlertDialog(
                                getString(R.string.alert_delete_user),
                                user.getName() + " " + getString(R.string.alert_delete_user_message),
                                user.getId(), 0L, position
                        );
                    }
                }
        );
    }

    private void showAlertDialog(String title, String message, final Long userId,
                                 final Long rest, final int pos) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(title);
        alert.setMessage(message);
        // "OK" button to save the values
        alert.setPositiveButton(R.string.OK,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        users.remove(pos);
                        mDelAdapter.clear();
                        mDelAdapter.addAll(users);
                        mDelAdapter.notifyDataSetChanged();
                        UserDAO uDAO = DatabaseManager.INSTANCE.getUserDAO();
                        chat.removeParticipant(uDAO.get(userId));
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
