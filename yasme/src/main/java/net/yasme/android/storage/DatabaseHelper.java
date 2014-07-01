package net.yasme.android.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.j256.ormlite.dao.Dao;

import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Message;
import net.yasme.android.entities.User;
import net.yasme.android.ui.AbstractYasmeActivity;

import java.sql.SQLException;

/**
 * Created by robert on 13.06.14.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    // when anything changes in your database objects, we have to increase the database version
    private static final int DATABASE_VERSION = 25;

    // name of the database file
    private static final String DATABASE = "net.yasme.android.DATABASE";

    // the DAO object for chat
    private Dao<Chat, Long> chatDao = null;

    // the DAO object for user
    private Dao<User, Long> userDao = null;
    private Dao<Message, Long> messageDao = null;
    private Dao<ChatUser, Long> chatUserDao = null;


    public DatabaseHelper(Context context, long userId) {
        super(context, DATABASE + "_" + Long.toString(userId), null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Chat.class);
            TableUtils.createTable(connectionSource, Message.class);
            TableUtils.createTable(connectionSource, User.class);
            TableUtils.createTable(connectionSource, ChatUser.class);
        } catch (SQLException e) {
            System.out.println("Can't create database");
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, Chat.class, true);
            TableUtils.dropTable(connectionSource, Message.class, true);
            TableUtils.dropTable(connectionSource, User.class, true);
            TableUtils.dropTable(connectionSource, ChatUser.class, true);
            onCreate(db, connectionSource);
        } catch (java.sql.SQLException e) {
            System.out.println("Can't drop databases");
            System.out.println(e.getMessage());
        }
    }


    /**
     * Function to access Chat data
     * from database
     *
     * @return chatDao
     */
    public Dao<Chat, Long> getChatDao() {
        if (null == chatDao) {
            try {
                chatDao = DaoManager.createDao(connectionSource, Chat.class);
            }catch (java.sql.SQLException e) {
                Log.w(this.getClass().getSimpleName(), e.getMessage());
            }
        }
        return chatDao;
    }

    public Dao<User, Long> getUserDao() {
        if(null == userDao) {
            try {
                userDao = DaoManager.createDao(connectionSource, User.class);
            } catch (SQLException e) {
                Log.w(this.getClass().getSimpleName(), e.getMessage());
            }
        }
        return userDao;
    }

    public Dao<Message, Long> getMessageDao() {
        if(null == messageDao) {
            try {
                messageDao = DaoManager.createDao(connectionSource, Message.class);
            } catch (SQLException e) {
                Log.w(this.getClass().getSimpleName(), e.getMessage());
            }
        }
        return messageDao;
    }

    public Dao<ChatUser, Long> getChatUserDao() {
        if(null == chatUserDao) {
            try {
                chatUserDao = DaoManager.createDao(connectionSource, ChatUser.class);
            } catch (SQLException e) {
                Log.w(this.getClass().getSimpleName(), e.getMessage());
            }
        }
        return chatUserDao;
    }

    @Override
    public void close() {
        super.close();
        chatDao = null;
    }
}
