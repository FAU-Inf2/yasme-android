package net.yasme.android.asyncTasks;

import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.connection.ChatTask;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.ChatUser;
import net.yasme.android.storage.DatabaseManager;

import java.util.Date;
import java.util.List;

/**
 * Created by robert on 07.07.14.
 */
public class GetChatTask extends AsyncTask<String, Void, Boolean> {
    DatabaseManager dbManager;
    long userId;
    String accessToken;

    public GetChatTask() {
        dbManager = DatabaseManager.getInstance();
    }

    /**
     * @param params 0 is userId
     *               1 is accessToken
     * @return Returns true if it was successful, otherwise false
     */
    protected Boolean doInBackground(String... params) {
        userId = Long.parseLong(params[0]);
        accessToken = params[1];

        List<Chat> serverChats = null;
        List<Chat> dbChats = dbManager.getAllChats();

        try {
            serverChats = ChatTask.getInstance().getAllChatsForUser();
        } catch (RestServiceException e) {
            Log.w(this.getClass().getSimpleName(), e.getMessage());
        }


        for (Chat chat : serverChats) {
            if (chat.getLastModified().after(new Date())) {
                Chat chatWithInfo;

                //Infos fuer jeden chat abrufen
                try {
                    chatWithInfo = ChatTask.getInstance().getInfoOfChat(chat.getId());
                } catch (RestServiceException e) {
                    Log.w(this.getClass().getSimpleName(), e.getMessage());
                    return false;
                }
                chat.setParticipants(chatWithInfo.getParticipants());
                chat.setStatus(chatWithInfo.getStatus());

                //Participants in DB speichern, Beziehungstabelle aktualisieren
                List<User> users = chat.getParticipants();
                for (User user : users) {
                    if (user.getName() != null) {
                        Log.d(this.getClass().getSimpleName(), "[Debug] added " + user.getName());
                    } else {
                        continue;
                    }
                    dbManager.createOrUpdateUser(user);
                    dbManager.createChatUser(new ChatUser(chat, user));
                    Log.d(this.getClass().getSimpleName(), "User and ChatUser added to DB");
                }
                dbManager.updateChat(chat);
            } else {
                continue;
            }
        }


        return true;
    }


    /**
     * updates Database,
     * stores lastMessageId
     */
    @Override
    protected void onPostExecute(final Boolean success) {

        if (!success) {
            Log.w(this.getClass().getSimpleName(), "");
            return;
        }

        Log.i(this.getClass().getSimpleName(), "");


        //TODO: abrufen der neuen nachrichten durch den Chat triggern
        //Observer benachrichtigen
    }
}