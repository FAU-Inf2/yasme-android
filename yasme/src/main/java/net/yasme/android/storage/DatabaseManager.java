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
            for(User user : c.getParticipants()) {
                getHelper().getChatUserDao().createIfNotExists(new ChatUser(c, user));
            }
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
        try {
            List<ChatUser> temp = getHelper().getChatUserDao().
                    queryForEq(DatabaseConstants.CHAT_FIELD_NAME, chat);
            List<User> insert = new ArrayList<>();
            for(ChatUser cu : temp) {
                insert.add(cu.user);
            }
            chat.setParticipants(insert);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chat;
    }


    /**
     * Retrieves the chat which all given users (and no one else) participate in
     * @param users list of users who have joined the chat
     * @return list of chats if existent, null or an empty list otherwise
     */
    public List<Chat> getChats(List<User> users) {
        List<Chat> matchingChats = null;
        Chat search = new Chat(0, users, null, null, null, 0);
        try {
            matchingChats = getHelper().getChatDao().queryForMatchingArgs(search);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return matchingChats;
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

    public void createOrUpdateChat(Chat chat) {
        try {
            getHelper().getChatDao().createOrUpdate(chat);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    /**
     * ChatUser methods
     */
    public void createChatUser(ChatUser cu) {
        try {
            getHelper().getChatUserDao().create(cu);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteChatUser(ChatUser cu) {
        try {
            getHelper().getChatUserDao().delete(cu);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    /**
     * User methods
     */
    /**
     * Adds one user to database (using createIfNotExists)
     */
    public void createUserIfNotExists(User u) {
        try {
            getHelper().getUserDao().createIfNotExists(u);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     * This function will delete a user with userId
     * from table
     *
     * @param userId        ID (primary key) of user
     */
    public void deleteUser(long userId) {
        try {
            getHelper().getUserDao().deleteById(userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function will get all participants of one chat
     * @param chatId    long
     * @return participants of chat with chatId or null on error
     */
    public ArrayList<User> getParticipantsFromDB(long chatId) {
        ArrayList<User> participants = null;
        List<ChatUser> matching = null;
        try {
            Chat chat = getHelper().getChatDao().queryForId(chatId);
            matching = getHelper().getChatUserDao().
                    queryForEq(DatabaseConstants.CHAT_FIELD_NAME, chat);
        } catch (SQLException e) {
            return null;
        }
        for(ChatUser current : matching) {
            participants.add(current.user);
        }
        return participants;
    }

    /**
     * This function will get all participants with contactFlag = true
     * @return contacts or null on error
     */
    public List<User> getContactsFromDB() {
        List<User> contacts;
        try {
            contacts = getHelper().getUserDao().queryForEq(DatabaseConstants.CONTACT, 1);
        } catch (SQLException e) {
            contacts = null;
        }
        return contacts;
    }
}