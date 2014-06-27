package net.yasme.android.asyncTasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.connection.ChatTask;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.ui.AbstractYasmeActivity;

import java.util.ArrayList;

/**
 * Created by robert on 19.06.14.
 */
@Deprecated
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
            chats = new ArrayList<Chat>(ChatTask.getInstance().getAllChatsForUser());
        } catch (RestServiceException e) {
            System.out.println(e.getMessage());
            return false;
        }
        if(chats == null || chats.isEmpty()) {
            return false;
        }
        for(Chat chat: chats) {
            Chat chatInfo;
            try {
                chatInfo = ChatTask.getInstance().getInfoOfChat(chat.getId());
            } catch (RestServiceException e) {
                chatInfo = null;
                Log.w(this.getClass().getSimpleName(), e.getMessage());
            }
            if(chatInfo != null) {
                ArrayList<User> participants = chatInfo.getParticipants();
                for(User user : participants) {
                    dbManager.createUserIfNotExists(user);
                }
                //if(chat.getNumberOfParticipants() != chatInfo.getNumberOfParticipants()) { //TODO
                    chat.setParticipants(participants);
                    Log.d(this.getClass().getSimpleName(), "Participants geupdatet");
                //}
                if(chatInfo.getStatus() != null) {
                    chat.setStatus(chatInfo.getStatus());
                }
            } else {
                Log.d(this.getClass().getSimpleName(),
                        "Fehler bei getInfoOfChat " + Long.toString(chatInfo.getId()));
            }
            if(/*dbManager.createIfNotExists(chat) != null*/dbManager.getChat(chat.getId()) != null) {
                dbManager.updateChat(chat);
                Log.i(this.getClass().getSimpleName(), "Chat upgedatet");
            } else {
                dbManager.createChat(chat);
                Log.i(this.getClass().getSimpleName(), "Neuer Chat eingefuegt");
            }
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if(success) {
            System.out.println("[Debug] GetAllChatsForUser hat geklappt");
        } else {
            System.out.println("[DEBUG] GetAllChatsForUser hat nicht geklappt!!");
        }
    }
}
