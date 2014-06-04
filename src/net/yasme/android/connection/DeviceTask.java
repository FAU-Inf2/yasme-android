package net.yasme.android.connection;

import net.yasme.android.entities.Device;
import net.yasme.android.exception.Error;
import net.yasme.android.exception.RestServiceException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
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

    public Long registerDevice(Device device) throws RestServiceException {

        String requestURL = url;

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(requestURL);

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(device);

            httpPost.setEntity(new StringEntity(json));

            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Accept", "application/json");

            HttpResponse httpResponse = httpClient.execute(httpPost);

            switch (httpResponse.getStatusLine().getStatusCode()) {
                case 201:

                    System.out.println("Device registration was successful");

                    return (new JSONObject((new BufferedReader(new InputStreamReader(
                            httpResponse.getEntity().getContent()))).readLine())).getLong("id");
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

    public Device getDevice(long deviceId) throws RestServiceException {

        String requestURL = url.concat("/" + deviceId);

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(requestURL);
            httpGet.addHeader("accept", "application/json");

            HttpResponse httpResponse = httpClient.execute(httpGet);

            switch (httpResponse.getStatusLine().getStatusCode()) {
                case 200:

                    JSONObject jsonObject = new JSONObject((new BufferedReader(new InputStreamReader(
                            httpResponse.getEntity().getContent()))).readLine());
                    JSONObject user = jsonObject.getJSONObject("user");

                    /*TODO:
                    Platform Enum aus Rückgabe JSON auslesen und in Device ablegen
                    Sinn von Number Obj erfragen
                     */

                    return new Device(jsonObject.getLong("id"), (jsonObject.getJSONObject("user")).getLong("id"),
                            null, jsonObject.getString("type"));
                case 500:
                    throw new RestServiceException(Error.CONNECTION_ERROR);
                default:
                    throw new RestServiceException(Error.ERROR);
            }


        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }

        return null;
    }

    public ArrayList<Device> getAllDevices(String userID) {

        //TODO: IN PROGRESS
        /*
        ArrayList<Device> devices = new ArrayList<Device>();

        try {

            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(url + "/device/all/" + userID);
            request.addHeader("accept", "application/json");

            HttpResponse response = client.execute(request);

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()));

            String json = reader.readLine();

            JSONArray jArray = new JSONArray(json);

            for (int i = 0; i < jArray.length(); i++) {


                JSONObject obj = jArray.getJSONObject(i);
                devices.add(new Device(obj.getString("id"), obj
                        .getString("platform"), obj.getString("type"), obj
                        .getString("userID")));

            }

        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return devices;
        */
        return null;
    }

    //TODO: DELETE Methode erstellen
}
