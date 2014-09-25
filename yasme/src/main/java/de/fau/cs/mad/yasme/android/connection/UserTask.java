package de.fau.cs.mad.yasme.android.connection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import de.fau.cs.mad.yasme.android.controller.Log;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.exception.Error;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;

/**
 * Created by Florian Winklmeier <f.winklmeier@t-online.de> on 16.06.14.
 */

public class UserTask extends ConnectionTask {

    private static UserTask instance;

    public static synchronized UserTask getInstance() {
        if (instance == null) {
            synchronized (UserTask.class) {
                if (null == instance) {
                    instance = new UserTask();
                }
            }
        }
        return instance;
    }

    private UserTask() {
        try {
            this.uri = new URIBuilder(baseURI).setPath(ConnectionTask.APIVERSION + "/usr").build();
        } catch (URISyntaxException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
        }
    }

    public Long registerUser(User user) throws RestServiceException {

        try {
            HttpResponse httpResponse = executeRequest(Request.POST, "", user);
            JSONObject json = new JSONObject((new BufferedReader(
                    new InputStreamReader(httpResponse.getEntity()
                            .getContent(), "UTF-8")
            )).readLine());

            return Long.parseLong(json.getString("message"));
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (JSONException e) {
            throw new RestServiceException(Error.ERROR);
        }
    }

    public void changeUserData(User user) throws RestServiceException {
        executeRequest(Request.PUT, "", user);
        Log.d(this.getClass().getSimpleName(), "User name was: " + user.getName());
        Log.d(this.getClass().getSimpleName(), "User data changed");
    }


    public User getUser(long userId) throws RestServiceException {
        try {
            HttpResponse httpResponse = executeRequest(Request.GET, String.valueOf(userId));
            return new ObjectMapper().readValue(((new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent(), "UTF-8"))).readLine()), User.class);
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
    }

    public User getUserData() throws RestServiceException {

        try {
            HttpResponse httpResponse = executeRequest(Request.GET, "");
            return new ObjectMapper().readValue(((new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent(), "UTF-8"))).readLine()), User.class);
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
    }

    public void uploadProfilePicture(Bitmap bitmap) throws RestServiceException {

        HttpEntity multipartEntity = MultipartEntityBuilder.create()
                .addBinaryBody("file", scaleBitmap(bitmap, 500), ContentType.create("image/jpeg"), "")
                .build();

        executeUpload(Request.POST, "profile", multipartEntity, null);
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    private byte[] scaleBitmap(Bitmap bigBitmap, int newMaxSize) {
        float picScale = ((float) newMaxSize)
                / ((float) Math.max(bigBitmap.getHeight(), bigBitmap.getWidth()));
        int newHeight = (int) (bigBitmap.getHeight() * picScale);
        int newWidth = (int) (bigBitmap.getWidth() * picScale);
        Bitmap bitmap = Bitmap.createScaledBitmap(bigBitmap, newWidth, newHeight, false);
        return bitmapToByteArray(bitmap);
    }

    public BitmapDrawable getProfilePicture(long userId) throws RestServiceException {
        try {
            String path = "profile/" + userId;
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "image/jpeg; q=0.5, image/png");
            HttpEntity response = (executeRequest(Request.GET, path, headers)).getEntity();
            if (null == response) {
                return null;
            }
            InputStream stream = response.getContent();
            Bitmap picture = BitmapFactory.decodeStream(stream);
            return new BitmapDrawable(DatabaseManager.INSTANCE.getContext().getResources(), picture);
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
    }

    public void requirePasswordToken(User user) throws RestServiceException {
        Log.d(getClass().getSimpleName(), "Require password token");
        HttpResponse httpResponse = executeRequest(Request.POST, "password/token/" + language, user);
    }

    public void changePassword(User user, String token) throws RestServiceException {
        Log.d(getClass().getSimpleName(), "Change password");
        HttpResponse httpResponse = executeRequest(Request.POST, "password/" + token, user);
    }
}
