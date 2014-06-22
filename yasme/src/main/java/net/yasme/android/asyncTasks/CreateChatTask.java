package net.yasme.android.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import net.yasme.android.connection.ChatTask;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.ui.InviteToChatFragment;

import java.util.Date;
import java.util.List;

/**
 * Created by bene on 22.06.14.
 */
public class CreateChatTask extends AsyncTask<String, Void, Boolean> {

    private DatabaseManager databaseManager = DatabaseManager.getInstance();
    protected Context context;
    protected InviteToChatFragment fragment;
    private List<User> selectedUsers;
    private long userId;
    private String accessToken;
    private long newChatId = -1;

    public CreateChatTask(Context context, InviteToChatFragment fragment, List<User> selectedUsers) {
        this.context = context;
        this.fragment = fragment;
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
        userId = Long.parseLong(params[0]);
        accessToken = params[1];

        Chat chat = databaseManager.getChat(selectedUsers);
        if (null != chat) {
            newChatId = chat.getId();
        } else {
            // No chat found in database. Create a new one

            User owner = fragment.getAbstractYasmeActivity().getSelfUser();
            // Concatenate chat name according to the participant's names
            String name = "";
            for (int i=0; i<selectedUsers.size(); i++) {
                name += selectedUsers.get(i).getName();
                if (i != selectedUsers.size() - 1) {
                    name += ", ";
                }
            }

            chat = new Chat(owner, "Created: " + new Date().toString(), name);
            chat.setParticipants(selectedUsers);
            chat.setNumberOfParticipants(selectedUsers.size());
            try {
                newChatId = ChatTask.getInstance().createChatwithPar(chat, userId, accessToken);
            } catch (RestServiceException e) {
                // TODO
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }


    /**
     * Invokes the fragment's method to show the chat activity
     */
    protected void onPostExecute(final Boolean success) {
        // TODO store new chat in DB

        if (success) {
            fragment.startChat(newChatId);
        }
    }
}
