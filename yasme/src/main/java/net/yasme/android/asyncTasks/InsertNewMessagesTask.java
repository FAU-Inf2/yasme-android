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
public class InsertNewMessagesTask extends AsyncTask<String, Void, Integer> {
    ArrayList<Message> messages;

    public InsertNewMessagesTask(ArrayList<Message> messages) {
        this.messages = messages;
    }

    DatabaseManager dbManager = DatabaseManager.getInstance();

    /**
     * params[0] is lastMessageId
     * params[1] is userId
     * params[2] is accessToken
     */
    protected Integer doInBackground(String... params) {
        ArrayList<Chat> chats = dbManager.getAllChats();

        if (messages == null) {
            Log.d(this.getClass().getSimpleName(), "messages sind null");
            return 1;
        }
        if (messages.isEmpty()) {
            return 0;
        }
        if(chats == null) {
            return -1;
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
        return 1;
    }

    @Override
    protected void onPostExecute(final Integer success) {
        if (success == 1) {
            Log.i(this.getClass().getSimpleName(), "UpdateDB successfull");
        } else {
            Log.w(this.getClass().getSimpleName(), "UpdateDB not successfull");
            if(success == -1) {
                Log.d(this.getClass().getSimpleName(), "chats sind null");
            }
            if (success == 0) {
                Log.d(this.getClass().getSimpleName(), "messages sind empty");
            }
        }
    }
}