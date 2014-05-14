package net.yasme.android.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import net.yasme.android.entities.User;

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

	public String registerUser(User user) {
		try {

			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url + "/usr");

			// To Do: Complete UserObject as JSon

			JSONObject obj = new JSONObject();

			// To Do: UserDaten to JSon
			// Edit UserObject from Server

			// obj.put("Sender", message.getSender());
			// obj.put("Message", message.getMessage());

			String json = obj.toString();

			StringEntity se = new StringEntity(json);

			httpPost.setEntity(se);

			HttpResponse httpResponse = httpclient.execute(httpPost);

			if (httpResponse.getStatusLine().getStatusCode() == 201) {
				// To Do: return created UserID from Server
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} // catch (JSONException e) {
			// e.printStackTrace();
			// }
		return null;
	}

	public User getUserData(String userID) {

		User user = null;

		try {

			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(url + "/usr/" + userID);

			HttpResponse response = client.execute(request);

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent(), "UTF-8"));
			String json = reader.readLine();

			JSONObject jObject = new JSONObject(json);

			user = new User();

			// To Do: Create UserObject with Data from returned JSONObject
			//

		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// return UserObject
		return user;
	}
}
