package net.yasme.android.asyncTasks;

import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.controller.FragmentObserver2;
import net.yasme.android.entities.Chat;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.ui.ChatListAdapter;
import net.yasme.android.ui.ChatListFragment;

import java.util.ArrayList;

/**
 * Created by robert on 19.06.14.
 */
public class GetChatDataTask extends AsyncTask<String, Void, Boolean> {
    ChatListFragment fragment;
    int layoutId;

    public GetChatDataTask(ChatListFragment fragment) {
        this.fragment = fragment;
    }

    ArrayList<Chat> chatRooms = null;

    protected Boolean doInBackground(String... params) {
        chatRooms = DatabaseManager.getInstance().getAllChats();
        return chatRooms != null;
    }

    protected void onPostExecute(final Boolean success) {

        if(!success) {
            Log.d(this.getClass().getName(), "Fehler bei Datenbankzugriff");
            createDummyChatRoomList();
        }

        if (chatRooms.size() <= 0) {
            createDummyChatRoomList();
        }

        for(Chat chat : chatRooms) {
            System.out.println("[Debug] " + chat.toString());
        }

        ChatListAdapter adapter = (ChatListAdapter)fragment.getListAdapter();
        //fragment.setListAdapter(adapter);
        Log.d(this.getClass().getSimpleName(), "UpdateChats: " + chatRooms.size());
        adapter.updateChats(chatRooms);
        adapter.notifyDataSetChanged();
    }

    protected void createDummyChatRoomList() {
        Log.d(this.getClass().getSimpleName(), "Benutze Dummy-Liste");
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

