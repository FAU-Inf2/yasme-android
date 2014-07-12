package net.yasme.android.asyncTasks.server;

import android.os.AsyncTask;

import net.yasme.android.connection.ChatTask;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.ui.UserDetailsFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Stefan on 29.06.14.
 */
@Deprecated
public class CreateSingleChatTask extends AsyncTask<String, Void, Boolean> {

    private DatabaseManager databaseManager = DatabaseManager.INSTANCE;
    private User user;
    private User selfUser;
    private UserDetailsFragment userDetailsFragment;
    private long newChatId = -1;
    private Chat newChat;


    public CreateSingleChatTask(User selfUser, User user){
        this.selfUser = selfUser;
        this.user = user;
    }



    @Override
    protected Boolean doInBackground(String... params) {

        List<User> userList = new ArrayList<User>();
        userList.add(user);

        List<Chat> matchingChats = databaseManager.getChatDAO().getByParticipants(userList);
        if (null != matchingChats && matchingChats.size() > 0) {
            // Take first chat
            newChatId = matchingChats.get(0).getId();
        } else {
            // No chat found in database. Create a new one

            // Concatenate chat name according to the participant's names
            String name = user.getName();


            newChat = new Chat(selfUser, "Created: " + new Date().toString(), name);
            newChat.setParticipants(userList);
            try {
                newChatId = ChatTask.getInstance().createChatWithPar(newChat);
            } catch (RestServiceException e) {
                // TODO
                e.printStackTrace();
                return false;
            }
            newChat.setId(newChatId);
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
    }
}
