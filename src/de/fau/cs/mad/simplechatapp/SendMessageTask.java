package de.fau.cs.mad.simplechatapp;

import java.io.IOException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

public class SendMessageTask extends AsyncTask<String, Void, Boolean> {

	private String msg;
	private URL url;
	private String clientID;

	public SendMessageTask(URL url, String ClientID) {
		this.url = url;
		this.clientID = ClientID;
	}

	protected Boolean doInBackground(String... message) {

		this.msg = message[0];

		JSONObject jMessage = new JSONObject();
		String json;
		try {
			jMessage.put("Sender", clientID);
			jMessage.put("message", this.msg);

			json = jMessage.toString();

			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url + "/message/sendMessage");

			StringEntity se = new StringEntity(json);

			httpPost.setEntity(se);

			httpPost.setHeader("Content-type", "application/json");

			HttpResponse httpResponse = httpclient.execute(httpPost);

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// To Do: Repsonse auf Erfolg überprüfen

		return true;
	}

	protected void onPostExecute(Boolean result) {

	}
}
