package de.fau.cs.mad.yasme.android.ui.fragments;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.asyncTasks.database.GetAllTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.GetMessageTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.GetMyChatsTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.GetProfileDataTask;
import de.fau.cs.mad.yasme.android.asyncTasks.server.LeaveChatTask;
import de.fau.cs.mad.yasme.android.controller.FragmentObservable;
import de.fau.cs.mad.yasme.android.controller.NotifiableFragment;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.entities.Chat;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.dao.ChatDAO;
import de.fau.cs.mad.yasme.android.ui.AbstractYasmeActivity;
import de.fau.cs.mad.yasme.android.ui.ChatListAdapter;
import de.fau.cs.mad.yasme.android.ui.activities.ChatActivity;
import de.fau.cs.mad.yasme.android.ui.activities.ChatSettingsActivity;

/**
 * Created by martin on 21.06.2014.
 */
public class ChatListFragment extends ListFragment implements NotifiableFragment<List<Chat>> {

    private List<Chat> chatRooms = new ArrayList<Chat>();
    private ChatListAdapter adapter;

    public ChatListFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();

        adapter = new ChatListAdapter(activity, R.layout.chatlist_item, chatRooms);
        //adapter.setNotifyOnChange(true);
        setListAdapter(adapter);

        //holt vor allem den Namen des Users ab
        new GetProfileDataTask().execute();

        //wird schon in GetMyChatsTask erledigt
        //new GetAllTask(chatDAO, ChatListFragment.class).execute();
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

        //ab hier ist alles aus der onCreateMethode

        // At first, retrieve the chats from the database
        ChatDAO chatDAO = DatabaseManager.INSTANCE.getChatDAO();
        new GetAllTask(chatDAO, ChatListFragment.class).execute();

        // Dann beim Server nachfragen, ob es neue gibt, und in der Datenbank abspeichern
        // Aktualisiert die Datenbank auf den aktuellen Stand des Servers

        new GetMyChatsTask().execute(this.getClass().getName());
        new GetMessageTask().execute(this.getClass().getName());
    }

    @Override
    public void onStop() {
        super.onStop();
        //De-Register at observer
        //ObserverRegistry.getRegistry(ObserverRegistry.Observers.CHATLISTFRAGMENT).remove(this);
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
        Chat chat = (Chat) getListAdapter().getItem(info.position);
        switch (item.getItemId()) {

            case R.id.context_settings:
                Intent intent = new Intent(getActivity(), ChatSettingsActivity.class);
                intent.putExtra("chat", chat);
                startActivity(intent);
                return true;
            case R.id.context_leave:
                new LeaveChatTask(chat, getActivity()).onPreExecute();
                return true;
            default:
                return super.onContextItemSelected(item);
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
        if(chatRooms == null) {
            adapter.notifyDataSetChanged();
            return;
        }
        ChatListAdapter adapter = (ChatListAdapter) this.getListAdapter();
        this.chatRooms = chatRooms;
        adapter.updateChats(chatRooms);
        adapter.notifyDataSetChanged();
    }

//    private void startAlarm() {
//        Timer timer = new Timer("outdatedServerCall");
//        final String outdatedMessage;
//
//        TimerTask task = new TimerTask() {
//            @Override
//            public void run() {
//                TextView server = (TextView) getActivity().findViewById(R.id.server_messages);
//                String outdatedMessage;
//                try {
//                    outdatedMessage = AuthorizationTask.getInstance().outdated();
//                } catch (RestServiceException e) {
//                    Log.e(this.getClass().getSimpleName(), e.getMessage());
//                    outdatedMessage = "";
//                }
//                if(outdatedMessage.isEmpty()) {
//                    server.setVisibility(View.GONE);
//                    return;
//                }
//                server.setText(outdatedMessage);
//                server.setVisibility(View.VISIBLE);
//            }
//        };
//        timer.scheduleAtFixedRate(task, 0, 86400000);
//    }
}
