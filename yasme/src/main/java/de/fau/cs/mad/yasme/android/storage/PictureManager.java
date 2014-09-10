package de.fau.cs.mad.yasme.android.storage;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.controller.Toaster;
import de.fau.cs.mad.yasme.android.entities.User;

/**
 * Created by Benedikt Lorch <benedikt.lorch@studium.fau.de> on 02.07.14.
 */
public enum PictureManager {
    INSTANCE;

    private Context mContext = DatabaseManager.INSTANCE.getContext();

    public Bitmap getPicture(User user) throws IOException {
        String path = user.getProfilePicture();
        if (path == null || path.isEmpty()) {
            return null;
        }
        Log.e(this.getClass().getSimpleName(), "Try to load Picture from: " + path);
        Toaster.getInstance().toast("Try to load Picture from: " + path, Toast.LENGTH_LONG);
        return getPictureNew(user);
    }

    /**
     * Stores a given bitmap on internal storage
     * @param user for generating the filename
     * @param bitmap bitmap to be stored
     * @return path of the stored picture
     */
    public String storePicture(User user, Bitmap bitmap) throws IOException {
        // TODO Remove old picture

        // Create directory userProfiles
        ContextWrapper cw = new ContextWrapper(mContext);
        File directory = cw.getDir("userPictures", Context.MODE_PRIVATE);

        // Generate file name
        String filename = user.getId() + "_profilePicture_"/* + user.getLastModified().getTime()*/ + ".jpg";

        // Concatenate directory and filename to path
        File path = new File(directory, filename);

        FileOutputStream fileOutputStream = new FileOutputStream(path);
        BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);

        // Use the compress method on the BitMap object to write image to the OutputStream
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);

        bos.flush();
        bos.close();
        fileOutputStream.close();

        Log.d(this.getClass().getSimpleName(), "Picture stored under: " + path.getAbsolutePath());
        return path.getAbsolutePath();
    }

    /**
     * Fetches the profilePicture from the storage
     *
     * @param user User
     * @return profilePicture as a bitmap
     * @throws IOException
     */
    private Bitmap getPictureFromStream(User user) throws IOException {
        Bitmap picture;
        String path = user.getProfilePicture();

        if (path == null || path.isEmpty()) {
            return null;
        }

        // Open a stream
        FileInputStream fis = new FileInputStream(path);
        BufferedInputStream buf = new BufferedInputStream(fis);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        picture = BitmapFactory.decodeStream(buf, null, options);

        if (fis != null) {
            fis.close();
        }
        if (buf != null) {
            buf.close();
        }

        return picture;
    }

    private static int calculateInSampleSize(
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
     * @throws IOException
     */
    private static Bitmap getSampledPicture(User user, int reqWidth, int reqHeight)
            throws IOException {

        Bitmap picture;
        String path = user.getProfilePicture();

        if (path == null || path.isEmpty()) {
            return null;
        }

        // Open a stream
        FileInputStream fis = new FileInputStream(path);
        BufferedInputStream buf = new BufferedInputStream(fis);

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(buf, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        picture = BitmapFactory.decodeStream(buf, null, options);

        if (fis != null) {
            fis.close();
        }
        if (buf != null) {
            buf.close();
        }

        return picture;
    }

    private Bitmap getPictureFromFile(User user) {
        String path = user.getProfilePicture();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        return BitmapFactory.decodeFile(path);
    }

    private Bitmap getPictureNew(User user) throws IOException {
        // Create directory userProfiles
        ContextWrapper cw = new ContextWrapper(mContext);
        File directory = cw.getDir("userPictures", Context.MODE_PRIVATE);

        // Generate file name
        String filename = user.getId() + "_profilePicture_"/* + user.getLastModified().getTime()*/ + ".jpg";

        // Concatenate directory and filename to path
        File path = new File(directory, filename);


        FileInputStream inStream = new FileInputStream(path);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inStream);

        Bitmap bitmap = BitmapFactory.decodeFile(path.getAbsolutePath());
        return bitmap;
    }
}
