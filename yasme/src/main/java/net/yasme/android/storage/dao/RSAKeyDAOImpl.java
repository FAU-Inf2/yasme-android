package net.yasme.android.storage.dao;

import android.util.Log;

import net.yasme.android.storage.DatabaseConstants;
import net.yasme.android.storage.DatabaseHelper;
import net.yasme.android.storage.RSAKey;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by robert on 19.07.14.
 */
public enum RSAKeyDAOImpl implements RSAKeyDAO{
    INSTANCE;

    private DatabaseHelper databaseHelper;

    public void setDatabaseHelper(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public RSAKey addIfNotExists(RSAKey rsaKey){
        RSAKey returnRSAKey;
        try {
            returnRSAKey = databaseHelper.getRSAKeyDao().createIfNotExists(rsaKey);
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
        return returnRSAKey;
    }

    public RSAKey addOrUpdate(RSAKey rsaKey){
        try {
            RSAKey fromDb = databaseHelper.getRSAKeyDao().queryForId(rsaKey.getId());
            if (null == fromDb) {
                return addIfNotExists(rsaKey);
            } else {
                return update(rsaKey);
            }
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    public RSAKey get(long id){
        RSAKey rsaKey;
        try {
            rsaKey = databaseHelper.getRSAKeyDao().queryForId(id);
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
        return rsaKey;
    }

    public List<RSAKey> getAll(){
        List<RSAKey> rsaKeyList;
        try {
            rsaKeyList = databaseHelper.getRSAKeyDao().queryForAll();
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
        return rsaKeyList;
    }

    public List<RSAKey> getMessageKeysByDevice(long deviceId){
        List<RSAKey> matching;
        try {
            matching = databaseHelper.getRSAKeyDao().
                    queryForEq(DatabaseConstants.RSA_KEY_DEVICE_ID, deviceId);
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
        return matching;
    }

    public RSAKey update(RSAKey rsaKey){
        try {
            if(1 == databaseHelper.getRSAKeyDao().update(rsaKey)) {
                return rsaKey;
            }
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
        return null;
    }

    public boolean delete(RSAKey rsaKey){
        try {
            return (1 == databaseHelper.getRSAKeyDao().delete(rsaKey));
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }
}
