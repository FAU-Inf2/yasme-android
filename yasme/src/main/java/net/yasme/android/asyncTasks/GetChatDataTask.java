package net.yasme.android.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import net.yasme.android.ChatListAdapter;
import net.yasme.android.R;
import net.yasme.android.YasmeChats;
import net.yasme.android.entities.Chat;
import net.yasme.android.storage.DatabaseManager;

import java.util.ArrayList;

/**
 * Created by robert on 19.06.14.
 */
public class GetChatDataTask extends AsyncTask<String, Void, Boolean> {
    Context context;
    YasmeChats activity;

    public GetChatDataTask(Context context, YasmeChats activity) {
        this.context = context;
        this.activity = activity;
    }

    ArrayList<Chat> chatrooms = null;

    protected Boolean doInBackground(String... params) {
        chatrooms = DatabaseManager.getInstance().getAllChats();
        return chatrooms != null;
    }

    protected void onPostExecute(final Boolean success) {

        if(!success) {
            //TODO: Debug
            Log.e(this.getClass().getName(), "Fehler bei Datenbankzugriff");
            System.out.println("Fehler bei Datenbankzugriff");
        }

        //DEBUG
        if (chatrooms.size() <= 0) {
            Log.d(this.getClass().getSimpleName(), "Benutze Dummy-Liste");
            System.out.println("Benutze Dummy-Liste");
            createDummyChatroomList();
        }

        ListAdapter adapter = new ChatListAdapter(activity, R.layout.chatlist_item, chatrooms);
        final ListView list = (ListView)activity.findViewById(R.id.chatroom_list);

        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
            {
                Long chatId = (Long)view.getTag();
                activity.showChat(chatId);
            }
        });

    }

    protected void createDummyChatroomList() {
        chatrooms = new ArrayList<Chat>();
        int number = 10;
        for (int i = 1; i < number; i++)
        {
            Chat chat = new Chat();
            chat.setId(i);
            chat.setName("Chat " + i);
            chat.setNumberOfParticipants(number-i);
            chatrooms.add(chat);
        }
    }
}