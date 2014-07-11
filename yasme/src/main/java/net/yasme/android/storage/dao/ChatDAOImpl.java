package net.yasme.android.storage.dao;

import android.util.Log;

import net.yasme.android.entities.Chat;
import net.yasme.android.entities.User;
import net.yasme.android.storage.ChatUser;
import net.yasme.android.storage.DatabaseHelper;

import java.sql.SQLException;
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
    public Chat add(Chat chat) {
        try {
            Chat ret = databaseHelper.getChatDao().createIfNotExists(chat);
            // Add participants
            for (User participant : chat.getParticipants()) {
                databaseHelper.getChatUserDao().create(new ChatUser(chat, participant));
            }
            return ret;
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    @Override
    public Chat get(long id) {
        try {
            return databaseHelper.getChatDao().queryForId(id);
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    @Override
    public List<Chat> getAll() {
        try {
            return databaseHelper.getChatDao().queryForAll();
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    @Override
    public Chat update(Chat chat) {
        try {
            int ret = databaseHelper.getChatDao().update(chat);
            if (ret != 1) {
                // Nothing changed
            }
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }

        return chat;
    }

    @Override
    public boolean delete(Chat chat) {
        try {
            databaseHelper.getChatDao().delete(chat);
            return true;
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }


}
