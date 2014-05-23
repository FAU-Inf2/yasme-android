package net.yasme.android.connection;

import net.yasme.android.entities.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

public class UserTask {

	private String url;

	public UserTask(String url) {
		this.url = url;
	}

	/**
	 * registerUser() get as return value an ID which should be saved on
	 * the client to use it for all user requests
	 * @param user
	 * @return userID, which should be stored on the device
	 */
	public String registerUser(User user) {
		try {

			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url + "/usr");

			// To Do: Complete UserObject as JSon

			JSONObject obj = new JSONObject();

			// To Do: UserDaten to JSon
			// Edit UserObject from Server

			obj.put("pw", user.getPw());
			obj.put("email", user.getEmail());
			obj.put("name", user.getName());

			String json = obj.toString();

			StringEntity se = new StringEntity(json);

			httpPost.setEntity(se);

			httpPost.setHeader("Content-type", "application/json");
			httpPost.setHeader("Accept", "application/json");

			HttpResponse httpResponse = httpclient.execute(httpPost);

			if (httpResponse.getStatusLine().getStatusCode() == 201) {

				BufferedReader rd = new BufferedReader(new InputStreamReader(
						httpResponse.getEntity().getContent()));
				String id = rd.readLine();
				return id;
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

	public User getUserData(String userID) {

		User user = null;

		try {

			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(url + "/usr/" + userID);
			request.addHeader("accept", "application/json");

			HttpResponse response = client.execute(request);

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent(), "UTF-8"));
			String json = reader.readLine();

			JSONObject jObject = new JSONObject(json);

			user = new User(null, jObject.getString("name"),
					jObject.getString("email"));

			/******** DEBUG ***********/
			System.out.println(user.getEmail() + " " + user.getName());
			/******** DEBUG*END *******/

		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return user;
	}
}
