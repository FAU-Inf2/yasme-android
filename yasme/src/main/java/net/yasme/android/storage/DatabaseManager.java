package net.yasme.android.storage;

import android.content.Context;

import net.yasme.android.storage.dao.ChatDAO;
import net.yasme.android.storage.dao.ChatDAOImpl;
import net.yasme.android.storage.dao.CurrentKeyDAO;
import net.yasme.android.storage.dao.CurrentKeyDAOImpl;
import net.yasme.android.storage.dao.MessageDAO;
import net.yasme.android.storage.dao.MessageDAOImpl;
import net.yasme.android.storage.dao.MessageKeyDAO;
import net.yasme.android.storage.dao.MessageKeyDAOImpl;
import net.yasme.android.storage.dao.UserDAO;
import net.yasme.android.storage.dao.UserDAOImpl;

/**
 * Created by robert on 13.06.14.
 */
public enum DatabaseManager {
    INSTANCE;

    private boolean mInitialized = false;
    private DatabaseHelper mHelper;
    private Context mContext;
    private long mUserId;

    private UserDAO userDAO;
    private ChatDAO chatDAO;
    private MessageDAO messageDAO;
    private MessageKeyDAO messageKeyDAO;
    private CurrentKeyDAO currentKeyDAO;

    public void init(Context context, long userId) {
        mContext = context;
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


    private void initializeDAOs() {
        UserDAOImpl.INSTANCE.setDatabaseHelper(mHelper);
        userDAO = UserDAOImpl.INSTANCE;

        ChatDAOImpl.INSTANCE.setDatabaseHelper(mHelper);
        chatDAO = ChatDAOImpl.INSTANCE;

        MessageDAOImpl.INSTANCE.setDatabaseHelper(mHelper);
        messageDAO = MessageDAOImpl.INSTANCE;

        MessageKeyDAOImpl.INSTANCE.setDatabaseHelper(mHelper);
        messageKeyDAO = MessageKeyDAOImpl.INSTANCE;

        CurrentKeyDAOImpl.INSTANCE.setDatabaseHelper(mHelper);
        currentKeyDAO = CurrentKeyDAOImpl.INSTANCE;
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

    public CurrentKeyDAO getCurrentKeyDAO() {
        return currentKeyDAO;
    }
}