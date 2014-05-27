package net.yasme.android.connection;

import java.io.IOException;
import java.util.ArrayList;

import net.yasme.android.entities.Message;
import net.yasme.android.exception.MessageError;
import net.yasme.android.exception.RestServiceException;

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

	public boolean sendMessage(Message message) throws RestServiceException {

		try {

			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url + "/msg");

			JSONObject msg = new JSONObject();

			msg.put("sender", message.getSender());
			msg.put("recipient", message.getRecipient());
			msg.put("message", message.getMessage());

			String json = msg.toString();

			StringEntity se = new StringEntity(json);

			httpPost.setEntity(se);

			httpPost.setHeader("Content-type", "application/json");
			httpPost.setHeader("Accept", "application/json");

			HttpResponse httpResponse = httpclient.execute(httpPost);

			switch (httpResponse.getStatusLine().getStatusCode()) {
			case 201:
				return true;
			case 500:
				throw new RestServiceException(MessageError.SEND_MESSAGE_FAILED);
			default:
				throw new RestServiceException(MessageError.ERROR);
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			throw new RestServiceException(MessageError.SEND_MESSAGE_FAILED);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	public ArrayList<Message> getMessage(String lastMessageID) {

		ArrayList<Message> messages = new ArrayList<Message>();

		try {

			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(url + "/msg/" + lastMessageID);
			// HttpGet request = new HttpGet(url + "/msg/");

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

				// To Do: Rückgabewert vom Server zu klären

				/*
				 * JSONObject obj = jArray.getJSONObject(i); messages.add(new
				 * Message( Long.parseLong(obj.getString("sender")), Long
				 * .parseLong(obj.getString("recipient")), obj
				 * .getString("message")));
				 */

				// Temporäre Version:

				JSONObject obj = jArray.getJSONObject(i);
				JSONObject sender = obj.getJSONObject("sender");
				JSONObject recipient = obj.getJSONObject("recipient");

				messages.add(new Message(sender.getLong("id"), recipient
						.getLong("id"), obj.getString("message")));
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
