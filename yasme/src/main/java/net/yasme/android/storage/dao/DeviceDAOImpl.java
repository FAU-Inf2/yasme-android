package net.yasme.android.storage.dao;

import android.util.Log;

import net.yasme.android.entities.Device;
import net.yasme.android.storage.DatabaseConstants;
import net.yasme.android.storage.DatabaseHelper;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by robert on 19.07.14.
 */
public enum DeviceDAOImpl implements DeviceDAO {
    INSTANCE;

    private DatabaseHelper databaseHelper;

    public void setDatabaseHelper(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public Device addIfNotExists(Device device){
        Device returnDevice;
        try {
            returnDevice = databaseHelper.getDeviceDao().createIfNotExists(device);
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
        return returnDevice;
    }

    public Device addOrUpdate(Device device){
        try {
            Device fromDb = databaseHelper.getDeviceDao().queryForId(device.getId());
            if (null == fromDb) {
                return addIfNotExists(device);
            } else {
                return update(device);
            }
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    public Device get(long id){
        Device device;
        try {
            device = databaseHelper.getDeviceDao().queryForId(id);
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
        return device;
    }

    public List<Device> getAll(){
        List<Device> devicesList;
        try {
            devicesList = databaseHelper.getDeviceDao().queryForAll();
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
        return devicesList;
    }

    public Device update(Device device){
        try {
            if(1 == databaseHelper.getDeviceDao().update(device)) {
                return device;
            }
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
        return null;
    }

    public boolean delete(Device device){
        try {
            return (1 == databaseHelper.getDeviceDao().delete(device));
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }
}