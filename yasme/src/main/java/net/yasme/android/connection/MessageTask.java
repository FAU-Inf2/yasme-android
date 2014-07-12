package net.yasme.android.connection;

import android.util.Log;

import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Message;
import net.yasme.android.entities.User;
import net.yasme.android.exception.Error;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.storage.dao.ChatDAO;
import net.yasme.android.storage.dao.UserDAO;

import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageTask extends ConnectionTask {

	private static MessageTask instance;
    private ChatDAO chatDAO = DatabaseManager.INSTANCE.getChatDAO();
    private UserDAO userDAO = DatabaseManager.INSTANCE.getUserDAO();

	public static MessageTask getInstance() {
		if (instance == null) { instance = new MessageTask(); }
		return instance;
	}

	private MessageTask() {
		try {
			this.uri = new URIBuilder(baseURI).setPath(ConnectionTask.APIVERSION + "/msg").build();
		} catch (URISyntaxException e) { e.printStackTrace(); }
	}

	public void sendMessage(Message message) throws RestServiceException {
		executeRequest(Request.POST, "", message);
		Log.d(this.getClass().getSimpleName(), "Message stored!");
	}

	public List<Message> getMessage(long lastMessageId) throws RestServiceException {
		List<Message> messages = new ArrayList<Message>();

		try {
			HttpResponse httpResponse = executeRequest(Request.GET, Long.toString(lastMessageId));
			String json = (new BufferedReader(
				new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8")
			)).readLine();

			JSONArray jsonArray = new JSONArray(json);

			Log.d(this.getClass().getSimpleName(),"getMessageRequest successful: " + jsonArray.length() + " new messages");// + json);

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject messageObj = jsonArray.getJSONObject(i);
				JSONObject senderObj = messageObj.getJSONObject("sender");
				JSONObject chatObj = messageObj.getJSONObject("chat");

				Log.d(this.getClass().getSimpleName(), "Message: " + messageObj.toString());

				long chatId = chatObj.getLong("id");
                long senderId = senderObj.getLong("id");
				long keyId = messageObj.getLong("messageKeyId");

				/* extracting Keys and save it */
				JSONObject key;
				try {
					key = messageObj.getJSONObject("messageKey");
				} catch (Exception e) { key = null; }

				if (key != null) {
					String messageKey = key.getString("messageKey");
					String iv = key.getString("initVector");
					//decrypt the key with RSA
					//TODO: statt userId deviceId uebergeben
					/*
						MessageSignatur rsa = new MessageSignatur(context, userId);
						String messageKey = rsa.decrypt(messageKeyEncrypted);
					*/

					long timestamp = key.getLong("timestamp");
					//MessageEncryption keyStorage = new MessageEncryption(context, chatId);

					//keyStorage.saveKey(obj.getLong("messageKeyId"), messageKey, iv, timestamp);
					// TODO: storeKeyToDatabase
					Log.d(this.getClass().getSimpleName(), "[???] Key " + keyId + " aus den Nachrichten extrahiert und gespeichert");

					// Delete Key from Server
					// TODO: Remove comment
					//new DeleteMessageKeyTask().execute(keyId);
				} else {
					Log.d(this.getClass().getSimpleName(), "[???] Es wurde kein Key in der Message gefunden");
				}

                // Get chat and sender from database
                Chat chat = chatDAO.get(chatId);
                User sender = userDAO.get(senderId);

				Message msg = new Message(
                    Long.valueOf(messageObj.getString("id")),
                    chat,
                    sender,
                    new Date(messageObj.getLong("dateSent")),
                    messageObj.getString("message"),
                    keyId
				);
				messages.add(msg);
				Log.d(this.getClass().getSimpleName(), "Message added: " + msg.getMessage());
			} // end of for-loop

		} catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (JSONException e) { e.printStackTrace(); }

		Log.d(this.getClass().getSimpleName(), "Number new Messages: " + messages.size());
		return messages;
	} // end of getMessage
}
