package net.yasme.android.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Message;
import net.yasme.android.storage.DatabaseManager;

import java.util.ArrayList;

/**
 * Created by robert on 19.06.14.
 */
public class UpdateDBTask extends AsyncTask<String, Void, Boolean> {
    Context context;
    ArrayList<Message> messages;

    public UpdateDBTask(Context context, ArrayList<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    DatabaseManager dbManager = DatabaseManager.getInstance();

    /**
     * params[0] is lastMessageId
     * params[1] is userId
     * params[2] is accessToken
     */
    protected Boolean doInBackground(String... params) {
        ArrayList<Chat> chats = dbManager.getAllChats();

        if (messages == null) {
            Log.d(this.getClass().getSimpleName(), "messages sind null");
            return false;
        }
        if (messages.isEmpty()) {
            Log.d(this.getClass().getSimpleName(), "messages sind empty");
            return false;
        }
        if(chats == null) {
            Log.d(this.getClass().getSimpleName(), "chats sind null");
            return false;
        }

        for (Chat chat : chats) {
            for (Message msg : messages) {
                if(msg.getChat() == chat.getId()) {
                    chat.addMessage(msg);
                    Log.d(this.getClass().getSimpleName(), "Message added to DB");
                }
            }
            dbManager.updateChat(chat);
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if (success) {
            Log.i(this.getClass().getSimpleName(), "UpdateDB successfull");
        } else {
            Log.w(this.getClass().getSimpleName(), "UpdateDB not successfull");
        }
    }
}