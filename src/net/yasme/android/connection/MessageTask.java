package net.yasme.android.connection;

import android.annotation.SuppressLint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import net.yasme.android.entities.Message;

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

public class MessageTask {

	private String url;

	public MessageTask(String url) {
		this.url = url;
		// this.url = "http://devel.yasme.net"; // Debug
	}

	public boolean sendMessage(Message message) {

		try {

			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url + "/msg");

			// To Do: Complete Message as JSon

			JSONObject obj = new JSONObject();
			obj.put("Sender", message.getSender());
			obj.put("Message", message.getMessage());
			String json = obj.toString();

			StringEntity se = new StringEntity(json);

			httpPost.setEntity(se);

			httpPost.setHeader("Content-type", "application/json");
			httpPost.setHeader("Accept", "application/json");

			HttpResponse httpResponse = httpclient.execute(httpPost);

			if (httpResponse.getStatusLine().getStatusCode() == 201) {

				/****************** Debug*Output ********************************/
				BufferedReader rd = new BufferedReader(new InputStreamReader(
						httpResponse.getEntity().getContent()));
				String line = "";
				while ((line = rd.readLine()) != null) {
					System.out.println(line);
				}
				/****************** Debug*END ***********************************/

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

	@SuppressLint("NewApi")
	// To Do: Prüfen
	public ArrayList<Message> getMessage(String lastMessageID) {

		ArrayList<Message> messages = new ArrayList<Message>();

		try {

			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(url + "/get/" + lastMessageID);

			HttpResponse response = client.execute(request);
			JSONArray jArray = new JSONArray(response.getEntity().getContent());

			for (int i = 0; i < jArray.length(); i++) {

				JSONObject obj = jArray.getJSONObject(i);
				messages.add(new Message(Long.parseLong((String) obj
						.get("sender")), Long.parseLong((String) obj
						.get("recipient")), (String) obj.get("message")));
			}

		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return messages;
	}
}
