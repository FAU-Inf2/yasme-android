package de.fau.cs.mad.yasme.android.ui.fragments;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.asyncTasks.database.GetAllTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.ChangeOwnerAndLeaveTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.GetMessageTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.GetMyChatsTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.GetProfileDataTask;
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
import de.fau.cs.mad.yasme.android.ui.ChatListAdapter;
import de.fau.cs.mad.yasme.android.ui.activities.ChatActivity;
import de.fau.cs.mad.yasme.android.ui.activities.ChatSettingsActivity;
import de.fau.cs.mad.yasme.android.ui.activities.ContactActivity;

/**
 * Created by Martin Sturm <martin.sturm@fau.de> on 21.06.2014.
 */
public class ChatListFragment extends ListFragment implements NotifiableFragment<List<Chat>> {
    private List<Chat> chatRooms;
    private ChatListAdapter adapter;

    public ChatListFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
        chatRooms = new ArrayList<Chat>();
        adapter = new ChatListAdapter(activity, R.layout.chatlist_item, chatRooms);
        this.setListAdapter(adapter);
        User self = activity.getSelfUser();

        String name = null;
        try {
            name = self.getName();
        } catch (NullPointerException npe) {
            Log.e(this.getClass().getSimpleName(), "Error: " + npe.getMessage());
        }
        if (name != null) {
            activity.setTitle(activity.getSelfUser().getName());
        }

        //holt vor allem den Namen des Users ab
        new GetProfileDataTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chatlist, container, false);
        rootView.findViewById(android.R.id.empty).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ContactActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        //Register at observer
        Log.d(this.getClass().getSimpleName(), "Try to get ChatListObservableInstance");
        FragmentObservable<ChatListFragment, List<Chat>> obs = ObservableRegistry.getObservable(ChatListFragment.class);
        Log.d(this.getClass().getSimpleName(), "... successful");
        obs.register(this);

        //register for context menu
        registerForContextMenu(this.getListView());
    }

    @Override
    public void onResume() {
        super.onResume();

        // At first, retrieve the chats from the database
        ChatDAO chatDAO = DatabaseManager.INSTANCE.getChatDAO();
        new GetAllTask(chatDAO, this.getClass()).execute();

        // Dann beim Server nachfragen, ob es neue gibt, und in der Datenbank abspeichern
        // Aktualisiert die Datenbank auf den aktuellen Stand des Servers
        new GetMyChatsTask(this.getClass()).execute();
        new GetMessageTask(this.getClass()).execute();
    }

    @Override
    public void onStop() {
        super.onStop();
        //De-Register at observer
        FragmentObservable<ChatListFragment, List<Chat>> obs = ObservableRegistry.getObservable(ChatListFragment.class);
        Log.d(this.getClass().getSimpleName(), "Remove from observer");
        obs.remove(this);
    }

    @Override
    public void onListItemClick(ListView l, View view, int pos, long id) {
        Chat chat = (Chat) getListAdapter().getItem(pos);
        showChat(chat.getId());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = this.getActivity().getMenuInflater();
        inflater.inflate(R.menu.chatlist_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final Chat chat = (Chat) getListAdapter().getItem(info.position);
        switch (item.getItemId()) {
            case R.id.context_settings:
                Intent intent = new Intent(getActivity(), ChatSettingsActivity.class);
                intent.putExtra(ChatSettingsActivity.CHAT_ID, chat.getId());
                startActivity(intent);
                return true;
            case R.id.context_leave:
                handleLeaveChat(chat);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void handleLeaveChat(final Chat chat) {
        AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
        if (chat.isOwner(DatabaseManager.INSTANCE.getUserId())) {
            AlertDialog.Builder alert = new AlertDialog.Builder(activity);
            alert.setTitle(activity.getString(R.string.alert_owner));

            LinearLayout layout = new LinearLayout(activity);
            layout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);

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
            final ArrayAdapter<List<User>> adapter = new ArrayAdapter<List<User>>(activity,
                    android.R.layout.simple_list_item_single_choice, (List) participantNames);
            list.setAdapter(adapter);

            layout.addView(text, layoutParams);
            layout.addView(list, layoutParams);
            alert.setView(layout);

            alert.setPositiveButton(R.string.change_and_leave_chat,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            int position = list.getCheckedItemPosition();
                            if (position != AdapterView.INVALID_POSITION) {
                                Long newOwnerId = chat.getParticipants().get(position).getId();
                                Long leaveChat = 1L; // leaveChat rest call
                                new ChangeOwnerAndLeaveTask(chat).execute(newOwnerId, leaveChat);
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
                            dialog.dismiss();
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

    public void showChat(long chatId) {
        AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
        Log.d(this.getClass().getSimpleName(), "ShowChat: " + chatId);
        Intent intent = new Intent(activity, ChatActivity.class);
        intent.putExtra(activity.USER_MAIL, activity.getUserMail());
        intent.putExtra(activity.USER_ID, activity.getUserId());
        intent.putExtra(activity.CHAT_ID, chatId);
        intent.putExtra(activity.USER_NAME, activity.getSelfUser().getName());
        startActivity(intent);
    }

    @Override
    public void notifyFragment(List<Chat> chatRooms) {
        Log.d(super.getClass().getSimpleName(), "I have been notified. Yeeha!");
        if (chatRooms == null) {
            return;
        }
        this.chatRooms = chatRooms;
        adapter.clear();
        adapter.addAll(chatRooms);
        adapter.notifyDataSetChanged();
    }
}
