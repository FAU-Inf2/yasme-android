package net.yasme.android.ui;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.GetChatListTask;
import net.yasme.android.asyncTasks.GetChatTask;
import net.yasme.android.asyncTasks.GetMessageTask;
import net.yasme.android.asyncTasks.GetProfileDataTask;
import net.yasme.android.controller.FragmentObservable;
import net.yasme.android.controller.NotifiableFragment;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.entities.Chat;
import net.yasme.android.storage.DatabaseManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by martin on 21.06.2014.
 */
public class ChatListFragment extends ListFragment implements NotifiableFragment<List<Chat>> {

    private AbstractYasmeActivity activity;
    private List<Chat> chatRooms = new ArrayList<Chat>();
    private ChatListAdapter adapter;

    public ChatListFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (AbstractYasmeActivity) getActivity();
        adapter = new ChatListAdapter(activity, R.layout.chatlist_item, chatRooms);
        setListAdapter(adapter);

        //Register at observer
        //ChatListParam test = new ChatListParam(null);
        //ObserverRegistry.getRegistry(ObserverRegistry.Observers.CHATLISTFRAGMENT).register(this);
        Log.d(this.getClass().getSimpleName(), "Try to get ChatListObservableInstance");
        FragmentObservable<ChatListFragment, List<Chat>> obs = ObservableRegistry.getObservable(ChatListFragment.class);
        Log.d(this.getClass().getSimpleName(), "... successful");
        obs.register(this);


        // Holt erst die Chats aus der Datenbank ab
        List<Chat> chatRoomsFromDB = DatabaseManager.INSTANCE.getChatDAO().getAll();
        adapter.updateChats(chatRooms);
        adapter.notifyDataSetChanged();

        //holt vor allem den Namen des Users ab
        new GetProfileDataTask(activity.storage).execute();

        
        // Dann beim Server nachfragen, ob es neue gibt, und in der Datenbank abspeichern

        //Aktualisiert die Datenbank auf den aktuellen Stand des Servers
        new GetChatTask().execute(Long.toString(activity.getUserId()), activity.getAccessToken());
        new GetMessageTask(activity.getStorage()).execute(Long.toString(activity.getUserId()), activity.getAccessToken());
        //new UpdateDBTask(activity.storage)
        //        .execute(Long.toString(activity.getUserId()), activity.getAccessToken());


        // Danach evtl nochmal aktualisieren

        //Laedt die Liste aller Chats von der Datenbank in das Fragment
        new GetChatListTask().execute();
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
        /*
       @Override
       public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
           View rootView = inflater.inflate(R.layout.fragment_chatlist, container,
                   false);
           return rootView;
       }
       */

    @Override
    public void onListItemClick(ListView l, View view, int pos, long id) {
        Chat chat = (Chat) getListAdapter().getItem(pos);
        showChat(chat.getId());
    }

    public void showChat(long chatId) {
        System.out.println("ShowChat: " + chatId);
        Intent intent = new Intent(activity, ChatActivity.class);
        intent.putExtra(activity.USER_MAIL, activity.getUserMail());
        intent.putExtra(activity.USER_ID, activity.getUserId());
        intent.putExtra(activity.CHAT_ID, chatId);
        intent.putExtra(activity.USER_NAME, activity.getSelfUser().getName());
        startActivity(intent);
    }

    // @Override
    // public void notifyFragment(ChatListParam value) {
    //    //Log.d(this.getClass().getSimpleName(),"I have been notified. Yeeha!");
    //}

    @Override
    public void notifyFragment(List<Chat> chatRooms) {
        Log.d(super.getClass().getSimpleName(), "I have been notified. Yeeha!");
        ChatListAdapter adapter = (ChatListAdapter) this.getListAdapter();
        //createDummyChatRoomList();
        //        chatRooms = ((ChatListParam)param).getChatRooms();
        this.chatRooms = chatRooms;
        adapter.updateChats(chatRooms);
        adapter.notifyDataSetChanged();
    }

/*
    public static class ChatListParam {
        public ArrayList<Chat> getChatRooms() {
            return chatRooms;
        }

        private ArrayList<Chat> chatRooms = null;
                public ChatListParam(ArrayList<Chat> chatRooms) {
                    this.chatRooms = chatRooms;
                }
        }

    //ChatListAdapter adapter = (ChatListAdapter)fragment.getListAdapter();
    //Log.d(this.getClass().getSimpleName(), "UpdateMessages: " + chatRooms.size());
    //adapter.updateChats(chatRooms);
    //adapter.notifyDataSetChanged();


    protected void createDummyChatRoomList() {
        chatRooms = new ArrayList<Chat>();
        int number = 10;
        for (int i = 1; i < number; i++)
        {
            Chat chat = new Chat();
            chat.setId(i);
            chat.setName("Chat " + i);
            chatRooms.add(chat);
        }
    }
    */
}
