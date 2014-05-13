package net.yasme.android.connection;

import java.io.IOException;
import java.net.URL;

import net.yasme.android.entities.Message;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import org.apache.http.impl.client.DefaultHttpClient;
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

			// To Do: Complete Message as JSon

			JSONObject obj = new JSONObject();
			obj.put("Sender", message.getSender());
			obj.put("Message", message.getMessage());
			String json = obj.toString();

			StringEntity se = new StringEntity(json);

			httpPost.setEntity(se);

			HttpResponse httpResponse = httpclient.execute(httpPost);

			if (httpResponse.getStatusLine().getStatusCode() == 201) {
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

	public Message[] getMessage() {

		// To Do: getMessage()
		return null;
	}
}
