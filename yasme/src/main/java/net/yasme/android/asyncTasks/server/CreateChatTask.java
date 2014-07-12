package net.yasme.android.asyncTasks.server;

import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.connection.ChatTask;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.DatabaseManager;

import java.util.Date;
import java.util.List;

/**
 * Created by bene on 22.06.14.
 */
public class CreateChatTask extends AsyncTask<String, Void, Boolean> {

    private DatabaseManager databaseManager = DatabaseManager.INSTANCE;
    private User selfUser;
    private List<User> selectedUsers;
    private long newChatId = -1;
    private Chat newChat;

    public CreateChatTask(User selfUser, List<User> selectedUsers) {
        this.selfUser = selfUser;
        this.selectedUsers = selectedUsers;
    }


    /**
     * @param params
     *              0 is userId
     *              1 is accessToken
     * @return Returns true if it was successful, otherwise false
     */
    @Override
    protected Boolean doInBackground(String... params) {

        List<Chat> matchingChats = databaseManager.getChatDAO().getByParticipants(selectedUsers);
        if (null != matchingChats && matchingChats.size() > 0) {
            // Take first chat
            newChatId = matchingChats.get(0).getId();
        } else {
            // No chat found in database. Create a new one

            // Concatenate chat name according to the participant's names
            String name = "";
            for (int i=0; i<selectedUsers.size(); i++) {
                name += selectedUsers.get(i).getName();
                if (i != selectedUsers.size() - 1) {
                    name += ", ";
                }
            }

            newChat = new Chat(selfUser, "Created: " + new Date().toString(), name);
            newChat.setParticipants(selectedUsers);
            try {
                newChatId = ChatTask.getInstance().createChatWithPar(newChat);
            } catch (RestServiceException e) {
                Log.e(this.getClass().getSimpleName(), e.getMessage());
                return false;
            }
            newChat.setId(newChatId);
        }

        return true;
    }


    /**
     * Invokes the fragment's method to show the chat activity
     */
    protected void onPostExecute(final Boolean success) {
        if (newChatId == -1) {
            // Something went wrong
            Log.e(this.getClass().getSimpleName(), "newChatId is still -1.");
        }

        if (success) {
            // If a new chat was created, store it in the internal database
            if (null != newChat) {
                databaseManager.getChatDAO().addIfNotExists(newChat);
            }

            //TODO: Observer benachrichtigen, mit zwei Fragments UserDetailFragment und Invite to Chat
        }
    }
}
