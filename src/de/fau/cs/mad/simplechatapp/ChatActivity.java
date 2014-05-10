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
import de.fau.cs.mad.simplechatapp.BoundService.LocalBinder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class ChatActivity extends Activity {

	String clientID;
	String lastMessageID;
	String url; // URL???

	BoundService mService;
	boolean mBound = false;

	EditText message;
	TextView status;
	TextView textView1;
	TextView textView2;
	TextView textView3;
	TextView textView4;
	TextView textView5;
	TextView textView6;
	TextView textView7;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

	}

	@Override
	protected void onStart() {
		super.onStart();
		// Bind to LocalService
		Intent service = new Intent(this, BoundService.class);
		bindService(service, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Unbind from the service
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}

	private void initializeViews() {
		message = (EditText) findViewById(R.id.text_message);
		status = (TextView) findViewById(R.id.text_status);
		textView1 = (TextView) findViewById(R.id.textView1);
		textView2 = (TextView) findViewById(R.id.textView2);
		textView3 = (TextView) findViewById(R.id.textView3);
		textView4 = (TextView) findViewById(R.id.textView4);
		textView5 = (TextView) findViewById(R.id.textView5);
		textView6 = (TextView) findViewById(R.id.textView6);
		textView7 = (TextView) findViewById(R.id.textView7);
	}

	public void send(View view) {
		initializeViews();

		String msg = message.getText().toString();

		if (!msg.isEmpty()) {

			/*
			 * if (mBound) { textView1.setText(msg); status.setText("gesendet" +
			 * msg);
			 */

			new SendMessageTask().execute(msg);
		}
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

			if (result) {
				textView1.setText(msg);
				status.setText("gesendet" + msg);
				message.setText("");
			}
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chat, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder service) {
			// We've bound to BoundService, cast the IBinder and get
			// LocalService instance
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mService = null;
			mBound = false;
		}
	};

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_chat, container,
					false);
			return rootView;
		}
	}

}
