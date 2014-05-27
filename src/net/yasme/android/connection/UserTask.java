package net.yasme.android.connection;

import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.exception.UserError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.Header;
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
	 * registerUser() get as return value an ID which should be saved on the
	 * client to use it for all user requests
	 * 
	 * @param user
	 * @return userID, which should be stored on the device
	 */
	public String registerUser(User user) throws RestServiceException {
		try {

			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url + "/usr");

			// TODO: Complete UserObject as JSon

			JSONObject obj = new JSONObject();

			// TODO: UserDaten to JSon
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

			switch (httpResponse.getStatusLine().getStatusCode()) {
			case 201:
				return (new BufferedReader(new InputStreamReader(httpResponse
						.getEntity().getContent(), "UTF-8"))).readLine();
			case 500:
				throw new RestServiceException(UserError.REGISTRATION_FAILED);
			default:
				throw new RestServiceException(UserError.ERROR);
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			throw new RestServiceException(UserError.REGISTRATION_FAILED);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String[] loginUser(User user) throws RestServiceException {

		try {

			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url + "/sign/in");

			JSONObject obj = new JSONObject();

			// TODO: make it dynamic:
			// obj.put("email", user.getEmail());
			// obj.put("pw", user.getPw());

			// Debug: Testdaten
			obj.put("email", "flo@yasme.net");
			obj.put("pw", "pw");

			String json = obj.toString();

			StringEntity se = new StringEntity(json);

			httpPost.setEntity(se);

			httpPost.setHeader("Content-type", "application/json");
			httpPost.setHeader("Accept", "application/json");

			HttpResponse httpResponse = httpclient.execute(httpPost);

			switch (httpResponse.getStatusLine().getStatusCode()) {

			case 200:
				Header userID = httpResponse.getFirstHeader("userId");
				Header token = httpResponse.getFirstHeader("Authorization");
				System.out.println("Login Success");
				return new String[] { userID.getValue(), token.getValue() };

			case 401:
				throw new RestServiceException(UserError.LOGIN_FAILED);

			default:
				throw new RestServiceException(UserError.ERROR);
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			throw new RestServiceException(UserError.LOGIN_FAILED);
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
