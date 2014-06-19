package net.yasme.android.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;
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
        if(updateChats()) {
            return updateMessages();
        }
        return false;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if (success) {
            Toast.makeText(context.getApplicationContext(), "[Debug] UpdateDB successfull",
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            Toast.makeText(context.getApplicationContext(), "[Debug] UpdateDB not successfull",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private Boolean updateMessages() {
        ArrayList<Chat> chats = dbManager.getAllChats();

        if (messages == null) {
            return false;
        }
        if (messages.isEmpty()) {
            return false;
        }
        if(chats == null) {
            return false;
        }

        for (Chat chat : chats) {
            for (Message msg : messages) {
                if(msg.getChat() == chat.getId()) {
                    chat.addMessage(msg);
                }
            }
            dbManager.updateChat(chat);
        }
        return true;
    }

    private Boolean updateChats() {
        //ChatTask chatTask = ChatTask.getInstance();

        /*long numberOfChats = 15;
        Chat chat = null;

        for(long i = 1; i <= numberOfChats; i++) {
            try {
                chat = chatTask.getInfoOfChat(i, userId, accessToken);
            } catch (RestServiceException e) {
                e.printStackTrace();
            }
            if (chat != null) {
                System.out.println("Inserted: " + chat.toString());
                instance.addChat(chat);
            } else {
                break;
            }
        }*/
        //TODO: geaenderte chats updaten
        return true;
    }
}