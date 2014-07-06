package net.yasme.android.ui;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.GetChatListTask;
import net.yasme.android.asyncTasks.GetProfileDataTask;
import net.yasme.android.asyncTasks.UpdateDBTask;
import net.yasme.android.controller.NotifiableFragment;
import net.yasme.android.controller.NotifyFragmentParameter;
import net.yasme.android.controller.ObserverRegistry;
import net.yasme.android.entities.Chat;

import java.util.ArrayList;

/**
 * Created by martin on 21.06.2014.
 */
public class ChatListFragment extends ListFragment implements NotifiableFragment<NotifyFragmentParameter> {

       private AbstractYasmeActivity activity;
       private ArrayList<Chat> chatRooms = new ArrayList<Chat>();
       private ChatListAdapter adapter;

       public ChatListFragment() {

       }

       @Override
       public void onCreate(Bundle savedInstanceState) {
           super.onCreate(savedInstanceState);
           activity = (AbstractYasmeActivity)getActivity();
           adapter = new ChatListAdapter(activity, R.layout.chatlist_item, chatRooms);
           setListAdapter(adapter);

            //Register at observer
           //ChatListParam test = new ChatListParam(null);
           ObserverRegistry.getRegistry(ObserverRegistry.Observers.CHATLISTFRAGMENT).register(this);

           new GetProfileDataTask(activity.getApplicationContext(), this, activity.storage)
                   .execute(Long.toString(activity.getUserId()), activity.getAccessToken(),
                           activity.getUserMail());

           //Aktualisiert die Datenbank auf den aktuellen Stand des Servers
           new UpdateDBTask(activity.getApplicationContext(), activity.storage)
                   .execute(Long.toString(activity.getUserId()), activity.getAccessToken());
           //Laedt die Liste aller Chats von der Datenbank in das Fragment
           new GetChatListTask().execute();
       }

        @Override
        public void onStop() {
            super.onStop();
            //De-Register at observer
            //ObserverRegistry.getRegistry(ObserverRegistry.Observers.CHATLISTFRAGMENT).remove(this);
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
       public void onListItemClick(ListView l, View view, int pos, long id)
       {
           Chat chat = (Chat)getListAdapter().getItem(pos);
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
    public void notifyFragment(NotifyFragmentParameter value) {
        Log.d(super.getClass().getSimpleName(),"I have been notified. Yeeha!");
        ChatListAdapter adapter = (ChatListAdapter)this.getListAdapter();
        //createDummyChatRoomList();
        chatRooms = ((ChatListParam)value).getChatRooms();
        adapter.updateChats(chatRooms);
        adapter.notifyDataSetChanged();
    }


    public static class ChatListParam implements NotifyFragmentParameter {
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
}
