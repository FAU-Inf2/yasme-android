package net.yasme.android.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.j256.ormlite.dao.Dao;

import net.yasme.android.entities.Chat;

import java.sql.SQLException;

/**
 * Created by robert on 13.06.14.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    // name of the database file
    private static final String DATABASE = "net.yasme.android.DATABASE";

    // when anything changes in your database objects, we have to increase the database version
    private static final int DATABASE_VERSION = 1;

    // the DAO object for chat
    private Dao<Chat, Integer> chatDao = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database,ConnectionSource connectionSource) {
       //try {
        try {
            TableUtils.createTable(connectionSource, Chat.class);
        } catch (SQLException e) {
            System.out.println("Can't create database");
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, Chat.class, true);
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
    public Dao<Chat, Integer> getChatDao() {
        if (null == chatDao) {
            try {
                chatDao = getDao(Chat.class);
            }catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }
        return chatDao;
    }
}