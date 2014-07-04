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
        //ArrayList<Chat> chats = dbManager.getAllChats();

        if (messages == null) {
            Log.d(this.getClass().getSimpleName(), "messages sind null");
            return 1;
        }
        if (messages.isEmpty()) {
            return 0;
        }
        //if(chats == null) {
        //    return -1;
        //}

        //wird von DB erledigt

        /*for (Chat chat : chats) {
            for (Message msg : messages) {
                if(msg.getChatId() == chat.getId()) {
                    chat.addMessage(msg);
                    Log.d(this.getClass().getSimpleName(), "Message added to DB");
                }
            }
            dbManager.updateChat(chat);
        }*/
        dbManager.storeMessages(messages);
        return 1;
    }

    @Override
    protected void onPostExecute(final Integer success) {
        switch(success) {
            case -1:
                Log.w(this.getClass().getSimpleName(), "UpdateDB not successfull");
                Log.d(this.getClass().getSimpleName(), "chats sind null");
                break;
            case 0:
                Log.w(this.getClass().getSimpleName(), "UpdateDB not successfull");
                Log.d(this.getClass().getSimpleName(), "messages sind empty");
                break;
            case 1:
                Log.i(this.getClass().getSimpleName(), "UpdateDB successfull");
                break;
        }
    }
}