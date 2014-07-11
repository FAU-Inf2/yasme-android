package net.yasme.android.asyncTasks.server;

import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.entities.Chat;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.ui.ChatListFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by robert on 19.06.14.
 */
public class GetChatListTask extends AsyncTask<String, Void, Boolean> {

    private List<Chat> chatRooms = null;

    public GetChatListTask() {
    }


    protected Boolean doInBackground(String... params) {
        //chatRooms = DatabaseManager.INSTANCE.getAllChats();
        chatRooms = DatabaseManager.INSTANCE.getChatDAO().getAll();
        return chatRooms != null;
    }

    protected void onPostExecute(final Boolean success) {
        if (!success) {
            Log.d(this.getClass().getName(), "Fehler bei Datenbankzugriff");
            //createDummyChatRoomList();
        }

        //if (chatRooms.size() <= 0) {
        //    createDummyChatRoomList();
        //}

        ObservableRegistry.getObservable(ChatListFragment.class).notifyFragments(chatRooms);
    }

    protected void createDummyChatRoomList() {
        Log.d(this.getClass().getSimpleName(), "Benutze Dummy-Liste");
        chatRooms = new ArrayList<Chat>();
        int number = 10;
        for (int i = 1; i < number; i++) {
            Chat chat = new Chat();
            chat.setId(i);
            chat.setName("Chat " + i);
            chatRooms.add(chat);
        }
    }
}

