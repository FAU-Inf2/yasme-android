package net.yasme.android.connection;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.exception.Error;

import org.apache.http.entity.ContentType;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class UserTask extends ConnectionTask {

    private static UserTask instance;

    public static UserTask getInstance() {
        if (instance == null) {
            instance = new UserTask();
        }
        return instance;
    }

    private UserTask() {
        try {
            this.uri = new URIBuilder(baseURI).setPath("/usr").build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public Long registerUser(User user) throws RestServiceException {

        try {
            HttpResponse httpResponse = executeRequest(Request.POST, "", user);
            return Long.parseLong(new BufferedReader(new InputStreamReader(httpResponse
                    .getEntity().getContent(), "UTF-8")).readLine());
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
    }

    public void changeUserData(User user) throws RestServiceException {
        executeRequest(Request.PUT, "", user);
        System.out.println("[DEBUG] User data changed");
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

    //TODO: implement method
    public void uploadProfilePicture(Drawable drawable) throws RestServiceException {

        HttpEntity multipartEntity = MultipartEntityBuilder.create()
                .addBinaryBody("file", drawableToByteArray(drawable), ContentType.create("image/jpeg"), "")
                .build();

        executeRequest(Request.POST, "profile", multipartEntity);
    }


    private byte[] drawableToByteArray(Drawable drawable) {
        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    //public Drawable getOwnProfilePicture() throws RestServiceException {
//
  //      try {
    //        InputStream stream = (executeRequest(Request.GET, "profile")).getEntity().getContent();
      //      return Drawable.createFromStream(stream, null);
//
  //      } catch (IOException e) {
    //        throw new RestServiceException(Error.CONNECTION_ERROR);
      //  }
    //}

    public Drawable getProfilePicture(long userId) throws RestServiceException {
        try {
            String path = "profile/" + userId;
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "image/jpeg; q=0.5, image/png");
            InputStream stream = (executeRequest(Request.GET, path, headers)).getEntity().getContent();
            return Drawable.createFromStream(stream, null);

        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
    }
}
