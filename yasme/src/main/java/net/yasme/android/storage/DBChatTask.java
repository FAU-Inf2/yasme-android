package net.yasme.android.storage;

import android.os.AsyncTask;

import net.yasme.android.entities.Chat;

/**
 * Created by robert on 15.06.14.
 */
public class DBChatTask {
    private static DBChatTask instance;
    private static DatabaseManager dbManager = DatabaseManager.getInstance();

    public static DBChatTask getInstance() {
        if (instance == null) {
            instance = new DBChatTask();
        }
        return instance;
    }

    private Chat chat;
    public class getChatTask extends AsyncTask<Long, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Long... params) {
            chat = dbManager.getChat(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(final Boolean success) {

        }
    }
}
