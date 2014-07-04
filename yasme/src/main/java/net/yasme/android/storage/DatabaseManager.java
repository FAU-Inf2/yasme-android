package net.yasme.android.storage;

import android.content.Context;
import android.util.Log;

import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Message;
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

    /**
     * Adds one chat to database
     * @param chat     Chat
     */
    public void createChat(Chat chat) {
        try {
            for(User user : chat.getParticipants()) {
                getHelper().getUserDao().createIfNotExists(user);
                getHelper().getChatUserDao().create(new ChatUser(chat, user));
            }
            getHelper().getChatDao().create(chat);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function will return all chats from database
     * @return list of chats or an empty list on error
     */
    public ArrayList<Chat> getAllChats() {
        List<Chat> chats = null;
        try {
            chats = getHelper().getChatDao().queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
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
            chat.setMessages(new ArrayList<Message>(getMessagesForChat(chat.getId())));
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
        Chat search = new Chat(0, users, null, null, null); //TODO: not working
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
            List<User> dbParticipants = getParticipantsForChat(chat.getId());
            if(dbParticipants == null) {
                Log.e(this.getClass().getSimpleName(), "Error: Kein Teilnehmer in DB vorhanden");
                return;
            }
            for(User u : dbParticipants) {
                if(!chat.getParticipants().contains(u)) {
                    Chat queryChat = new Chat();
                    queryChat.setId(chat.getId());
                    ChatUser queryChatUser = new ChatUser(queryChat, u);
                    deleteChatUser(queryChatUser);
                }
            }
            for(User u: chat.getParticipants()) {
                if(!dbParticipants.contains(u)) {
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

    /**
     * Updates the given chat or creates a new chat, if no such chat exists
     * @param chat      Chat
     */
    public void createOrUpdateChat(Chat chat) {
        try {
            if(getHelper().getChatDao().idExists(chat.getId())) {
            //if(getChat(chat.getId()) != null) {
                updateChat(chat);
                Log.d(this.getClass().getSimpleName(), "updated chat");
            } else {
                createChat(chat);
                Log.d(this.getClass().getSimpleName(), "created chat");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * ChatUser methods
     */
    /**
     * creates a new ChatUser object
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

    /**
     * Removes a ChatUser object from the Database
     * @param cu    ChatUser
     */
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
     * Updates the given user or creates a new user, if no such user exists
     * @param u     User
     */
    public void createOrUpdateUser(User u) {
        try {
            User tmp = getHelper().getUserDao().queryForId(u.getId());
            if(tmp == null) {
                getHelper().getUserDao().create(u);
            } else {
                if(tmp.isContact() == 1) {
                    u.addToContacts();
                    getHelper().getUserDao().update(u);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function if a user is already stored in the Database
     * @param userId    long
     * @return true if user with userId exists, otherwise false
     */
    public boolean existsUser(long userId) {
        try {
            return getHelper().getUserDao().idExists(userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * This function returns all participants from the chat with the given chatId
     * @param chatId    long
     * @return a list of users
     */
    public List<User> getParticipantsForChat(long chatId) {
        List<User> insert = null;
        try {
            Chat c = new Chat();
            c.setId(chatId);
            List<ChatUser> temp = null;
            temp = getHelper().getChatUserDao().queryForMatchingArgs(new ChatUser(c, null));

            insert = new ArrayList<User>();
            if(temp == null) {
                Log.d(this.getClass().getSimpleName(), "[Debug] keine participants in DB gefunden");
            }
            if(temp.isEmpty()) {
                Log.d(this.getClass().getSimpleName(), "[Debug] keine participants in DB gefunden");
            }
            for(ChatUser cu : temp) {
                User tmp = getHelper().getUserDao().queryForId(cu.user.getId());
                if(tmp == null) {
                    continue;
                }
                insert.add(getHelper().getUserDao().queryForId(cu.user.getId()));
                Log.d(this.getClass().getSimpleName(), "[Debug] name from DB: " + tmp.getName());
            }
            if(insert.isEmpty()) {
                Log.d(this.getClass().getSimpleName(), "[Debug] keine participants in DB gefunden");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return insert;
    }

    /**
     * This function returns the user with userId
     * @param userId    long
     * @return User with userId or null if no such User exists
     */
    public User getUser(long userId) {
        try {
            return getHelper().getUserDao().queryForId(userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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

    /**
     * This function will reset the contactFlag so that the user is removed from the contact list
     * @param u     User
     */
    public void removeContactFromDB(User u) {
        try {
            getHelper().getUserDao().queryForId(u.getId());
            u.removeFromContacts();
            getHelper().getUserDao().update(u);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Message methods
     */
    public void storeMessages(List<Message> messages) {
        for(Message msg : messages) {
            try {
                getHelper().getMessageDao().create(msg);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Message> getMessagesForChat(long chatId) {
        Chat chat = new Chat();
        chat.setId(chatId);
        Message matchingObj = new Message(null, null, null, chat, 0);
        List<Message> matching = null;
        try {
            matching = getHelper().getMessageDao().queryForEq(DatabaseConstants.CHAT, chat);
            //matching = getHelper().getMessageDao().queryForMatchingArgs(matchingObj);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return matching;
    }
}