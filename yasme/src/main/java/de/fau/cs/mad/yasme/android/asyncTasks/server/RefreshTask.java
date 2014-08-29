package de.fau.cs.mad.yasme.android.asyncTasks.server;

import android.os.AsyncTask;
import de.fau.cs.mad.yasme.android.controller.Log;

import de.fau.cs.mad.yasme.android.connection.ChatTask;
import de.fau.cs.mad.yasme.android.connection.DeviceTask;
import de.fau.cs.mad.yasme.android.controller.SpinnerObservable;
import de.fau.cs.mad.yasme.android.entities.Chat;
import de.fau.cs.mad.yasme.android.entities.Device;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;
import de.fau.cs.mad.yasme.android.storage.dao.ChatDAO;
import de.fau.cs.mad.yasme.android.storage.dao.DeviceDAO;
import de.fau.cs.mad.yasme.android.storage.dao.UserDAO;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Martin Sturm <martin@sturms.name> on 26.07.2014.
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
        SpinnerObservable.getInstance().registerBackgroundTask(this);
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
        SpinnerObservable.getInstance().removeBackgroundTask(this);
        if (!success) {
            Log.e(this.getClass().getSimpleName(), "failed");
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
            Log.e(this.getClass().getSimpleName(),e.getMessage());
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
            Log.e(this.getClass().getSimpleName(),e.getMessage());
            return false;
        }
    }

    private boolean refreshUserRecursive(long id) {
        Log.d(this.getClass().getSimpleName(), "Refresh userRec " + id);
        try {
            List<Device> devices = DeviceTask.getInstance().getAllDevices(id);
            deviceDAO.deleteAll(new User(id));
            for (Device device : devices) {
                device.getUser().addToContacts();
                deviceDAO.addOrUpdate(device);
            }
            Log.d(this.getClass().getSimpleName(), "... successful" + id);
            return true;
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "... failed" + id);
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
            Log.e(this.getClass().getSimpleName(), "... failed" + id);
            return false;
        }
    }
}