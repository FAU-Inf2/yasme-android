package net.yasme.android.storage;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.stmt.DeleteBuilder;

import net.yasme.android.asyncTasks.GetAllChatsForUserTask;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.User;

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

        new GetAllChatsForUserTask(context).execute(Long.toString(userId), accessToken);
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
            System.out.println("DB Access GetChats");
            chats = getHelper().getChatDao().queryForAll();
            System.out.println("DB Access after GetChats");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("SQLException");
        } catch (NullPointerException e) {
            System.out.println("DB Access failed");
            chats = null;
        }

        if(chats == null) {
            return new ArrayList<Chat>();
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
     * Retrieves the chat which all given users (and no one else) participate in
     * @param users list of users who have joined the chat
     * @return chat if existent, null otherwise
     */
    public Chat getChat(List<User> users) {
        // TODO

        List<Chat> chats = getAllChats();
        if (null == chats) {
            return null;
        }

        //for (Chat chat : chats) {
        //    List<User> participants = chat.getParticipants();
        //
        //    if (users.size() != participants.size()) {
        //        // Different number of participants. It's not this chat.
        //        continue;
        //    }
        //
        //    // Assume that we will find the exact list of participants in this chat
        //    boolean foundChat = true;
        //    for (int i=0; i<users.size() && foundChat; i++) {
        //        User user = users.get(i);
        //        boolean foundUser = false;
        //        for (int j=0; j<participants.size() && !foundUser; j++) {
        //            User participant = participants.get(j);
        //            foundUser |= participant.getId() == user.getId();
        //        }

                // Let's see if we found the current user
        //        if (!foundUser) {
                    // Next chat.. It's not this one
        //            foundChat = false;
        //        }
        //    }

        //   if (foundChat) {
        //       return chat;
        //   }
        //}

        return null;
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

    /**
     *
     * @param chat    Chat
     * @return true if chat with chatId exists, otherwise false
     */
    public Chat createIfNotExists(Chat chat) {
        try {
            return getHelper().getChatDao().createIfNotExists(chat);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @return the number of Chats stored in the database or -1 on error
     */
    public long getNumberOfChats() {
        try {
            return getHelper().getChatDao().countOf();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}