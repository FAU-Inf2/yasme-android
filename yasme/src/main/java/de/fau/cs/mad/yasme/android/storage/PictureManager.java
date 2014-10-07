package de.fau.cs.mad.yasme.android.storage;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.entities.User;

/**
 * Created by Benedikt Lorch <benedikt.lorch@studium.fau.de> on 02.07.14.
 */
public enum PictureManager {
    INSTANCE;

    private Context mContext = DatabaseManager.INSTANCE.getContext();

    private Bitmap scaleBitmap(Bitmap bigBitmap, int newMaxSize) {
        float picScale = ((float) newMaxSize)
                / ((float) Math.max(bigBitmap.getHeight(), bigBitmap.getWidth()));
        int newHeight = (int) (bigBitmap.getHeight() * picScale);
        int newWidth = (int) (bigBitmap.getWidth() * picScale);
        Bitmap bitmap = Bitmap.createScaledBitmap(bigBitmap, newWidth, newHeight, false);
        return bitmap;
    }

    public byte[] scaledBitmapToByteArray(Bitmap bitmap, int newMaxSize) {
        float picScale = ((float) newMaxSize)
                / ((float) Math.max(bitmap.getHeight(), bitmap.getWidth()));
        int newHeight = (int) (bitmap.getHeight() * picScale);
        int newWidth = (int) (bitmap.getWidth() * picScale);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    /**
     * Stores a given bitmap on internal storage
     *
     * @param user   for generating the filename
     * @param bitmap bitmap to be stored
     * @return path of the stored picture
     */
    public String storePicture(User user, Bitmap bitmap) throws IOException {
        // TODO Remove old picture
        // I think it will be replaced automatically

        // Create directory userProfiles
        ContextWrapper cw = new ContextWrapper(mContext);
        File directory = cw.getDir("userPictures", Context.MODE_PRIVATE);

        // Generate file name
        String filename = user.getId() + "_profilePicture.jpg";

        // Concatenate directory and filename to path
        File path = new File(directory, filename);

        FileOutputStream fileOutputStream = new FileOutputStream(path);
        BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);

        // scale down bitmap
        Bitmap scaledBitmap = scaleBitmap(bitmap, 300);

        // Use the compress method on the BitMap object to write image to the OutputStream
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);

        bos.flush();
        bos.close();
        fileOutputStream.close();

        Log.d(this.getClass().getSimpleName(), "Picture stored under: " + path.getAbsolutePath());
        return path.getAbsolutePath();
    }


    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * Fetches the profilePicture from the storage sampled to a smaller size
     *
     * @param user      User
     * @param reqHeight int
     * @param reqWidth  int
     * @return profilePicture as a bitmap
     */
    public Bitmap getPicture(User user, int reqHeight, int reqWidth) {
        String path = user.getProfilePicture();
        if (path == null || path.isEmpty()) {
            return null;
        }
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public boolean deletePicture(User user) {
        String path = user.getProfilePicture();
        if (path == null || path.isEmpty()) {
            return false;
        }
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        return file.delete();
    }

    /**
     * @param bitmap
     * @return converting bitmap and return a string
     */
    public String bitMapToString(Bitmap bitmap) {
        Bitmap btmp = scaleBitmap(bitmap, 500);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    /**
     * @param encodedString
     * @return bitmap (from given string)
     */
    public Bitmap stringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }
}
