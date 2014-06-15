package net.yasme.android.connection;

import net.yasme.android.connection.ssl.HttpClient;
import net.yasme.android.entities.Device;
import net.yasme.android.exception.Error;
import net.yasme.android.exception.RestServiceException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class DeviceTask extends  ConnectionTask {

    private static DeviceTask instance;
    private URI uri;

    public static DeviceTask getInstance() {
        if (instance == null) {
            instance = new DeviceTask();
        }
        return instance;
    }

    private DeviceTask() {

        //TODO: URI dynamisch auslesen
        try {
            this.uri = new URIBuilder().setScheme(serverScheme).
                    setHost(serverHost).setPort(serverPort).setPath("/device").build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public Long registerDevice(Device device, String accessToken) throws RestServiceException {

        URI requestURI = uri;

        try {
            CloseableHttpClient httpClient = HttpClient.createSSLClient();
            HttpPost httpPost = new HttpPost(requestURI);

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
                    System.out.println("[DEBUG] Device registration was successful");

                    return (new JSONObject((new BufferedReader(
                            new InputStreamReader(httpResponse.getEntity()
                                    .getContent())
                    )).readLine())).getLong("id");
                case 401:
                    System.out.println("Unauthorized");
                    throw new RestServiceException(Error.UNAUTHORIZED);
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
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Device getDevice(long deviceId, long userId, String accessToken) throws RestServiceException {

        try {
            URI requestURI = new URIBuilder(uri).setPath(uri.getPath()+"/"+deviceId).build();


            CloseableHttpClient httpClient = HttpClient.createSSLClient();
            HttpGet httpGet = new HttpGet(requestURI);

            httpGet.setHeader("accept", "application/json");
            httpGet.setHeader("userId", Long.toString(userId));
            httpGet.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpGet);

            switch (httpResponse.getStatusLine().getStatusCode()) {

                case 200:
                    JSONObject jsonObject = new JSONObject(new BufferedReader (new InputStreamReader(httpResponse.getEntity()
                            .getContent())).readLine());

                    Device.Platform platform = null;
                    String jsonPlatform = jsonObject.getString("platform");

                    if (jsonPlatform.equals(Device.Platform.ANDROID.toString()))
                        platform = Device.Platform.ANDROID;
                    else if (jsonPlatform.equals(Device.Platform.IOS.toString()))
                        platform = Device.Platform.IOS;
                    else if (jsonPlatform.equals(Device.Platform.WINDOWSPHONE.toString()))
                        platform = Device.Platform.WINDOWSPHONE;

                    //TODO: publicKey nachtragen
                    return new Device((jsonObject.getJSONObject("user")).getLong("id"),platform,
                            jsonObject.getString("type"),jsonObject.getString("number"));
                case 401:
                    System.out.println("Unauthorized");
                    throw new RestServiceException(Error.UNAUTHORIZED);
                case 500:
                     throw new RestServiceException(Error.NOT_FOUND_EXCEPTION);
                default:
                     throw new RestServiceException(Error.ERROR);
            }

        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.UNAUTHORIZED);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<Device> getAllDevices(long userId, String accessToken) throws RestServiceException {

        ArrayList<Device> devices = new ArrayList<Device>();

        try {
            URI requestURI = new URIBuilder(uri).setPath(uri.getPath()+"/all/"+userId).build();


            CloseableHttpClient httpClient = HttpClient.createSSLClient();
            HttpGet httpGet = new HttpGet(requestURI);

            httpGet.setHeader("accept", "application/json");

            httpGet.setHeader("userId", Long.toString(userId));
            httpGet.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpGet);

            System.out.println(httpResponse.getStatusLine().getStatusCode());

            switch (httpResponse.getStatusLine().getStatusCode()) {

                case 200:
                    JSONArray jsonArray = new JSONArray(new BufferedReader(new InputStreamReader(
                            httpResponse.getEntity().getContent())).readLine());

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
                case 401:
                    System.out.println("Unauthorized");
                    throw new RestServiceException(Error.UNAUTHORIZED);
                default:
                    throw new RestServiceException(Error.ERROR);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return devices;
    }

    public boolean deleteDevice(long deviceId, long userId, String accessToken) throws RestServiceException {

        try {

            URI requestURI = new URIBuilder(uri).setPath(uri.getPath()+"/"+deviceId).build();

            CloseableHttpClient httpClient = HttpClient.createSSLClient();
            HttpDelete httpDelete = new HttpDelete(requestURI);

            httpDelete.setHeader("userId", Long.toString(userId));
            httpDelete.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpDelete);

            System.out.println(httpResponse.getStatusLine().getStatusCode());
            switch (httpResponse.getStatusLine().getStatusCode()) {

                case 201:
                    System.out.println("[DEBUG] Device removed!");
                    return true;
                case 401:
                    System.out.println("Unauthorized");
                    throw new RestServiceException(Error.UNAUTHORIZED);
                case 404:
                    throw new RestServiceException(Error.NOT_FOUND_EXCEPTION);
                default:
                    throw new RestServiceException(Error.ERROR);

            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }
}
