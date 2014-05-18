package net.yasme.android.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.yasme.android.entities.Device;

public class DeviceTask {

	private String url;

	public DeviceTask(String url) {
		this.url = url;
	}

	public String registerDevice(Device device) {

		try {

			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url + "/device");

			// To Do: Complete Device as JSon
			// ObjectMapper mapper = new ObjectMapper();
			// String json = mapper.writeValueAsString(message);

			JSONObject obj = new JSONObject();
			obj.put("id", device.getId());
			obj.put("platform", device.getPlatform());
			obj.put("type", device.getType());
			obj.put("userID", device.getUserID());
			obj.put("number", device.getNumber());
			String json = obj.toString();

			StringEntity se = new StringEntity(json);

			httpPost.setEntity(se);

			httpPost.setHeader("Content-type", "application/json");
			httpPost.setHeader("Accept", "application/json");

			HttpResponse httpResponse = httpclient.execute(httpPost);

			if (httpResponse.getStatusLine().getStatusCode() == 201) {

				BufferedReader rd = new BufferedReader(new InputStreamReader(
						httpResponse.getEntity().getContent()));

				return rd.readLine();
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}

	public Device getDevice(String deviceID) {

		Device device = null;

		try {

			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(url + "/device/" + deviceID);
			request.addHeader("accept", "application/json");

			HttpResponse response = client.execute(request);

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent(), "UTF-8"));
			String json = reader.readLine();

			JSONObject jObject = new JSONObject(json);

			device = new Device(jObject.getString("id"),
					jObject.getString("platform"), jObject.getString("type"),
					jObject.getString("userID"));

			/******** DEBUG ***********/
			System.out.println(device.getUserID() + " " + device.getPlatform());
			/******** DEBUG*END *******/

		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return device;
	}

	public ArrayList<Device> getAllDevices(String userID) {

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
	}
}
