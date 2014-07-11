package net.yasme.android.storage.dao;

import android.util.Log;

import net.yasme.android.entities.Chat;
import net.yasme.android.entities.MessageKey;
import net.yasme.android.storage.CurrentKey;
import net.yasme.android.storage.DatabaseConstants;
import net.yasme.android.storage.DatabaseHelper;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by robert on 11.07.14.
 */
public enum CurrentKeyDAOImpl implements CurrentKeyDAO{
    INSTANCE;

    private DatabaseHelper databaseHelper;

    public void setDatabaseHelper(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    @Override
    public CurrentKey add(CurrentKey currentKey) {
        CurrentKey returnCurrentKey;
        try {
            returnCurrentKey = databaseHelper.getCurrentKeyDao().createIfNotExists(currentKey);
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
        return returnCurrentKey;
    }

    @Override
    public CurrentKey addOrUpdate(CurrentKey currentKey) {
        try {
            CurrentKey fromDb = databaseHelper.getCurrentKeyDao().queryForId(currentKey.getId());
            if (null == fromDb) {
                return add(currentKey);
            } else {
                return update(currentKey);
            }
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    @Override
    public boolean existsCurrentKeyForChat(long chatId) {
        //TODO: effektiver machen, evtl. SELECT
        List<CurrentKey> currentKeys = null;
        Chat chat = new Chat();
        chat.setId(chatId);
        try {
            currentKeys = databaseHelper.getCurrentKeyDao().queryForEq(DatabaseConstants.CURRENT_KEY_CHAT, chat);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(currentKeys.size() != 1) {
            Log.e(this.getClass().getSimpleName(), "Mehrere currentKeys pro Chat");
        }
        return (currentKeys.size() != 0);
    }

    @Override
    public CurrentKey get(long id) {
        CurrentKey currentKey;
        try {
            currentKey = databaseHelper.getCurrentKeyDao().queryForId(id);
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
        return currentKey;
    }

    @Override
    public List<CurrentKey> getAll() {
        List<CurrentKey> currentKeyList;
        try {
            currentKeyList = databaseHelper.getCurrentKeyDao().queryForAll();
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
        return currentKeyList;
    }

    @Override
    public List<CurrentKey> getCurrentKeysByChat(long chatId) {
        List<CurrentKey> currentKeys = null;
        Chat chat = new Chat();
        chat.setId(chatId);
        try {
            currentKeys = databaseHelper.getCurrentKeyDao().queryForEq(DatabaseConstants.CURRENT_KEY_CHAT, chat);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }
        if(currentKeys.size() != 1) {
            Log.e(this.getClass().getSimpleName(), "Mehrere currentKeys pro Chat");
            return null;
        }
        return currentKeys;//.get(0).getMessageKey().getId();
    }

    @Override
    public CurrentKey update(CurrentKey currentKey) {
        try {
            if (1 == databaseHelper.getCurrentKeyDao().update(currentKey)) {
                return currentKey;
            }
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
        return null;
    }

    @Override
    public void updateCurrentKey(long keyId, long chatId) {
        Chat chat = new Chat();
        chat.setId(chatId);
        MessageKey messageKey = new MessageKey();
        messageKey.setId(keyId);
        CurrentKey newCurrentKey = new CurrentKey(chat, messageKey);
        try {
            databaseHelper.getCurrentKeyDao().update(newCurrentKey);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean delete(CurrentKey currentKey) {
        try {
            return (1 == databaseHelper.getCurrentKeyDao().delete(currentKey));
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }
}
