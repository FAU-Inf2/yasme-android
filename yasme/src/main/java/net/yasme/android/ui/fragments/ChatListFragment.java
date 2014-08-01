package net.yasme.android.ui.fragments;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.database.GetAllTask;
import net.yasme.android.asyncTasks.server.GetMessageTask;
import net.yasme.android.asyncTasks.server.GetMyChatsTask;
import net.yasme.android.asyncTasks.server.GetProfileDataTask;
import net.yasme.android.controller.FragmentObservable;
import net.yasme.android.controller.NotifiableFragment;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.entities.Chat;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.storage.dao.ChatDAO;
import net.yasme.android.ui.AbstractYasmeActivity;
import net.yasme.android.ui.ChatListAdapter;
import net.yasme.android.ui.activities.ChatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by martin on 21.06.2014.
 */
public class ChatListFragment extends ListFragment implements NotifiableFragment<List<Chat>> {

    private List<Chat> chatRooms = new ArrayList<Chat>();
    private ChatListAdapter adapter;
    private int counter;

    public ChatListFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AbstractYasmeActivity activity = (AbstractYasmeActivity) getActivity();
        activity = (AbstractYasmeActivity) getActivity();

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

        //ab hier ist alles aus der onCreateMethode
        counter = 0;

        //progress bar on
        getActivity().setProgressBarIndeterminateVisibility(true);

        // At first, retrieve the chats from the database
        ChatDAO chatDAO = DatabaseManager.INSTANCE.getChatDAO();
        counter++;
        new GetAllTask(chatDAO, ChatListFragment.class).execute();

        // Dann beim Server nachfragen, ob es neue gibt, und in der Datenbank abspeichern
        // Aktualisiert die Datenbank auf den aktuellen Stand des Servers
        counter++;
        new GetMyChatsTask().execute();

        new GetMessageTask().execute();
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
        ChatListAdapter adapter = (ChatListAdapter) this.getListAdapter();
        this.chatRooms = chatRooms;
        adapter.updateChats(chatRooms);
        adapter.notifyDataSetChanged();

        counter--;
        if(counter == 0) {
            //progress bar off
            getActivity().setProgressBarIndeterminateVisibility(false);
        }
    }
}
