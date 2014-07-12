package net.yasme.android.storage.dao;

import android.util.Log;

import net.yasme.android.entities.Chat;
import net.yasme.android.entities.User;
import net.yasme.android.storage.ChatUser;
import net.yasme.android.storage.DatabaseConstants;
import net.yasme.android.storage.DatabaseHelper;
import net.yasme.android.storage.DatabaseManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;

/**
 * Created by bene on 11.07.14.
 */
public enum ChatDAOImpl implements ChatDAO {
    INSTANCE;

    private DatabaseHelper databaseHelper;

    public void setDatabaseHelper(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    @Override
    public Chat addIfNotExists(Chat chat) {
        try {
            Chat ret = databaseHelper.getChatDao().createIfNotExists(chat);
            // Add participants
            for (User participant : chat.getParticipants()) {
                databaseHelper.getUserDao().createIfNotExists(participant);
                databaseHelper.getChatUserDao().create(new ChatUser(chat, participant));
            }
            return ret;
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    @Override
    public Chat addOrUpdate(Chat chat) {
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
            ret.setParticipants(loadParticipants(ret));
            return ret;
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }


    private List<User> loadParticipants(Chat chat) throws SQLException {
        // If a chat object was found, fill it's participants list
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
    public List<Chat> getByParticipants(List<User> users) {
        // TODO
        return getAll();
    }

    @Override
    public Chat update(Chat chat) {
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
