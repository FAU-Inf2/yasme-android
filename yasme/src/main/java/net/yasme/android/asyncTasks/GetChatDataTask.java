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
            //TODO: Debug
            Log.e(this.getClass().getName(), "Fehler bei Datenbankzugriff");
            System.out.println("Fehler bei Datenbankzugriff");
        }


        //DEBUG
        if (chatRooms.size() <= 0) {
            Log.d(this.getClass().getSimpleName(), "Benutze Dummy-Liste");
            createDummyChatRoomList();
        }


        ChatListAdapter adapter = (ChatListAdapter)fragment.getListAdapter();
        //fragment.setListAdapter(adapter);
        Log.d(this.getClass().getSimpleName(), "UpdateMessages: " + chatRooms.size());
        adapter.updateChats(chatRooms);
        adapter.notifyDataSetChanged();
    }

    protected void createDummyChatRoomList() {
        chatRooms = new ArrayList<Chat>();
        int number = 10;
        for (int i = 1; i < number; i++)
        {
            Chat chat = new Chat();
            chat.setId(i);
            chat.setName("Chat " + i);
            chat.setNumberOfParticipants(number-i);
            chatRooms.add(chat);
        }
    }
}

