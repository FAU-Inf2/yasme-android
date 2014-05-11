package de.fau.cs.mad.simplechatapp;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

public class GetMessageTask extends AsyncTask<String, Void, String> {

	private URL url;

	public GetMessageTask(URL url) {
		this.url = url;
	}

	protected String doInBackground(String... lastMessageID) {

		Scanner scan;
		String str = null;
		try {
			URL url = new URL(this.url + "/message/getMessages/"
					+ lastMessageID[0]);
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

		ArrayList<String> messages = new ArrayList<String>(); // temporärer
																// Zwischenspeicher

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
		//	lastMessageID = id;
		}

		// To Do:
		/*
		 * Nachrichten auf View ausgeben Serverspezifikation betrachten für
		 * genaue Syntax des JSON Objects
		 */
	}
}
