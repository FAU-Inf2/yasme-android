package net.yasme.android.storage;

import android.content.Context;
import android.content.SharedPreferences;

import net.yasme.android.storage.dao.ChatDAO;
import net.yasme.android.storage.dao.ChatDAOImpl;
import net.yasme.android.storage.dao.MessageDAO;
import net.yasme.android.storage.dao.MessageDAOImpl;
import net.yasme.android.storage.dao.MessageKeyDAO;
import net.yasme.android.storage.dao.MessageKeyDAOImpl;
import net.yasme.android.storage.dao.DeviceDAO;
import net.yasme.android.storage.dao.DeviceDAOImpl;
import net.yasme.android.storage.dao.UserDAO;
import net.yasme.android.storage.dao.UserDAOImpl;

//import net.yasme.android.storage.dao.CurrentKeyDAO;
//import net.yasme.android.storage.dao.CurrentKeyDAOImpl;

/**
 * Created by robert on 13.06.14.
 */
public enum DatabaseManager {
    INSTANCE;

    private boolean mInitialized = false;
    private DatabaseHelper mHelper;
    private Context mContext;
    private long mUserId;
    private SharedPreferences mSharedPreferences;

    private UserDAO userDAO;
    private ChatDAO chatDAO;
    private MessageDAO messageDAO;
    private MessageKeyDAO messageKeyDAO;
    private DeviceDAO rsaKeyDAO;

    public void init(Context context, SharedPreferences sharedPreferences, long userId) {
        mContext = context;
        mSharedPreferences = sharedPreferences;
        mUserId = userId;
        mHelper = new DatabaseHelper(context, userId);
        initializeDAOs();
        mInitialized = true;
    }

    public boolean isInitialized() {
        return mInitialized;
    }


    private DatabaseHelper getHelper() {
        return mHelper;
    }

    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    public Context getContext() {
        return mContext;
    }

    private void initializeDAOs() {
        UserDAOImpl.INSTANCE.setDatabaseHelper(mHelper);
        userDAO = UserDAOImpl.INSTANCE;

        ChatDAOImpl.INSTANCE.setDatabaseHelper(mHelper);
        chatDAO = ChatDAOImpl.INSTANCE;

        MessageDAOImpl.INSTANCE.setDatabaseHelper(mHelper);
        messageDAO = MessageDAOImpl.INSTANCE;

        MessageKeyDAOImpl.INSTANCE.setDatabaseHelper(mHelper);
        messageKeyDAO = MessageKeyDAOImpl.INSTANCE;

        DeviceDAOImpl.INSTANCE.setDatabaseHelper(mHelper);
        rsaKeyDAO = DeviceDAOImpl.INSTANCE;
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }

    public ChatDAO getChatDAO() { return chatDAO; }

    public MessageDAO getMessageDAO() {
        return messageDAO;
    }

    public MessageKeyDAO getMessageKeyDAO() {
        return messageKeyDAO;
    }

    public DeviceDAO getRsaKeyDAO() {
        return rsaKeyDAO;
    }
}