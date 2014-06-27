package net.yasme.android.connection;

import net.yasme.android.entities.Device;
import net.yasme.android.exception.RestServiceException;

import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class DeviceTask extends ConnectionTask {

    private static DeviceTask instance;

    public static DeviceTask getInstance() {
        if (instance == null) {
            instance = new DeviceTask();
        }
        return instance;
    }

    private DeviceTask() {
        try {
            this.uri = new URIBuilder(baseURI).setPath("/device").build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public Long registerDevice(Device device) throws RestServiceException {

        try {
            HttpResponse httpResponse = executeRequest(Request.POST, "", device);

            System.out.println("[DEBUG] Device registration was successful");

            return (new JSONObject((new BufferedReader(
                    new InputStreamReader(httpResponse.getEntity()
                            .getContent())
            )).readLine())).getLong("id");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Device getDevice(long deviceId) throws RestServiceException {

        try {

            HttpResponse httpResponse = executeRequest(Request.GET, Long.toString(deviceId));
            return new ObjectMapper().readValue(new BufferedReader(new InputStreamReader(httpResponse.getEntity()
                    .getContent())).readLine(), Device.class);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<Device> getAllDevices() throws RestServiceException {

        ArrayList<Device> devices = new ArrayList<Device>();

        try {
            HttpResponse httpResponse = executeRequest(Request.GET, "all");

            JSONArray jsonArray = new JSONArray(new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent())).readLine());

            for (int i = 0; i < jsonArray.length(); i++)
                devices.add(new ObjectMapper().readValue((jsonArray.getJSONObject(i)).
                        toString(), Device.class));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.out.println("[DEBUG] No.Devices: " + devices.size());
        return devices;
    }

    public boolean deleteDevice(long deviceId) throws RestServiceException {

        executeRequest(Request.DELETE, Long.toString(deviceId));
        System.out.println("[DEBUG] Device removed!");
        return true;
    }
}
