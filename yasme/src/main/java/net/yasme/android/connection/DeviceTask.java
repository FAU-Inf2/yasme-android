package net.yasme.android.connection;

import android.util.Log;

import net.yasme.android.entities.Device;
import net.yasme.android.entities.OwnDevice;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.exception.Error;
import net.yasme.android.storage.DatabaseManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
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
            synchronized(DeviceTask.class) {
                if (null == instance) {
                    instance = new DeviceTask();
                }
            }
        }
        return instance;
    }

    private DeviceTask() {
        try {
            this.uri = new URIBuilder(baseURI).setPath(ConnectionTask.APIVERSION + "/device").build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public Long registerDevice(OwnDevice device) throws RestServiceException {

        try {
            HttpResponse httpResponse = executeRequest(Request.POST, "", device);

            Log.d(this.getClass().getSimpleName(),"Device registration was successful");

            long deviceId = (new JSONObject((new BufferedReader(
                    new InputStreamReader(httpResponse.getEntity()
                            .getContent())
            )).readLine())).getLong("id");
            DatabaseManager.INSTANCE.setDeviceId(deviceId);
            return deviceId;

        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Device getDevice(long devId) throws RestServiceException {

        try {
            HttpResponse httpResponse = executeRequest(Request.GET, String.valueOf(devId));
            return new ObjectMapper().readValue(new BufferedReader(new InputStreamReader(httpResponse.getEntity()
                    .getContent())).readLine(), Device.class);

        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
    }

    public ArrayList<Device> getAllDevices(Long userId) throws RestServiceException {

        ArrayList<Device> devices = new ArrayList<Device>();

        try {
            HttpResponse httpResponse = executeRequest(Request.GET, "all/" + userId);

            JSONArray jsonArray = new JSONArray(new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent())).readLine());

            for (int i = 0; i < jsonArray.length(); i++)
                devices.add(new ObjectMapper().readValue((jsonArray.getJSONObject(i)).
                        toString(), Device.class));

        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(this.getClass().getSimpleName(),"No.Devices: " + devices.size());
        return devices;
    }

    public void deleteDevice(long deviceId) throws RestServiceException {
        executeRequest(Request.DELETE, Long.toString(deviceId));
        Log.d(this.getClass().getSimpleName(),"Device removed!");
    }
}
