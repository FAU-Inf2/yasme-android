package net.yasme.android.storage;

import android.content.Context;
import android.content.SharedPreferences;

import net.yasme.android.controller.NewMessageNotificationManager;
import net.yasme.android.storage.dao.ChatDAO;
import net.yasme.android.storage.dao.ChatDAOImpl;
import net.yasme.android.storage.dao.DeviceDAO;
import net.yasme.android.storage.dao.DeviceDAOImpl;
import net.yasme.android.storage.dao.MessageDAO;
import net.yasme.android.storage.dao.MessageDAOImpl;
import net.yasme.android.storage.dao.MessageKeyDAO;
import net.yasme.android.storage.dao.MessageKeyDAOImpl;
import net.yasme.android.storage.dao.UserDAO;
import net.yasme.android.storage.dao.UserDAOImpl;
import net.yasme.android.ui.AbstractYasmeActivity;

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
    private long mUserId = -1;
    private long mDeviceId = -1;
    private String mAccessToken = null;
    private SharedPreferences mSharedPreferences;
    private String mUserEmail;
    private NewMessageNotificationManager notifier = null;

    private UserDAO userDAO;
    private ChatDAO chatDAO;
    private MessageDAO messageDAO;
    private MessageKeyDAO messageKeyDAO;
    private DeviceDAO deviceDAO;


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
        deviceDAO = DeviceDAOImpl.INSTANCE;
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

    public DeviceDAO getDeviceDAO() {
        return deviceDAO;
    }

    public long getUserId() {
        if (-1 == mUserId) {
            mUserId = getSharedPreferences().getLong(AbstractYasmeActivity.USER_ID, -1);
        }
        return mUserId;
    }

    public void setUserId(long mUserId) {
        this.mUserId = mUserId;
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putLong(AbstractYasmeActivity.USER_ID, mUserId);
        editor.commit();
    }

    public long getDeviceId() {
        if (-1 == mDeviceId) {
            mDeviceId = getSharedPreferences().getLong(AbstractYasmeActivity.DEVICE_ID, -1);
        }
        return mDeviceId;
    }

    public void setDeviceId(long mDeviceId) {
        this.mDeviceId = mDeviceId;
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putLong(AbstractYasmeActivity.DEVICE_ID, mDeviceId);
        editor.commit();
    }

    public String getAccessToken() {
        if (null == mAccessToken) {
            mAccessToken = getSharedPreferences().getString(AbstractYasmeActivity.ACCESSTOKEN, null);
        }
        return mAccessToken;
    }

    public void setAccessToken(String accessToken) {
        this.mAccessToken = accessToken;
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(AbstractYasmeActivity.ACCESSTOKEN, mAccessToken);
        editor.commit();
    }

    public String getUserEmail() {
        if (null == mUserEmail || "" == mUserEmail) {
            mUserEmail = getSharedPreferences().getString(AbstractYasmeActivity.USER_MAIL, null);
        }
        return mUserEmail;
    }

    public void setUserEmail(String mUserEmail) {
        this.mUserEmail = mUserEmail;
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(AbstractYasmeActivity.USER_MAIL, mUserEmail);
        editor.commit();
    }

    public NewMessageNotificationManager getNotifier() {
        if(notifier == null) {
            return new NewMessageNotificationManager();
        }
        return notifier;
    }
}