package net.yasme.android.asyncTasks.server;

import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.connection.ChatTask;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.ui.ContactActivity;
import net.yasme.android.ui.UserDetailsFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Stefan on 29.06.14.
 */
public class CreateSingleChatTask extends AsyncTask<String, Void, Boolean> {

    private DatabaseManager databaseManager = DatabaseManager.INSTANCE;
    private ContactActivity activity;
    private User user;
    private User selfUser;
    private long userId;
    private String accessToken;
    private UserDetailsFragment userDetailsFragment;
    private long newChatId = -1;
    private Chat newChat;


    public CreateSingleChatTask(ContactActivity activity,UserDetailsFragment userDetailsFragment, User selfUser, User user){
        this.activity = activity;
        this.selfUser = selfUser;
        this.user = user;
    }



    @Override
    protected Boolean doInBackground(String... params) {
        userId = Long.parseLong(params[0]);
        accessToken = params[1];

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
        if (newChatId == -1) {
            // Something went wrong
            Log.e(this.getClass().getSimpleName(), "newChatId is still -1.");
        }

        if (success) {
            // If a new chat was created, store it in the internal database
            if (null != newChat) {
                databaseManager.getChatDAO().add(newChat);
            }
            userDetailsFragment.startChat(newChatId);
        }
    }
}