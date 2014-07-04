package net.yasme.android.asyncTasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.connection.ChatTask;
import net.yasme.android.connection.MessageTask;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Message;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.ChatUser;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.ui.AbstractYasmeActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by robert on 26.06.14.
 */
public class UpdateDBTask extends AsyncTask<String, Void, Integer>{

    DatabaseManager dbManager;
    ChatTask chatTask;
    MessageTask messageTask;
    SharedPreferences storage;
    long lastMessageId;

    public UpdateDBTask(Context context, SharedPreferences storage) { //TODO: context entfernen
        dbManager = DatabaseManager.getInstance();
        chatTask = ChatTask.getInstance();
        messageTask = MessageTask.getInstance(context);
        this.storage = storage;
        lastMessageId = storage.getLong(AbstractYasmeActivity.LAST_MESSAGE_ID, 0L);
    }

    /**
    * @param params
    *              0 is userId
    *              1 is accessToken
    * @return Returns true if it was successful, otherwise false
    */

    @Override
    protected Integer doInBackground(String... params) {
        List<Chat> serverChats = null;
        List<Message> serverMessages = null;

        long userId = Long.parseLong(params[0]);
        String accessToken = params[1];

        try {
            serverChats = chatTask.getAllChatsForUser();
        } catch (RestServiceException e) {
            Log.w(this.getClass().getSimpleName(), e.getMessage());
            return -2;
        }
        try {
            serverMessages = messageTask.getMessage(lastMessageId);
        } catch (RestServiceException e) {
            Log.w(this.getClass().getSimpleName(), e.getMessage());
            return -2;
        }
        lastMessageId = serverMessages.size() + lastMessageId;

        //Neue Chats einfuegen, neue Nachrichten einfuegen, dann updaten
        for(Chat chat : serverChats) {
            Chat chatWithInfo;

            //Infos fuer jeden chat abrufen
            try {
                chatWithInfo = chatTask.getInfoOfChat(chat.getId());
            } catch (RestServiceException e) {
                Log.w(this.getClass().getSimpleName(), e.getMessage());
                return -2;
            }
            chat.setParticipants(chatWithInfo.getParticipants());
            chat.setStatus(chatWithInfo.getStatus());
            //chat.setName(chatWithInfo.getStatus()); //TODO: ChatName in Info??

            //Participants in DB speichern, Beziehungstabelle aktualisieren
            List<User> users = chat.getParticipants();
            for (User user : users) {
                if (user.getName() != null) {
                    Log.d(this.getClass().getSimpleName(), "[Debug]" + user.getName());
                } else {
                    user.setName("dummy");
                    Log.d(this.getClass().getSimpleName(), "[Debug] userName ist null, verwende Dummy-Name");
                }
                //dbManager.createUserIfNotExists(user); //TODO
                dbManager.createOrUpdateUser(user);
                dbManager.createChatUser(new ChatUser(chat, user));
                Log.d(this.getClass().getSimpleName(), "User and ChatUser added to DB");
            }

            Log.d(this.getClass().getSimpleName(),
                    "Number of messages from server: " + serverMessages.size());


            //Nachrichten in passende Chats einfuegen - wird von DB erledigt

            //for (Message message : serverMessages) {
            //    if (message.getChatId() == chat.getId()) {
            //        chat.addMessage(message);
            //    }
            //}
            //if(chat.getMessages() == null || chat.getMessages().size() == 0) {
            //    chat.setMessages(new ArrayList<Message>());
            //}
            dbManager.storeMessages(serverMessages);
            dbManager.createOrUpdateChat(chat);
        }
        return 0;
    }

    @Override
    protected void onPostExecute(final Integer result) {
        if(result == 0) {
            Log.d(this.getClass().getSimpleName(), "Update DB erfolgreich");
        } else if(result == -2) {
            Log.d(this.getClass().getSimpleName(), "SQL Exception");
        } else {

        }
        Log.d(this.getClass().getSimpleName(), "new lastMessageId: " + Long.toString(lastMessageId));
        SharedPreferences.Editor editor = storage.edit();
        editor.putLong(AbstractYasmeActivity.LAST_MESSAGE_ID, lastMessageId);
        editor.commit();
    }

}
