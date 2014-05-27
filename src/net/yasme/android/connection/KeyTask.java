package net.yasme.android.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import net.yasme.android.entities.Id;
import net.yasme.android.entities.MessageKey;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

public class KeyTask {

	private String url;

	public KeyTask(String url) {
		this.url = url;
	}

	public boolean saveKey(MessageKey messageKey) {

		try {

			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url + "/msgkey");

			// To Do: Complete MessageKey as JSon
			// ObjectMapper mapper = new ObjectMapper();
			// String json = mapper.writeValueAsString(message);

			JSONObject obj = new JSONObject();

			obj.put("id", messageKey.getId());

			/*
			 * To Do: All propertys to JSON
			 */

			String json = obj.toString();

			StringEntity se = new StringEntity(json);

			httpPost.setEntity(se);

			httpPost.setHeader("Content-type", "application/json");
			httpPost.setHeader("Accept", "application/json");

			HttpResponse httpResponse = httpclient.execute(httpPost);

			if (httpResponse.getStatusLine().getStatusCode() == 201) {

				/**** DEBUG *******/
				BufferedReader rd = new BufferedReader(new InputStreamReader(
						httpResponse.getEntity().getContent()));
				System.out.println(rd.readLine());
				/**** DEBUG*END ***/

				return true;
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return false;
	}

	public MessageKey getKey(String userID, String recID, String deviceID) {

		MessageKey messageKey = null;

		try {

			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(url + "/" + userID + "/" + recID
					+ "/" + deviceID);
			request.addHeader("accept", "application/json");

			HttpResponse response = client.execute(request);

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent(), "UTF-8"));
			String json = reader.readLine();

			JSONObject jObject = new JSONObject(json);

			messageKey = new MessageKey(
					Long.parseLong(jObject.getString("id")),
					Long.parseLong(jObject.getString("creator")),
					Long.parseLong(jObject.getString("recipient")),
					Long.parseLong(jObject.getString("devId")),
					jObject.getString("key"), Byte.parseByte(jObject
							.getString("encType")), Long.parseLong(jObject
							.getString("encInfoId")),
					jObject.getString("encInfo"), jObject.getString("sign"));

			/******** DEBUG ***********/
			System.out.println(messageKey.getId() + " "
					+ messageKey.getRecipient());
			/******** DEBUG*END *******/

		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return messageKey;
	}

	public Id getKeyId(String userID) {

		Id id = null;
		try {

			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(url + "/id/" + userID);
			request.addHeader("accept", "application/json");

			HttpResponse response = client.execute(request);

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent(), "UTF-8"));

			String json = reader.readLine();

			JSONObject jObject = new JSONObject(json);

			id = new Id(Long.parseLong(jObject.getString("id")));

		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return id;
	}

}