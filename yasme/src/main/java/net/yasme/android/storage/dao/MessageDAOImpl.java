package net.yasme.android.storage.dao;

import android.util.Log;

import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Message;
import net.yasme.android.storage.DatabaseConstants;
import net.yasme.android.storage.DatabaseHelper;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by robert on 11.07.14.
 */
public enum MessageDAOImpl implements MessageDAO{
    INSTANCE;

    private DatabaseHelper databaseHelper;

    public void setDatabaseHelper(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    @Override
    public Message addIfNotExists(Message message) {
        try {
            databaseHelper.getMessageDao().createIfNotExists(message);
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
        return message;
    }

    @Override
    public Message addOrUpdate(Message message) {
        try {
            Message fromDb = databaseHelper.getMessageDao().queryForId(message.getId());
            if (null == fromDb) {
                return addIfNotExists(message);
            } else {
                return update(message);
            }
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    @Override
    public Message get(long id) {
        Message message;
        try {
            message = databaseHelper.getMessageDao().queryForId(id);
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
        return message;
    }

    @Override
    public List<Message> getAll() {
        List<Message> messageList;
        try {
            messageList = databaseHelper.getMessageDao().queryForAll();
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
        return messageList;
    }

    @Override
    public List<Message> getMessagesByChatAndNumberOfMessages(long chatId, int numberOfMessages) {
        Chat chat = new Chat();
        chat.setId(chatId);
        List<Message> matching;
        try {
            matching = databaseHelper.getMessageDao().queryForEq(DatabaseConstants.CHAT, chat);
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
        //if(matching.size()-numberOfMessages<=0) {
        //    Log.d(this.getClass().getSimpleName(), "falsche Zahlen");
        //    return null;
        //}
        return matching.subList(numberOfMessages, matching.size());
        //return matching;
    }

    @Override
    public Message update(Message message) {
        try {
            if(1 == databaseHelper.getMessageDao().update(message)) {
                return message;
            }
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
        return null;
    }

    @Override
    public boolean delete(Message message) {
        try {
            return (1 == databaseHelper.getMessageDao().delete(message));
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }
}
