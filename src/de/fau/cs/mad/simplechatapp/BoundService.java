package de.fau.cs.mad.simplechatapp;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

public class BoundService extends Service{

	String clientID;
	String lastMessageID;
	String url; // URL???


    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

	
	/**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        BoundService getService() {
            // Return this instance of BoundService so clients can call public methods
            return BoundService.this;
        }
    }

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Allows the system to shut down the service
		return START_NOT_STICKY;
    }
	
	
	private class SendMessageTask extends AsyncTask<String, Void, Boolean> {

		private String msg;

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

			/*if (result) {
				textView1.setText(msg);
				status.setText("gesendet" + msg);
				message.setText("");
			}*/
		}
	}

	private class GetMessageTask extends AsyncTask<String, Void, String> {

		protected String doInBackground(String... getUrl) {

			Scanner scan;
			String str = null;
			try {

				URL url = new URL(getUrl[0] + "/message/getMessages/"
						+ lastMessageID);
				scan = new Scanner(url.openStream());
				str = new String();

				while (scan.hasNext()) {
					str += scan.nextLine();
				}

				scan.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

			return str;
		}

		@SuppressLint("NewApi")
		protected void onPostExecute(Boolean result) {

			ArrayList<String> messages = new ArrayList<String>(); // temporärer Zwischenspeicher

			JSONArray lang = null;

			try {
				lang = new JSONArray(result);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}

			String id = null;
			String msg = null;

			for (int i = 0; i < lang.length(); i++) {

				try {

					id = (lang.getJSONObject(i)).getString("id");
					msg = (lang.getJSONObject(i)).getString("message");

				} catch (JSONException e) {
					e.printStackTrace();
				}

				messages.add(msg);
				lastMessageID = id;
			}

			// To Do:

			/*
			 * Nachrichten auf View ausgeben Serverspezifikation betrachten für
			 * genaue Syntax des JSON Objects
			 */
		}
	}

	
	/**
	 * TODO: implement public methods the client can call
	 * - returns the current Service instance, which has public 
	 * 		methods the client can call
	 * - or, returns an instance of another class hosted by the 
	 * 		service with public methods the client can call
	 */

}
