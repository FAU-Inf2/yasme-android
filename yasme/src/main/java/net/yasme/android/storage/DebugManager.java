package net.yasme.android.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import net.yasme.android.entities.Message;
import net.yasme.android.entities.MessageKey;
import net.yasme.android.entities.OwnDevice;

import org.json.JSONArray;

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
    private OwnDevice ownDevice = new OwnDevice();

    public boolean isDebugMode() {
        return debugMode;
    }

    public boolean storeDeviceId(long deviceId) {
        ownDevice.setId(deviceId);
        return storeOwnDeviceToExternalStorage();
    }


    public boolean storePrivatePublicKeyToExternalStorage(String privateKey, String publicKey) {
        ownDevice.setPrivateKey(privateKey);
        ownDevice.setPublicKey(publicKey);
        return storeOwnDeviceToExternalStorage();
    }

    public boolean storePushId(String pushId) {
        ownDevice.setPushId(pushId);
        return storeOwnDeviceToExternalStorage();
    }

    private boolean storeOwnDeviceToExternalStorage() {
        return storeToExternalStorage(OWNDEVICE, ownDevice, false);
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
            if (append) {
                pw.println(json + ",");
            } else {
                pw.println(json);
            }
            pw.flush();
            pw.close();
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean restoreData() {
        Log.d(getClass().getSimpleName(),"Restoring data");
        restoreOwnDeviceFromExternalStorage();
        restoreMessageKeysFromExternalStorage();
        return true;
    }


    private boolean restoreOwnDeviceFromExternalStorage() {
         final String RSAKEY_STORAGE = "rsaKeyStorage"; //Storage for Private and Public Keys from user
         final String PRIVATEKEY = "privateKey";
         final String PUBLICKEY = "publicKey";

        try {
            OwnDevice device = getOwnDeviceFromExternalStorage();
            if (device == null) {
                return false;
            }

            String RSAKEY_STORAGE_USER = RSAKEY_STORAGE + "_" + ownDevice.getId();
            Context context = DatabaseManager.INSTANCE.getContext();
            SharedPreferences privKeyStorage = context.getSharedPreferences(RSAKEY_STORAGE_USER, Context.MODE_PRIVATE);
            SharedPreferences.Editor keyeditor = privKeyStorage.edit();

            keyeditor.putString(PRIVATEKEY, device.getPrivateKey());
            keyeditor.putString(PUBLICKEY,device.getPublicKey());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private OwnDevice getOwnDeviceFromExternalStorage() {
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

    private boolean restoreMessageKeysFromExternalStorage() {
        String text = readText(MESSAGEKEYS);
        String json = "[" + text + "{}]";
        if (text == null) {
            return false;
        }
        Log.d(getClass().getSimpleName(), "MessageKeys-Text: " + text);
        try {
            Log.d(getClass().getSimpleName(), "JSON: " + json);
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length() - 1; i++) {
                MessageKey messageKey = new ObjectMapper().readValue((jsonArray.getJSONObject(i)).
                        toString(), MessageKey.class);
                DatabaseManager.INSTANCE.getMessageKeyDAO().addOrUpdate(messageKey);
            }

            //OwnDevice device = new ObjectMapper().readValue(text, OwnDevice.class);
            //return device;
            return false;
        } catch (Exception e) {
            return false;
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
