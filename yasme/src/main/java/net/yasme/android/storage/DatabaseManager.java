package net.yasme.android.storage;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.stmt.DeleteBuilder;

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
            instance = new DatabaseManager(context, userId);
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

    private DatabaseManager(Context context, long userId) {
        helper = new DatabaseHelper(context, userId);
    }

    private DatabaseHelper getHelper() {
        return helper;
    }


    /******* CRUD functions ******/

    public List<User> getParticipantsForChat(long chatId) {
        List<User> insert = null;
        try {
            Chat c = new Chat();
            c.setId(chatId);
            List<ChatUser> temp = getHelper().getChatUserDao().queryForMatchingArgs(new ChatUser(c, null));
            insert = new ArrayList<User>();
            for(ChatUser cu : temp) {
                insert.add(cu.user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return insert;
    }

    /**
     * Adds one chat to database
     */
    public void createChat(Chat c) {
        try {
            getHelper().getChatDao().create(c);
            for(User user : c.getParticipants()) {
                getHelper().getUserDao().createIfNotExists(user);
                getHelper().getChatUserDao().create(new ChatUser(c, user));
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
            chats = getHelper().getChatDao().queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            chats = null;
        }

        if(chats == null) {
            return new ArrayList<Chat>();
        }
        for(Chat chat : chats) {
            chat.setParticipants(getParticipantsForChat(chat.getId()));
        }
        ArrayList<Chat> chatsArray = new ArrayList(chats);
        return chatsArray;
    }

    /**
     * This function will return one chat from database with chatId
     *
     * @param chatId        ID (primary key) of chat
     * @return chat with chatID or null if chat not exists
     */
    public Chat getChat(long chatId) {
        Chat chat = null;
        try {
            chat = getHelper().getChatDao().queryForId(chatId);
            if(chat == null) {
                return null;
            }
            chat.setParticipants(getParticipantsForChat(chat.getId()));
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
        Chat search = new Chat(0, users, null, null, null);
        try {
            matchingChats = getHelper().getChatDao().queryForMatchingArgs(search);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for(Chat chat : matchingChats) {
            chat.setParticipants(getParticipantsForChat(chat.getId()));
        }
        return matchingChats;
    }

    /**
     * This function will update a chat
     * in table
     *
     * @param chat    Chat
     */
    public void updateChat(Chat chat) {
        try {
            List<User> dBParticipants = getParticipantsFromDB(chat.getId());
            if(dBParticipants == null) {
                Log.e(this.getClass().getSimpleName(), "Error: Kein Teilnehmer in DB vorhanden");
                return;
            }
            for(User u : dBParticipants) {
                if(!chat.getParticipants().contains(u)) {
                    Chat queryChat = new Chat();
                    queryChat.setId(chat.getId());
                    ChatUser queryChatUser = new ChatUser(queryChat, u);
                    deleteChatUser(queryChatUser);
                }
            }
            for(User u: chat.getParticipants()) {
                if(!dBParticipants.contains(u)) {
                    Chat queryChat = new Chat();
                    queryChat.setId(chat.getId());
                    ChatUser queryChatUser = new ChatUser(queryChat, u);
                    createChatUser(queryChatUser);
                }
            }
            getHelper().getChatDao().update(chat);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createOrUpdateChat(Chat chat) {
        if(getChat(chat.getId()) != null) {
            updateChat(chat);
            Log.d(this.getClass().getSimpleName(), "updated chat");
        } else {
            createChat(chat);
            Log.d(this.getClass().getSimpleName(), "created chat");
        }
    }


    /**
     * ChatUser methods
     */
    /**
     *
     * @param cu
     */
    public void createChatUser(ChatUser cu) {
        try {
            Chat queryChat = new Chat();
            queryChat.setId(cu.chat.getId());
            ChatUser queryChatUser = new ChatUser(queryChat, cu.user);
            getHelper().getChatUserDao().create(queryChatUser);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteChatUser(ChatUser cu) {
        List<ChatUser> matching = new ArrayList<ChatUser>();
        try {
            Chat queryChat = new Chat();
            queryChat.setId(cu.chat.getId());
            ChatUser queryChatUser = new ChatUser(queryChat, cu.user);
            matching = getHelper().getChatUserDao().queryForMatchingArgs(queryChatUser);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(matching.isEmpty()) {
            Log.e(this.getClass().getSimpleName(),
                    "Error: Kein ChatUser zum Loeschen in DB vorhanden");
            return;
        }
        try {
            getHelper().getChatUserDao().deleteById(matching.get(0).id);
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
     * This function will get all participants of one chat
     * @param chatId    long
     * @return participants of chat with chatId or null on error
     */
    public ArrayList<User> getParticipantsFromDB(long chatId) {
        ArrayList<User> participants = new ArrayList<User>();
        List<ChatUser> matching;
        try {
            Chat queryChat = new Chat();
            queryChat.setId(chatId);
            matching = getHelper().getChatUserDao().
                    queryForEq(DatabaseConstants.CHAT_FIELD_NAME, queryChat);
        } catch (SQLException e) {
            return null;
        }
        for(ChatUser current : matching) {
            participants.add(current.user);
        }
        return participants;
    }

    /**
     * This function will get all participants with contactFlag = 1
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