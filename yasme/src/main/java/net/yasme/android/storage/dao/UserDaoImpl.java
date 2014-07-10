package net.yasme.android.storage.dao;

import android.util.Log;

import net.yasme.android.entities.User;
import net.yasme.android.storage.DatabaseConstants;
import net.yasme.android.storage.DatabaseHelper;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by bene on 10.07.14.
 */
public enum UserDaoImpl implements UserDao {
    INSTANCE;

    private DatabaseHelper databaseHelper;

    public void setDatabaseHelper(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    @Override
    public User add(User user) {
        try {
            user = databaseHelper.getUserDao().createIfNotExists(user);
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
        return user;
    }

    @Override
    public User addOrUpdate(User user) {
        try {
            User fromDatabase = databaseHelper.getUserDao().queryForId(user.getId());
            if(fromDatabase == null) {
                return databaseHelper.getUserDao().createIfNotExists(user);
            } else {
                if (fromDatabase.isContact() == 1) {
                    user.addToContacts();   // Make sure the contact flag remains 1
                }
                databaseHelper.getUserDao().update(user);
                return user;
            }
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    @Override
    public User update(User user) {
        try {
            int ret = databaseHelper.getUserDao().update(user);
            if (ret != 1) {
                // Nothing changed
            }
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }

        return user;
    }

    @Override
    public User get(long id) {
        try {
            return databaseHelper.getUserDao().queryForId(id);
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    @Override
    public List<User> getAll() {
        try {
            return databaseHelper.getUserDao().queryForAll();
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    @Override
    public List<User> getContacts() {
        try {
            return databaseHelper.getUserDao().queryForEq(DatabaseConstants.CONTACT, 1);
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    @Override
    public boolean removeFromContacts(User user) {
        try {
            user.removeFromContacts();
            int affectedRows = databaseHelper.getUserDao().update(user);
            if (0 == affectedRows) {
                // Nothing changed
                return false;
            }
            return true;
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(User user) {
        try {
            databaseHelper.getUserDao().delete(user);
            return true;
        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }
}
