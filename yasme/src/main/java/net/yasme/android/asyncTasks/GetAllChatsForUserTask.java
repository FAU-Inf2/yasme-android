package net.yasme.android.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import net.yasme.android.connection.ChatTask;
import net.yasme.android.entities.Chat;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.DatabaseManager;

import java.util.ArrayList;

/**
 * Created by robert on 19.06.14.
 */
public class GetAllChatsForUserTask extends AsyncTask<String, Void, Boolean>{
    Context context;

    public GetAllChatsForUserTask(Context context) {
        this.context = context;
    }

    DatabaseManager dbManager = DatabaseManager.getInstance();
    ArrayList<Chat> chats;

    /**
     *
     * @param params
     *              0 is userId
     *              1 is accessToken
     * @return
     */
    protected Boolean doInBackground(String... params) {
        try {
            chats = new ArrayList<Chat>(ChatTask.getInstance().getAllChatsForUser(Long.parseLong(params[0]), params[1]));
        } catch (RestServiceException e) {
            System.out.println(e.getMessage());
            return false;
        }
        if(chats == null || chats.isEmpty()) {
            System.out.println("[DEBUG] getAllChatsForUser hat nicht geklappt!!");
        }
        for(Chat chat: chats) {
            if(dbManager.createIfNotExists(chat) != null) {
                dbManager.updateChat(chat);
                System.out.println("[DEBUG] Chat upgedatet");
            } else {
                System.out.println("[DEBUG] Chat eingefuegt");
            }
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        System.out.println("[Debug] GetAllChatsForUser hat geklappt");
    }
}
