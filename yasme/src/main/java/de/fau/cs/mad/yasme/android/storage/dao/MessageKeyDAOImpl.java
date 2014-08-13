package de.fau.cs.mad.yasme.android.storage.dao;

import de.fau.cs.mad.yasme.android.controller.Log;

import de.fau.cs.mad.yasme.android.entities.Chat;
import de.fau.cs.mad.yasme.android.entities.MessageKey;
import de.fau.cs.mad.yasme.android.storage.DatabaseConstants;
import de.fau.cs.mad.yasme.android.storage.DatabaseHelper;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by robert on 11.07.14.
 */
public enum MessageKeyDAOImpl implements MessageKeyDAO {
    INSTANCE;

    private DatabaseHelper databaseHelper;

    public void setDatabaseHelper(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }


    @Override
    public MessageKey addIfNotExists(MessageKey messageKey) {
        MessageKey returnMessageKey;
        try {
            returnMessageKey = databaseHelper.getMessageKeyDao().createIfNotExists(messageKey);
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
        return returnMessageKey;
    }

    @Override
    public MessageKey addOrUpdate(MessageKey messageKey) {
        try {
            MessageKey fromDb = databaseHelper.getMessageKeyDao().queryForId(messageKey.getId());
            if (null == fromDb) {
                return addIfNotExists(messageKey);
            } else {
                return update(messageKey);
            }
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    @Override
    public MessageKey get(long id) {
        MessageKey messageKey;
        try {
            messageKey = databaseHelper.getMessageKeyDao().queryForId(id);
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
        return messageKey;
    }

    @Override
    public List<MessageKey> getAll() {
        List<MessageKey> messageKeyList;
        try {
            messageKeyList = databaseHelper.getMessageKeyDao().queryForAll();
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
        return messageKeyList;
    }

    @Override
    public List<MessageKey> getMessageKeysByChat(long chatId) {
        Chat chat = new Chat();
        chat.setId(chatId);
        List<MessageKey> matching;
        try {
            matching = databaseHelper.getMessageKeyDao().queryForEq(DatabaseConstants.KEY_CHAT, chat);
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
        Log.d(getClass().getSimpleName(), "Found " + matching.size() + " messageKeys for chat " + chatId);
        return matching;
    }

    @Override
    public MessageKey getCurrentKeyByChat(long chatId) {
        try {
            MessageKey result = null;
            for (MessageKey key : getMessageKeysByChat(chatId)) {
                if (result == null) {
                    result = key;
                }
                if (result.getCreated() == null || key.getCreated() == null) {
                    Log.d(getClass().getSimpleName(), "Date was not set");
                    continue;
                }
                if (result.getCreated().compareTo(key.getCreated()) < 0) {
                    result = key;
                }
            }
            return result;
        } catch (Exception e) {
            Log.d(getClass().getSimpleName(), "Exception:");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public MessageKey update(MessageKey messageKey) {
        try {
            if(1 == databaseHelper.getMessageKeyDao().update(messageKey)) {
                return messageKey;
            }
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
        return null;
    }

    @Override
    public boolean delete(MessageKey messageKey) {
        try {
            return (1 == databaseHelper.getMessageKeyDao().delete(messageKey));
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }
}
