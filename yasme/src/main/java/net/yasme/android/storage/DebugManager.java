package net.yasme.android.storage;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

/**
 * Created by martin on 04.08.2014.
 */
public enum DebugManager {
    INSTANCE;

    private boolean debugMode = false;

    public boolean isDebugMode() {
        return debugMode;
    }

    public boolean storeToExternalStorage(String name, Object data, boolean append) {
        try {
            String dirName = getDirname();
            String state = Environment.getExternalStorageState();
            Log.d(getClass().getSimpleName(), "Check state");
            if (!Environment.MEDIA_MOUNTED.equals(state)) {
                return false;
            }
            Log.d(getClass().getSimpleName(), "Open dir");
            File storageDirectory = Environment.getExternalStoragePublicDirectory(name);
            File dir = new File(storageDirectory.getAbsolutePath() + dirName );
            Log.d(getClass().getSimpleName(), "Mkdir");
            if (!dir.mkdirs()) {
                Log.d(getClass().getSimpleName(), "... failed");
               return false;
            }
            Log.d(getClass().getSimpleName(), "Open file");
            File file = new File(dir,name + ".txt");
            Log.d(getClass().getSimpleName(), "Write file");

            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            pw.println("test");
            pw.flush();
            pw.close();
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private String getDirname() {
        return "/yasme/" + DatabaseManager.INSTANCE.getUserId();
    }
}
