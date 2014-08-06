package net.yasme.android.storage;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;

import net.yasme.android.entities.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by bene on 02.07.14.
 */
public enum PictureManager {
    INSTANCE;

    private Context applicationContext;

    /**
     * Stores a given bitmap on internal storage
     * @param user for generating the filename
     * @param bitmap bitmap to be stored
     * @return path of the stored picture
     */
    public String storePicture(User user, Bitmap bitmap) throws IOException {
        // TODO Remove old picture

        // Generate file name
        String filename = user.getId() + "_profilePicture_" + user.getLastModified().getTime();

        ContextWrapper cw = new ContextWrapper(applicationContext);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("userProfiles", Context.MODE_PRIVATE);
        // Create imageDir
        File path = new File(directory, filename);

        FileOutputStream fos = new FileOutputStream(path);

        // Use the compress method on the BitMap object to write image to the OutputStream
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.close();
        return directory.getAbsolutePath() + "/" + filename;
    }
}
