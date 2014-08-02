package net.yasme.android.asyncTasks.server;

import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.connection.ChatTask;
import net.yasme.android.connection.DeviceTask;
import net.yasme.android.connection.UserTask;
import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.controller.SpinnerObservable;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Device;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.storage.dao.ChatDAO;
import net.yasme.android.storage.dao.DeviceDAO;
import net.yasme.android.storage.dao.UserDAO;
import net.yasme.android.ui.fragments.ChatListFragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by martin on 26.07.2014.
 */
public class RefreshTask extends AsyncTask<String, Void, Boolean> {

    private DatabaseManager dbManager;
    private UserDAO userDAO;
    private ChatDAO chatDAO;
    private DeviceDAO deviceDAO;

    private RefreshType type;
    private Set<Long> ids;
    private boolean recursive;

    public enum RefreshType {
        CHAT,
        USER,
        DEVICE;
    }

    public RefreshTask(RefreshType type, long id, boolean recursive) {
        this(type,recursive);
        ids = new HashSet<>();
        ids.add(id);
    }

    public RefreshTask(RefreshType type, Set<Long> ids, boolean recursive) {
        this(type,recursive);
        this.ids = ids;
    }

    public RefreshTask(RefreshType type, boolean recursive) {
        dbManager = DatabaseManager.INSTANCE;
        userDAO = DatabaseManager.INSTANCE.getUserDAO();
        chatDAO = DatabaseManager.INSTANCE.getChatDAO();
        deviceDAO = DatabaseManager.INSTANCE.getDeviceDAO();

        this.type = type;
        this.recursive = recursive;
    }
    /**
     * Requests the user's chats from the server and updates the database.
     * @return Returns true if it was successful, otherwise false
     */
    protected Boolean doInBackground(String... params) {
        //SpinnerObservable.getInstance().registerBackgroundTask(this);
        boolean result = true;
        Log.d(this.getClass().getSimpleName(), "Result " + result);
        for (long id : ids) {
            Log.d(this.getClass().getSimpleName(), "Refresh id " + id);
            switch (type) {
                case CHAT:
                    if (recursive) {
                        result &= refreshChatRecursive(id);
                        Log.d(this.getClass().getSimpleName(), "Result " + result);
                    } else {
                        result &= refreshChat(id);
                        Log.d(this.getClass().getSimpleName(), "Result " + result);
                    }
                    break;
                case USER:
                    if (recursive) {
                        result &= refreshUserRecursive(id);
                        Log.d(this.getClass().getSimpleName(), "Result " + result);
                    } else {
                        result &= refreshUser(id);
                        Log.d(this.getClass().getSimpleName(), "Result " + result);
                    }
                    break;
                case DEVICE:
                    result &= refreshDevice(id);
                    Log.d(this.getClass().getSimpleName(), "Result " + result);
                    break;
                default:
                    result &= false;
                    break;
            }
        }
        return result;
    }


    @Override
    protected void onPostExecute(final Boolean success) {
        //SpinnerObservable.getInstance().removeBackgroundTask(this);
        if (!success) {
            Log.w(this.getClass().getSimpleName(), "failed");
            return;
        }

        Log.i(this.getClass().getSimpleName(), "success");
    }

    private boolean refreshChatRecursive(long id) {
        Log.d(this.getClass().getSimpleName(), "Refresh chatRec " + id);
        try {
            Chat chat = ChatTask.getInstance().getInfoOfChat(id);
            chatDAO.addOrUpdate(chat);
            for (User user : chat.getParticipants()) {
                refreshUserRecursive(user.getId());
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean refreshChat(long id) {
        Log.d(this.getClass().getSimpleName(), "Refresh chat " + id);
        try {
            Chat chat = ChatTask.getInstance().getInfoOfChat(id);
            chatDAO.addOrUpdate(chat);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean refreshUserRecursive(long id) {
        Log.d(this.getClass().getSimpleName(), "Refresh userRec " + id);
        try {
            List<Device> devices = DeviceTask.getInstance().getAllDevices(id);
            deviceDAO.deleteAll(new User(id));
            for (Device device : devices) {
                deviceDAO.addOrUpdate(device);
            }
            Log.d(this.getClass().getSimpleName(), "... successful" + id);
            return true;
        } catch (Exception e) {
            Log.d(this.getClass().getSimpleName(), "... failed" + id);
            e.printStackTrace();
            return false;
        }
    }

    private boolean refreshUser(long id) {
        Log.d(this.getClass().getSimpleName(), "Refresh user " + id);
        // Nothing to do
        return true;
    }

    private boolean refreshDevice(long id) {
        Log.d(this.getClass().getSimpleName(), "Refresh device " + id);
        try {
            Device device = DeviceTask.getInstance().getDevice(id);
            deviceDAO.addOrUpdate(device);
            Log.d(this.getClass().getSimpleName(), "... successful" + id);
            return true;
        } catch (Exception e) {
            Log.d(this.getClass().getSimpleName(), "... failed" + id);
            e.printStackTrace();
            return false;
        }
    }
}