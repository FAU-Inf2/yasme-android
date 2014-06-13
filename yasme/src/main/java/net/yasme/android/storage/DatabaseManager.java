package net.yasme.android.storage;

import android.content.Context;

/**
 * Created by robert on 13.06.14.
 */
public class DatabaseManager {

    static private DatabaseManager instance;

    static public void init(Context context) {
        if (null == instance) {
            instance = new DatabaseManager(context);
        }
    }

    static public DatabaseManager getInstance() {
        return instance;
    }

    private DatabaseHelper helper;

    private DatabaseManager(Context context) {
        helper = new DatabaseHelper(context);
    }

    private DatabaseHelper getHelper() {
        return helper;
    }
}
