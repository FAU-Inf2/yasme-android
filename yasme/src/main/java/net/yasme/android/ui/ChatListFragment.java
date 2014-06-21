package net.yasme.android.ui;

import android.app.ListFragment;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.util.Log;

import net.yasme.android.R;
import net.yasme.android.asyncTasks.GetChatDataTask;
import net.yasme.android.asyncTasks.GetProfileDataTask;
import net.yasme.android.entities.Chat;
import net.yasme.android.ui.*;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by martin on 21.06.2014.
 */
public class ChatListFragment extends ListFragment {

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
            new GetChatDataTask(this).execute();
           //new GetProfileDataTask(getApplicationContext(), this).execute(Long.toString(userId), accessToken, userMail);
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

       public void showStandardChat() {
           showChat(1);
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
}
