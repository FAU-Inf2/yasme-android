package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.os.AsyncTask;
import android.util.Log;

import de.fau.cs.mad.yasme.android.connection.ChatTask;
import de.fau.cs.mad.yasme.android.controller.ObservableRegistry;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.entities.Chat;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.dao.ChatDAO;
import de.fau.cs.mad.yasme.android.storage.dao.UserDAO;
import de.fau.cs.mad.yasme.android.ui.fragments.ChatListFragment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by robert on 07.07.14.
 */
public class GetMyChatsTask extends AsyncTask<String, Void, Boolean> {

    private DatabaseManager dbManager;
    private UserDAO userDAO;
    private ChatDAO chatDAO;

    private List<Chat> chatsToReturn;

    public GetMyChatsTask() {
        dbManager = DatabaseManager.INSTANCE;
        userDAO = DatabaseManager.INSTANCE.getUserDAO();
        chatDAO = DatabaseManager.INSTANCE.getChatDAO();
    }

    /**
     * Requests the user's chats from the server and updates the database.
     * @return Returns true if it was successful, otherwise false
     */
    protected Boolean doInBackground(String... params) {
        SpinnerObservable.getInstance().registerBackgroundTask(this);
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

        Log.e(this.getClass().getSimpleName(), "Something to refresh?");
        refresh(serverChats);
        Log.e(this.getClass().getSimpleName(), "Something to refresh finished!");

        // Debug
        if (serverChats.size() > 0) {
            Log.d(getClass().getSimpleName(),"LastMod: " + serverChats.get(0).getOwner().getLastModified().toString());
        }


        // Swap the complete database table with the new chats
        if (!chatDAO.refreshAll(serverChats)) {
            Log.e(this.getClass().getSimpleName(), "Refreshing all chats failed");
        }

        if (null == (chatsToReturn = chatDAO.getAll())) {
            Log.e(this.getClass().getSimpleName(), "Error while trying to retrieve all chats from the database.");
            return false;
        }
        return true;
    }


    @Override
    protected void onPostExecute(final Boolean success) {
        new GetInfoTask().execute();
        if (!success) {
            Log.w(this.getClass().getSimpleName(), "failed");
            SpinnerObservable.getInstance().removeBackgroundTask(this);
            return;
        }

        Log.i(this.getClass().getSimpleName(), "success");
        ObservableRegistry.getObservable(ChatListFragment.class).notifyFragments(chatsToReturn);
        SpinnerObservable.getInstance().removeBackgroundTask(this);
    }

    private boolean refresh(List<Chat> serverChats) {
        Set<Long> refreshUserIds = new HashSet<>();
        if (serverChats == null) {
            return false;
        }
        for (Chat chat : serverChats) {
            Log.d(this.getClass().getSimpleName(), "Chat " + chat.getId() + " outdated?");
            for (User user : chat.getParticipants()) {
                Log.d(this.getClass().getSimpleName(), "User " + user.getId() + " outdated?");
                User dbUser = userDAO.get(user.getId());
                if (dbUser == null || dbUser.getLastModified() == null) {
                    Log.d(this.getClass().getSimpleName(), "Yes, not in DB");
                    refreshUserIds.add(user.getId());
                    continue;
                }
                if (user.getLastModified().compareTo(dbUser.getLastModified()) > 0) {
                    Log.d(this.getClass().getSimpleName(), "Yes, not up-to-date");
                    refreshUserIds.add(user.getId());
                }
            }
        }
        if (refreshUserIds.size() > 0 ) {
            Log.e(this.getClass().getSimpleName(), refreshUserIds.size() + " users are not up-to-date");
            RefreshTask refreshTask = new RefreshTask(RefreshTask.RefreshType.USER, refreshUserIds, true);
            refreshTask.execute();
        }
        return true;
    }
}