package de.fau.cs.mad.yasme.android.contacts;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import de.fau.cs.mad.yasme.android.asyncTasks.QRTask;
import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.encryption.KeyEncryption;
import de.fau.cs.mad.yasme.android.entities.QRData;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;

/**
 * Created by Martin Sturm <martin.sturm@fau.de> on 11.09.2014.
 */

public class QR {
    private static final int SIZE = 1024;
    private static Bitmap qrCode;
    private static boolean isRunning = false;

    public static void init(boolean force) {
        if (force) {
            qrCode = null;
            isRunning = false;
        }
        if (qrCode == null && !isRunning) {
            isRunning = true;
            new QRTask().execute();
        }
    }

    public static void finished() {
        isRunning = false;
    }

    public Bitmap generateQRCode() {
        if (qrCode != null) {
            return qrCode;
        }
        QRData qrdata = new QRData();
        DatabaseManager db = DatabaseManager.INSTANCE;
        qrdata.setDeviceId(db.getDeviceId());

        Context context = db.getContext();
        String RSAKEY_STORAGE_USER = KeyEncryption.RSAKEY_STORAGE + "_" + db.getDeviceId();
        SharedPreferences privKeyStorage = context.getSharedPreferences(RSAKEY_STORAGE_USER, Context.MODE_PRIVATE);
        String pubKeyInBase64 = privKeyStorage.getString(KeyEncryption.PUBLICKEY, "");
        qrdata.setPublicKey(pubKeyInBase64);

        //ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        ObjectWriter objectWriter = new ObjectMapper().writer();
        try {
            String data = objectWriter.writeValueAsString(qrdata);
            qrCode = generateQRCode(data);
            return qrCode;
        } catch (Exception e) {
            return null;
        }
    }

    public Bitmap generateQRCode(String data) {
        Log.d(getClass().getName(), "Generate QR for " + data);
        com.google.zxing.Writer writer = new QRCodeWriter();
        BitMatrix bm;
        try {
            bm = writer.encode(data, BarcodeFormat.QR_CODE,SIZE, SIZE);
        } catch (Exception e) {
            return null;
        }

        Bitmap imageBitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);

        for (int i = 0; i < SIZE; i++) {//width
            for (int j = 0; j < SIZE; j++) {//height
                imageBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK: Color.WHITE);
            }
        }
        return imageBitmap;
    }

}
