package net.yasme.android.asyncTasks.server;

import android.os.AsyncTask;
import android.util.Log;

import net.yasme.android.controller.ObservableRegistry;
import net.yasme.android.entities.Chat;
import net.yasme.android.storage.DatabaseManager;
import net.yasme.android.ui.ChatActivity;
import net.yasme.android.connection.MessageTask;
import net.yasme.android.encryption.MessageEncryption;
import net.yasme.android.entities.Message;
import net.yasme.android.entities.User;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.ui.ChatFragment;
import net.yasme.android.ui.InviteToChatFragment;

/**
 * Created by robert on 19.06.14.
 */
public class SendMessageTask extends AsyncTask<Message, Void, Boolean> {

    MessageEncryption aes;

    public SendMessageTask(MessageEncryption aes) {
        this.aes = aes;
    }

    MessageTask messageTask = MessageTask.getInstance();

    String msg;

    /**
     *
     * @param msgs
     *              0 is message
     *              1 is userName
     *              2 is userMail
     *              3 is userId
     *              4 is chatId
     *              5 is accessToken
     * @return true on success and false on error
     */
    protected Boolean doInBackground(Message... msgs) {
/*
        msg = params[0];

        String uName = params[1];
        String uMail = params[2];
        long uId = Long.parseLong(params[3]);
        long chatId = Long.parseLong(params[4]);

        // encrypt Message
        //String msgEncrypted = aes.encrypt(msg); //TODO
        String msgEncrypted = msg;
        Log.d(this.getClass().getSimpleName(), "Zu sendende Nachricht: " + msgEncrypted);

        // create Message
        User user = new User(uName, uMail,  uId);
        long aesId = aes.getKeyId();
        Chat chat = new Chat();
        chat.setId(chatId);
        // chat.setParticipants(DatabaseManager.INSTANCE.getParticipantsForChat(chatId));

        Message createdMessage = new Message(user, msgEncrypted, chat, aesId);
        Log.d(this.getClass().getSimpleName(), "AES getKeyID: " + aes.getKeyId());
*/
        /*Log.d(this.getClass().getSimpleName(), "[Debug] " + createdMessage.getMessage() +
                ", " + createdMessage.getId() + ", " + createdMessage.getChatId());
        /*DEBUG*/
/*
        if(createdMessage == null) {
            Log.d(this.getClass().getSimpleName(), "1 createdMessage is null!");
        }
        if(user == null) {
            Log.d(this.getClass().getSimpleName(), "2 createdMessage is null!");
        }
        if(msgEncrypted == null) {
            Log.d(this.getClass().getSimpleName(), "3 createdMessage is null!");
        }
        if(chatId == 0) {
            Log.d(this.getClass().getSimpleName(), "4 createdMessage is null!");
        }
        if(aesId == 0) {
            Log.d(this.getClass().getSimpleName(), "5 createdMessage is null!");
        }
        Log.e(this.getClass().getSimpleName(), msgEncrypted + " " + user.getId() + " " +
                chatId + " " + aesId);
*/
        /*DEBUG END*/
/*
        try {
            if(createdMessage == null) {
                Log.e(this.getClass().getSimpleName(), "createdMessage is null!");
            }
            messageTask.sendMessage(createdMessage);
            return true;
        } catch (RestServiceException e) {
            e.printStackTrace();
            Log.w(this.getClass().getSimpleName(), e.getMessage());
        }
        return false;
*/
			Message msg = msgs[0];
			try {
				if(null!=msg) Log.e(this.getClass().getSimpleName(),"Received message is null!");
				Message ret = messageTask.sendMessage(msg);
				if(null!=DatabaseManager.INSTANCE.getMessageDAO().addIfNotExists(ret)) return false;
				return true;
			} catch (RestServiceException rse) {
				rse.printStackTrace();
				Log.w(this.getClass().getSimpleName(),rse.getMessage());
			}
			return false;
    }

    protected void onPostExecute(final Boolean success) {
        if (success) {
            Log.i(this.getClass().getSimpleName(), "Gesendet: " + msg);
            // TODO ObservableRegistry.getObservable(ChatFragment.class).notifyFragments(msg);
        } else {
            Log.w(this.getClass().getSimpleName(), "Senden fehlgeschlagen");
        }
    }
}
