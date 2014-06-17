package net.yasme.android.storage;

import android.content.Context;
import android.os.AsyncTask;

import com.j256.ormlite.stmt.DeleteBuilder;

import net.yasme.android.connection.ChatTask;
import net.yasme.android.entities.Chat;
import net.yasme.android.exception.RestServiceException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by robert on 13.06.14.
 */
public class DatabaseManager {

    static private DatabaseManager instance;
    private static Boolean initialized = false;

    static public void init(Context context, long userId, String accessToken) {
        if (null == instance) {
            instance = new DatabaseManager(context);
        }
        GetSeedTask getSeedTask = new GetSeedTask();
        getSeedTask.execute(Long.toString(userId), accessToken);
        initialized = true;
    }

    public static Boolean isInitialized() {
        return initialized;
    }

    static public DatabaseManager getInstance() {
        return instance;
    }

    private DatabaseHelper helper;

    private DatabaseManager(Context context) {
        helper = new DatabaseHelper(context);
    }

    private DatabaseHelper getHelper() {
        return helper;
    }


    /******* CRUD functions ******/

    /**
     * Adds one chat to database
     */
    public void addChat(Chat c) {
        try {
            getHelper().getChatDao().create(c);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function will return all chats from database
     *
     * @return List of chats or null on error
     */
    public ArrayList<Chat> getAllChats() {
        List<Chat> chats = null;
        try {
            chats = getHelper().getChatDao().queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println("DB Access failed");
            chats = null;
        }

        if(chats == null) {
            return null;
        }
        ArrayList<Chat> chatsArray = new ArrayList(chats);
        return chatsArray;
    }

    /**
     * This function will return one chats from database with chatId
     *
     * @param chatId        ID (primary key) of chat
     * @return chat with chatID or null if chat not exists
     */
    public Chat getChat(long chatId) {
        Chat chat = null;
        try {
            chat = getHelper().getChatDao().queryForId(chatId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chat;
    }

    /**
     * This function will delete chat with chatName
     * from table
     *
     * @param chatName    the name of the chat
     */
    public void deleteChat(String chatName) {
        try {
            DeleteBuilder<Chat, Long> deleteBuilder = getHelper().getChatDao().deleteBuilder();
            deleteBuilder.where().eq(DatabaseConstants.CHAT_NAME, chatName);
            deleteBuilder.delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function will delete a chat with chatId
     * from table
     *
     * @param chatId        ID (primary key) of chat
     */
    public void deleteChat(long chatId) {
        try {
            getHelper().getChatDao().deleteById(chatId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function will update a chat
     * in table
     *
     * @param chat    Chat
     */
    public void updateChat(Chat chat) {
        try {
            getHelper().getChatDao().update(chat);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static class GetSeedTask extends AsyncTask<String, Void, Boolean> {

        protected Boolean doInBackground(String... params) {
            ChatTask chatTask = ChatTask.getInstance();

            long numberOfChats = 16L;
            Chat chat = null;
            for(long i = 0; i < numberOfChats; i++) {
                try {
                    chat = chatTask.getInfoOfChat(i, Long.parseLong(params[0]), params[1]);
                } catch (RestServiceException e) {
                    e.printStackTrace();
                }
                if (chat != null) {
                    instance.addChat(chat);
                } else {
                    break;
                }
            }
            return true;
        }
    }
}