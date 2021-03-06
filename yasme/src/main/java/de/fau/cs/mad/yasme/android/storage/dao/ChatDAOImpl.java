package de.fau.cs.mad.yasme.android.storage.dao;

import de.fau.cs.mad.yasme.android.controller.Log;

import com.j256.ormlite.dao.GenericRawResults;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.fau.cs.mad.yasme.android.entities.Chat;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.storage.ChatUser;
import de.fau.cs.mad.yasme.android.storage.DatabaseConstants;
import de.fau.cs.mad.yasme.android.storage.DatabaseHelper;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;

/**
 * Created by Benedikt Lorch <benedikt.lorch@studium.fau.de> on 11.07.14.
 */
public enum ChatDAOImpl implements ChatDAO {
    INSTANCE;

    private DatabaseHelper databaseHelper;

    public void setDatabaseHelper(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    @Override
    public Chat addIfNotExists(Chat chat) {
        if (null == chat || chat.getId() < 0) {
            throw new IllegalArgumentException("Chat is null or id < 0");
        }
        try {
            Chat ret = databaseHelper.getChatDao().createIfNotExists(chat);
            // Add participants
            for (User participant : chat.getParticipants()) {
                databaseHelper.getUserDao().createIfNotExists(participant);

                ChatUser addParticipantIfNotExists = new ChatUser(ret, participant);
                List<ChatUser> matching;
                if ((matching = databaseHelper.getChatUserDao().queryForMatching(addParticipantIfNotExists)) == null || matching.size() == 0) {
                    databaseHelper.getChatUserDao().create(addParticipantIfNotExists);
                }
            }
            return ret;
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    @Override
    public Chat addOrUpdate(Chat chat) {
        if (null == chat || chat.getId() < 0) {
            throw new IllegalArgumentException("Chat is null or id < 0");
        }
        try {
            Chat fromDb = databaseHelper.getChatDao().queryForId(chat.getId());
            if (null == fromDb) {
                return addIfNotExists(chat);
            } else {
                return update(chat);
            }
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    @Override
    public Chat get(long id) {
        try {
            Chat ret = databaseHelper.getChatDao().queryForId(id);
            if(ret == null) {
                return null;
            }
            ret.setParticipants(loadParticipants(ret));
            return ret;
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    private List<User> loadParticipants(Chat chat) throws SQLException {
        // If a chat object was found, fill it's participants list
        if(chat == null) {
            Log.e(this.getClass().getSimpleName(), "Chat is null");
            return null;
        }
        List<ChatUser> participantsByChat = databaseHelper.getChatUserDao().queryForEq(DatabaseConstants.CHAT_FIELD_NAME, chat.getId());
        if (null != participantsByChat && !participantsByChat.isEmpty()) {
            // Get only the users from the chatUser objects
            List<User> participants = new ArrayList<>();
            for (ChatUser chatUser : participantsByChat) {
                DatabaseManager.INSTANCE.getUserDAO().get(chatUser.getUser().getId());
                participants.add(DatabaseManager.INSTANCE.
                        getUserDAO().get(chatUser.getUser().getId()));
            }
            return participants;
        }
        return null;
    }

    @Override
    public List<Chat> getAll() {
        try {
            List<Chat> chats = databaseHelper.getChatDao().queryForAll();
            if (null != chats && !chats.isEmpty()) {
                for (Chat chat : chats) {
                    chat.setParticipants(loadParticipants(chat));
                }
            }
            return chats;
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }


    @Override
    public List<Chat> getByParticipantsExact(Set<User> users) {
        List<Chat> theseParticipantsOrMore = getByTheseParticipantsOrMore(users);
        List<Chat> exactMatch = new ArrayList<>();
        if (null != theseParticipantsOrMore && theseParticipantsOrMore.size() > 0) {
            for (Chat chat : theseParticipantsOrMore) {
                Log.d(this.getClass().getSimpleName(), "Chat " + chat.getId() + " has " + chat.getParticipants().toString() + " participants");
                if (null == chat.getParticipants() || chat.getParticipants().size() < 2) {
                    Log.e(this.getClass().getSimpleName(), "Chat " + chat.getId() + " has no or less than 2 participants?!");
                } else {
                    if (chat.getParticipants().size() == users.size()) {
                        exactMatch.add(chat);
                    }
                }
            }
        }
        return exactMatch;
    }


    @Override
    public List<Chat> getByTheseParticipantsOrMore(Set<User> users) {
        final String count = "hitcount";

        StringBuilder conditionBuilder = new StringBuilder();
        Iterator<User> iterator = users.iterator();
        while(iterator.hasNext()) {
            User user = iterator.next();
            conditionBuilder.append(DatabaseConstants.USER_FIELD_NAME + " = " + user.getId());
            if (iterator.hasNext()) {
                conditionBuilder.append(" OR ");
            }
        }

        String query = "SELECT " + DatabaseConstants.CHAT_FIELD_NAME + ", count(*) AS " + count + " FROM " + DatabaseConstants.CHAT_USER_TABLE + " WHERE " + conditionBuilder.toString() + " GROUP BY " + DatabaseConstants.CHAT_FIELD_NAME + ";"; // wow :S
        Log.d(this.getClass().getSimpleName(), "Get by participants query: " + query);


        GenericRawResults<String[]> result = null;
        try {
            result = databaseHelper.getChatUserDao().queryRaw(query);

            List<String[]> matches = (List<String[]>) result.getResults();
            List<Chat> chats = new ArrayList<Chat>();
            for (String[] match : matches) {
                // match[0] chatId, match[1] number of matching participants
                if (Integer.valueOf(match[1]) >= users.size()) {
                    // All participants found
                    Chat get = get(Long.valueOf(match[0]));
                    if (null == get) {
                        // Null should not happen actually
                        Log.e(this.getClass().getSimpleName(), "A chat (id=" + match[0] + ") which contains the given users (and more) could not be retrieved from the database");
                    }
                    else {
                        chats.add(get);
                    }
                }
            }
            return chats;
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
        }
        return null;
    }

    @Override
    public Chat update(Chat chat) {
        if (null == chat || chat.getId() < 0) {
            throw new IllegalArgumentException("Chat is null or id < 0");
        }
        try {
            List<ChatUser> dbParticipants = databaseHelper.getChatUserDao().queryForEq(DatabaseConstants.CHAT_FIELD_NAME, chat.getId());

            // Determine difference between dbParticipants and chat's participants

            // 1. Chat should not have any participants?
            if (null == chat.getParticipants() || chat.getParticipants().isEmpty()) {

                // Remove all participants if there have been any at all
                if (null != dbParticipants && !dbParticipants.isEmpty()) {
                    Collection<ChatUser> rowsToRemove = databaseHelper.getChatUserDao().queryForEq(DatabaseConstants.CHAT_FIELD_NAME, chat.getId());
                    if (null != rowsToRemove && !rowsToRemove.isEmpty()) {
                        int affectedRows = databaseHelper.getChatUserDao().delete(rowsToRemove);
                        if (affectedRows != rowsToRemove.size()) {
                            // Something went wrong
                            throw new SQLException("Number of deleted rows and number of rows to be deleted do not match.");
                        }
                    }
                }
            }

            // 2. New chat object contains a list of participants, but there are none in the database yet
            else if ((null == dbParticipants || dbParticipants.isEmpty()) && (null != chat.getParticipants() && !chat.getParticipants().isEmpty())) {
                // Insert them all
                for (User participant : chat.getParticipants()) {
                    databaseHelper.getUserDao().createIfNotExists(participant);
                    databaseHelper.getChatUserDao().create(new ChatUser(chat, participant));
                }
            }

            // 3. New chat object has some participants, some of whom have already been added to the database
            BitSet formerParticipantsStillThere = new BitSet(dbParticipants.size());
            for (User nowParticipant : chat.getParticipants()) {
                // If the "new" participant has already been a participant, mark him as seen
                boolean found = false;
                int i=0;
                for (; i<dbParticipants.size() && !found; i++) {
                    // Only compare ids. Objects may not be equal
                    found |= nowParticipant.getId() == dbParticipants.get(i).getUser().getId();
                }
                if (found) {
                    formerParticipantsStillThere.set(i-1);
                } else {
                    // nowParticipant not found in list of former participants => Insert him
                    databaseHelper.getUserDao().createIfNotExists(nowParticipant);
                    databaseHelper.getChatUserDao().create(new ChatUser(chat, nowParticipant));
                }
            }

            // All former participants which have not been visited during former iteration, you're out
            int next = 0;
            int size = dbParticipants.size();
            int cardinality = formerParticipantsStillThere.cardinality();
            for (int i=0; i<size - cardinality; i++) {
                int indexToBeRemoved = formerParticipantsStillThere.nextClearBit(next);
                databaseHelper.getChatUserDao().delete(dbParticipants.get(indexToBeRemoved));
                next = indexToBeRemoved + 1;
            }

            // if lastMessage is null, do not overwrite
            if (chat.getLastMessage() == null) {
                Chat tmp = get(chat.getId());
                if (tmp != null && tmp.getLastMessage() != null) {
                    chat.setLastMessage(tmp.getLastMessage());
                }
            }
            databaseHelper.getChatDao().update(chat);
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }

        return chat;
    }

    @Override
    public boolean delete(Chat chat) {
        try {
            // Remove chat user relation items first
            List<ChatUser> participantsList = databaseHelper.getChatUserDao().queryForEq(DatabaseConstants.CHAT_FIELD_NAME, chat.getId());
            if (null != participantsList && participantsList.size() != 0) {
                databaseHelper.getChatUserDao().delete(participantsList);
            }
            databaseHelper.getChatDao().delete(chat);
            return true;
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }


    @Override
    public boolean refreshAll(List<Chat> newChats) {
        List<Chat> oldChats = getAll();
        // Add all new chats if we haven't had any chats at present
        if (null == oldChats || oldChats.isEmpty()) {
            // Add all
            for (Chat chat : newChats) {
                addIfNotExists(chat);
            }
            return true;
        }

        // Determine difference of new chats and old chats
        BitSet oldChatsUsed = new BitSet(oldChats.size());
        for (Chat newChat : newChats) {
            boolean found = false;
            int i=0;
            for(; i<oldChats.size() && !found; i++) {
                found |= newChat.getId() == oldChats.get(i).getId();
            }

            if (found) {
                oldChatsUsed.set(i-1);
                update(newChat);
            }
            else {
                addIfNotExists(newChat);
            }
        }

        // Remove old chats which are not present in newChats list
        int next = 0;
        int size = oldChats.size();
        int cardinality = oldChatsUsed.cardinality();
        for (int i=0; i<size - cardinality; i++) {
            int indexToBeRemoved = oldChatsUsed.nextClearBit(next);
            delete(oldChats.get(indexToBeRemoved));
            next = indexToBeRemoved + 1;
        }

        return true;
    }

}
