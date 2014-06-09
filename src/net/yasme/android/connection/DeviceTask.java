package net.yasme.android.connection;

import net.yasme.android.entities.Device;
import net.yasme.android.exception.Error;
import net.yasme.android.exception.RestServiceException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class DeviceTask {

    private String url;

    public DeviceTask(String url) {
        this.url = url.concat("/device");
    }

    public Long registerDevice(Device device, String accessToken) throws RestServiceException {

        String requestURL = url;

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(requestURL);

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(device);

            httpPost.setEntity(new StringEntity(json));

            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Accept", "application/json");

            httpPost.setHeader("userId", Long.toString(device.getUser()));
            httpPost.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpPost);

            switch (httpResponse.getStatusLine().getStatusCode()) {
                case 201:

                    System.out.println("Device registration was successful");

                    return (new JSONObject((new BufferedReader(
                            new InputStreamReader(httpResponse.getEntity()
                                    .getContent())
                    )).readLine())).getLong("id");
                case 500:
                    throw new RestServiceException(Error.STORE_FAILED_EXCEPTION);
                default:
                    throw new RestServiceException(Error.ERROR);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Device getDevice(long deviceId, long userId, String accessToken) throws RestServiceException {

        String requestURL = url.concat("/" + deviceId);

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(requestURL);

            httpGet.setHeader("accept", "application/json");
            httpGet.setHeader("userId", Long.toString(userId));
            httpGet.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpGet);

            switch (httpResponse.getStatusLine().getStatusCode()) {

                case 200:
                    /*new InputStreamReader(httpResponse.getEntity()
                            .getContent()).readLine();

                    Device.Platform platform = null;
                    String jsonPlatform = jsonObject.getString("platform");

                    if (jsonPlatform.equals(Device.Platform.ANDROID.toString()))
                        platform = Device.Platform.ANDROID;
                    else if (jsonPlatform.equals(Device.Platform.IOS.toString()))
                        platform = Device.Platform.IOS;
                    else if (jsonPlatform.equals(Device.Platform.WINDOWSPHONE.toString()))
                        platform = Device.Platform.WINDOWSPHONE;

                    */
                    //TODO: publicKey nachtragen
                    //(jsonObject.getJSONObject("user")).getLong("id"), null,
                    //        platform, jsonObject.getString("type"), jsonObject.getString("number"), null);
                case 500:
                    throw new RestServiceException(Error.NOT_FOUND_EXCEPTION);
                default:
                    throw new RestServiceException(Error.ERROR);
            }

        } catch (IllegalStateException e) {
            e.printStackTrace();
        //} catch (JSONException e) {
        //    e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }

        return null;
    }

    public ArrayList<Device> getAllDevices(long userId, String accessToken) throws RestServiceException {

        String requestURL = url.concat("/all/" + userId);
        ArrayList<Device> devices = new ArrayList<Device>();
        /*
        try {

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(requestURL);

            httpGet.setHeader("accept", "application/json");

            httpGet.setHeader("userId", Long.toString(userId));
            httpGet.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpGet);

            System.out.println(httpResponse.getStatusLine().getStatusCode());

            switch (httpResponse.getStatusLine().getStatusCode()) {

                case 200:
                    JSONArray jsonArray = new JSONArray(new BufferedReader(new InputStreamReader(
                            httpResponse.getEntity().getContent())).readLine());
                    obj.getString("type"), obj.getString("userID")));
                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        Device.Platform platform = null;
                        String jsonPlatform = jsonObject.getString("platform");

                        if (jsonPlatform.equals(Device.Platform.ANDROID.toString()))
                            platform = Device.Platform.ANDROID;
                        else if (jsonPlatform.equals(Device.Platform.IOS.toString()))
                            platform = Device.Platform.IOS;
                        else if (jsonPlatform.equals(Device.Platform.WINDOWSPHONE.toString()))
                            platform = Device.Platform.WINDOWSPHONE;

                        devices.add(new Device(jsonObject.getLong("id"), jsonObject.getJSONObject("user").getLong("id"),
                                platform, jsonObject.getString("type"), jsonObject.getString("number"), null));
                    }
                    System.out.println("No.Devices: " + jsonArray.length());
                    break;

                //TODO: ErrorCode vermutlich fehlerhaft
                case 204:
                    throw new RestServiceException(Error.NOT_FOUND_EXCEPTION);
                default:
                    throw new RestServiceException(Error.ERROR);
            }
            new RestServiceException(Error.CONNECTION_ERROR);
        }*/
        return devices;
    }

    public boolean deleteDevice(long deviceId, long userId, String accessToken) throws RestServiceException {

        String requestURL = url.concat("/" + deviceId);
        try {

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpDelete httpDelete = new HttpDelete(requestURL);

            httpDelete.setHeader("userId", Long.toString(userId));
            httpDelete.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpDelete);

            switch (httpResponse.getStatusLine().getStatusCode()) {

                case 201:
                    System.out.println("[DEBUG] Device removed!");
                    return true;
                case 404:
                    throw new RestServiceException(Error.NOT_FOUND_EXCEPTION);
                default:
                    throw new RestServiceException(Error.ERROR);

            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
        return false;
    }
}
