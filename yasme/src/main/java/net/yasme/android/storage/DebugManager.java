package net.yasme.android.storage;

import android.os.Environment;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import net.yasme.android.entities.MessageKey;
import net.yasme.android.entities.OwnDevice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by martin on 04.08.2014.
 */
public enum DebugManager {
    INSTANCE;

    private final String OWNDEVICE = "owndevice";
    private final String MESSAGEKEYS = "messagekeys";

    private boolean debugMode = false;

    public boolean isDebugMode() {
        return debugMode;
    }

    public boolean storeOwnDeviceToExternalStorage(OwnDevice data) {
        return storeToExternalStorage(OWNDEVICE, data, false);
    }

    public boolean storeMessageKeyToExternalStorage(MessageKey data) {
        return storeToExternalStorage(MESSAGEKEYS, data, true);
    }

    private boolean storeToExternalStorage(String name, Object data, boolean append) {
        try {
            String state = Environment.getExternalStorageState();
            Log.d(getClass().getSimpleName(), "Check state");
            if (!Environment.MEDIA_MOUNTED.equals(state)) {
                return false;
            }
            Log.d(getClass().getSimpleName(), "Open dir");
            File dir = getDir();
            Log.d(getClass().getSimpleName(), "Mkdir");
            if (!dir.exists() && !dir.mkdirs()) {
                Log.d(getClass().getSimpleName(), "... failed");
               return false;
            }
            Log.d(getClass().getSimpleName(), "Open file");
            File file = new File(dir,name + ".txt");
            Log.d(getClass().getSimpleName(), "Write file");

            ObjectWriter objectWriter = new ObjectMapper().writer();
            String json = objectWriter.writeValueAsString(data);
            Log.d(getClass().getSimpleName(),"Generated JSON: " + json);

            FileOutputStream f = new FileOutputStream(file,append);
            PrintWriter pw = new PrintWriter(f);
            //PrintWriter pw2 =  new PrintWriter(new BufferedWriter(new FileWriter(f, true)));
            pw.println(json);
            pw.flush();
            pw.close();
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public OwnDevice getOwnDeviceFromExternalStorage() {
        String text = readText(OWNDEVICE);
        if (text == null) {
            return null;
        }
        Log.d(getClass().getSimpleName(), "Device-Text: " + text);
        try {
            OwnDevice device = new ObjectMapper().readValue(text, OwnDevice.class);
            Log.d(getClass().getSimpleName(), "PubKey: " + device.getPublicKey());
            return device;
        } catch (Exception e) {
            return null;
        }
    }

    public List<MessageKey> getMessageKeysFromExternalStorage() {
        String text = readText(MESSAGEKEYS);
        if (text == null) {
            return new ArrayList<>();
        }
        Log.d(getClass().getSimpleName(), "MessageKeys-Text: " + text);
        try {
            //OwnDevice device = new ObjectMapper().readValue(text, OwnDevice.class);
            //return device;
            return new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String readText(String name) {
        try {
            Log.d(getClass().getSimpleName(), "Open dir");
            File dir = getDir();
            if (!dir.exists()) {
                return null;
            }
            Log.d(getClass().getSimpleName(), "Open file");
            String filename = dir.getAbsolutePath() + "/" + name + ".txt";

            BufferedReader br = new BufferedReader(new FileReader(filename));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            br.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private File getDir() {
        return new File(Environment.getExternalStoragePublicDirectory("yasme"), String.valueOf(DatabaseManager.INSTANCE.getUserId()));
    }
}
