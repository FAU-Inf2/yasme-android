package de.fau.cs.mad.yasme.android.contacts;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Created by Martin Sturm <martin.sturm@fau.de> on 11.09.2014.
 */

public class QR {
    private static final int SIZE = 400;
    public Bitmap generateQRCode(String data) {
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
