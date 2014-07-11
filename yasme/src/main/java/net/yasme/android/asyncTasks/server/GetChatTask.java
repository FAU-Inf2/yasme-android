package net.yasme.android.asyncTasks.server;

import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.connection.ChatTask;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.ChatUser;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.storage.dao.ChatDAO;
import net.yasme.android.storage.dao.UserDAO;

import java.util.Date;
import java.util.List;

/**
 * Created by robert on 07.07.14.
 */
public class GetChatTask extends AsyncTask<String, Void, Boolean> {

    private DatabaseManager dbManager;
    private UserDAO userDAO;
    private ChatDAO chatDAO;

    private List<Chat> chatsToReturn;

    public GetChatTask() {
        dbManager = DatabaseManager.INSTANCE;
        userDAO = DatabaseManager.INSTANCE.getUserDAO();
        chatDAO = DatabaseManager.INSTANCE.getChatDAO();
    }

    /**
     * @return Returns true if it was successful, otherwise false
     */
    protected Boolean doInBackground(String... params) {

        List<Chat> serverChats = null;

        try {
            serverChats = ChatTask.getInstance().getAllChatsForUser();
        } catch (RestServiceException e) {
            Log.w(this.getClass().getSimpleName(), e.getMessage());
        }

        if(serverChats == null) {
            Log.e(this.getClass().getSimpleName(), "serverChats sind null");
            return false;
        }


        for (Chat chat : serverChats) {
            //if (chat.getLastModified().after(new Date())) {
                Chat chatWithInfo;

                //Infos fuer jeden chat abrufen
                //try {
                //    chatWithInfo = ChatTask.getInstance().getInfoOfChat(chat.getId());
                //} catch (RestServiceException e) {
                //    Log.w(this.getClass().getSimpleName(), e.getMessage());
                //    return false;
                //}


                //chat.setParticipants(chatWithInfo.getParticipants());
                //chat.setStatus(chatWithInfo.getStatus());

                //Participants in DB speichern, Beziehungstabelle aktualisieren

                //List<User> users = chat.getParticipants();
                //for (User user : users) {
                //    if (user.getName() != null) {
                //        Log.d(this.getClass().getSimpleName(), "[Debug] added " + user.getName());
                //    } else {
                //        continue;
                //    }
                //    userDAO.addOrUpdate(user);
                //    dbManager.createChatUser(new ChatUser(chat, user));
                //    Log.d(this.getClass().getSimpleName(), "User and ChatUser added to DB");
                //}
                //dbManager.createOrUpdateChat(chat);

                Chat insertedChat = chatDAO.addOrUpdate(chat);
                if (null == insertedChat) {
                    Log.e(this.getClass().getSimpleName(), "An error occurred while add or updating a chat object to the database");
                    return false;
                }
            //} else {
            //    continue;
            //}
        }

        if (null == (chatsToReturn = chatDAO.getAll())) {
            Log.e(this.getClass().getSimpleName(), "Error while trying to retrieve all chats from the database.");
            return false;
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
            Log.w(this.getClass().getSimpleName(), "failed");
            return;
        }

        Log.i(this.getClass().getSimpleName(), "success");

        //TODO: abrufen der neuen nachrichten durch den Chat triggern
        //Observer benachrichtigen
    }
}