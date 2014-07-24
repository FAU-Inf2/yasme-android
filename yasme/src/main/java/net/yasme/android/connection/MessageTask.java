package net.yasme.android.connection;

import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import net.yasme.android.R;
import net.yasme.android.controller.Toaster;
import net.yasme.android.encryption.KeyEncryption;
import net.yasme.android.encryption.MessageEncryption;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Device;
import net.yasme.android.entities.Message;
import net.yasme.android.entities.MessageKey;
import net.yasme.android.entities.User;
import net.yasme.android.exception.Error;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.storage.dao.ChatDAO;
import net.yasme.android.storage.dao.MessageKeyDAO;
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
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageTask extends ConnectionTask {

	private static MessageTask instance;
    private MessageKeyDAO keyDAO = DatabaseManager.INSTANCE.getMessageKeyDAO();
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

    /**
     * Send the given message to the server. It's id is generated by the server and send back
     * @param message to send
     * @return given message with generated id
     * @throws RestServiceException
     */
	public Message sendMessage(Message message, Chat chat, User user) throws RestServiceException {
		return sendMessage(message,chat,user,false);
	}

    public Message sendMessage(Message message, Chat chat, User user, boolean forceKeyGeneration) throws RestServiceException {
        Message unencrypted = null;
        try {
            if (message == null) {
                return null;
            }
            unencrypted = new Message(message.getSender(),message.getMessage(),message.getChat(),message.getMessageKeyId());

            // Encrypt
            MessageEncryption messageEncryption = new MessageEncryption(chat,user);
            if (forceKeyGeneration) {
                message = messageEncryption.encryptGenerated(message);
            } else {
                message = messageEncryption.encrypt(message);
            }

            // Send
            HttpResponse response = executeRequest(Request.POST, "", message);
            String json = (new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent(), "UTF-8")
            )).readLine();

            Log.e(this.getClass().getSimpleName(), "JsonOut: " + json);

            JSONObject messageObj = new JSONObject(json);
            JSONObject senderObj = messageObj.getJSONObject("sender");
            message.setId(messageObj.getLong("id"));

            User sender = new User();
            sender.setId(senderObj.getLong("id"));
            message.setSender(sender);
            return message;

        } catch (RestServiceException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            if (e.getStatusCode() == Error.OUTDATED.getNumber() && !forceKeyGeneration) {
                Log.e(this.getClass().getSimpleName(), "Try again with generated key.");
                return sendMessage(unencrypted,chat,user,true);
            } else {
                Log.e(this.getClass().getSimpleName(), "No more ideas");
                return null;
            }
        } catch (IOException | JSONException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
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

                // Get chat and sender from database
                Chat chat = chatDAO.get(chatId);
                User sender = userDAO.get(senderId);

				/* extracting Keys and save it */
				JSONObject key;
				try {
					key = messageObj.getJSONObject("messageKey");
				} catch (Exception e) { key = null; }

				if (key != null) {
                    
					JSONObject creatorDevice = key.getJSONObject("creatorDevice");
                    JSONObject recipientDevice = key.getJSONObject("recipientDevice");

                    MessageKey messageKeyEncrypted = new MessageKey(
                            key.getLong("id"),
                            new Device(creatorDevice.getLong("id")),
                            new Device(recipientDevice.getLong("id")),
                            chat,
                            key.getString("messageKey"),
                            key.getString("initVector"),
                            (byte)key.getInt("encType"),
                            key.getString("Sign"));

                    KeyEncryption keyEncryption = new KeyEncryption();

                    //verify the signature of the key and save authenticity-status in messageKeyEncrypted
                    if(messageKeyEncrypted.setAuthenticity(keyEncryption.verify(messageKeyEncrypted))){
                        Log.d(this.getClass().getSimpleName(), "[???] MessageKey has successfully been verified");
                        Toaster.getInstance().toast(R.string.authentication_successful, Toast.LENGTH_LONG);
                    }else{
                        Log.d(this.getClass().getSimpleName(), "[???] MessageKey could not be verified");
                        Toaster.getInstance().toast(R.string.authentication_failed, Toast.LENGTH_LONG);
                    }

					//decrypt the key with RSA
					MessageKey messageKey = keyEncryption.decrypt(messageKeyEncrypted);

                    Log.d(this.getClass().getSimpleName(), "[???] MessageKey was decrypted: "+ messageKey.getMessageKey());

                    Date created = new Date(key.getLong("created"));
                    Log.d(getClass().getSimpleName(), "Key created: " + created.toString());
                    messageKey.setCreated(created);

                    // TODO: storeKeyToDatabase
                    keyDAO.addIfNotExists(messageKey);
					Log.d(this.getClass().getSimpleName(), "[???] Key " + keyId + " aus den Nachrichten extrahiert und gespeichert");

					// Delete Key from Server
					// TODO: Remove comment
					//new DeleteMessageKeyTask().execute(keyId);
				} else {
					Log.d(this.getClass().getSimpleName(), "[???] Es wurde kein Key in der Message gefunden");
				}

				Message msg = new Message(
                    Long.valueOf(messageObj.getString("id")),
                    new Date(messageObj.getLong("dateSent")),
                    sender,
                    messageObj.getString("message"),
                    chat,
                    keyId
				);
                MessageEncryption messageEncryption = new MessageEncryption(chat,sender);
                msg = messageEncryption.decrypt(msg);
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
