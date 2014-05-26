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
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MessageTask {

	private String url;

	public MessageTask(String url) {
		this.url = url;
	}

	public boolean sendMessage(Message message) {

		try {

			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url + "/msg");

			JSONObject sender = new JSONObject();
			sender.put("pw", "");
			sender.put("name", message.getSender().getName());
			sender.put("email", message.getSender().getEmail());

			JSONObject recipient = new JSONObject();
			recipient.put("pw", "");
			recipient.put("name", message.getRecipient().getName());
			recipient.put("email", message.getRecipient().getEmail());

			JSONObject mes = new JSONObject();
			mes.put("sender", sender);
			mes.put("recipient", recipient);
			mes.put("message", message.getMessage());

			String json = mes.toString();

			System.out.println(json);

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

			System.out.println(false);
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
			// HttpGet request = new HttpGet(url + "/msg/" + lastMessageID);
			HttpGet request = new HttpGet(url + "/msg/");

			request.addHeader("accept", "application/json");

			HttpResponse response = client.execute(request);

			String json = EntityUtils.toString(response.getEntity(), "UTF-8");

			// BufferedReader reader = new BufferedReader(new InputStreamReader(
			// response.getEntity().getContent()));

			// String json = reader.readLine();

			/****************** Debug*Output ********************************/

			// messages.add(new Message(1, 1, json));
			// messages.add(new Message(1, 1, responseString));
			// messages.add(new Message(1, 1, "Debug: HalloTest"));

			/****************** Debug*END ***********************************/

			JSONArray jArray = new JSONArray(json);

			for (int i = 0; i < jArray.length(); i++) {
				/*
				 * JSONObject obj = jArray.getJSONObject(i); messages.add(new
				 * Message( Long.parseLong(obj.getString("sender")), Long
				 * .parseLong(obj.getString("recipient")), obj
				 * .getString("message")));
				 */
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
