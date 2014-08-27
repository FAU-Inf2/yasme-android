package de.fau.cs.mad.yasme.android.encryption;

import de.fau.cs.mad.yasme.android.controller.Log;
import android.widget.Toast;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.connection.ChatTask;
import de.fau.cs.mad.yasme.android.connection.MessageKeyTask;
import de.fau.cs.mad.yasme.android.controller.Toaster;
import de.fau.cs.mad.yasme.android.entities.Chat;
import de.fau.cs.mad.yasme.android.entities.Device;
import de.fau.cs.mad.yasme.android.entities.Message;
import de.fau.cs.mad.yasme.android.entities.MessageKey;
import de.fau.cs.mad.yasme.android.entities.User;
import de.fau.cs.mad.yasme.android.exception.*;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;


import java.util.ArrayList;
import java.util.List;

public class MessageEncryption {
    Chat chat;
    User creator;
    private DatabaseManager db = DatabaseManager.INSTANCE;

    public MessageEncryption(Chat chat, User creator) {
        this.chat = chat;
        this.creator = creator;
    }

    public MessageKey getCurrentKey() {
        try {
            Log.d(this.getClass().getSimpleName(),"Try to use a local Key");
            MessageKey key = db.getMessageKeyDAO().getCurrentKeyByChat(chat.getId());
            if (key != null) {
                Log.d(this.getClass().getSimpleName(),"... success");
                return key;
            } else {
                Log.d(this.getClass().getSimpleName(),"... failed");
                return generateKey();
            }
        }
        catch (Exception e){
            Log.e(this.getClass().getSimpleName(),"... failed with exception");
            return null;
        }
    }

    public MessageKey generateKey() {
        Log.d(this.getClass().getSimpleName(),"Generate Key");
        Toaster.getInstance().toast(R.string.generate_key, Toast.LENGTH_LONG);

        // Generate key
        AESEncryption aes = new AESEncryption();
        String key = aes.getKeyinBase64();
        String iv = aes.getIVinBase64();

        return sendKey(key, iv, true);
    }

    public MessageKey getKey(long keyId) {
        Log.d(getClass().getSimpleName(), "Get key from DB: " + keyId);
        return db.getMessageKeyDAO().get(keyId);
    }

    // encrypt
    public Message encrypt(Message message) {
        return encrypt(message,getCurrentKey());
    }

    public Message encryptGenerated(Message message) {
        return encrypt(message,generateKey());
    }

    private Message encrypt (Message message, MessageKey messageKey) {
        if (messageKey == null) {
            Log.e(getClass().getSimpleName(), "Message could not be encrypted");
            Toaster.getInstance().toast(R.string.key_generation_failed,Toast.LENGTH_LONG);
            return null;
        }
        AESEncryption aes = new AESEncryption(messageKey);
        message.setMessage(aes.encrypt(message.getMessage()));
        message.setMessageKeyId(messageKey.getId());
        return message;
    }

    // decrypt
    public Message decrypt(Message message) {
        MessageKey messageKey = getKey(message.getMessageKeyId());
        if (messageKey == null) {
            message.setMessage("");
            message.setErrorId(ErrorType.DECRYPTION_FAILED);
            return message;
        }
        AESEncryption aes = new AESEncryption(messageKey);
        message.setMessage(aes.decrypt(message.getMessage()));

        //is the message successfully authenticated?
        message.setAuthenticity(messageKey.getAuthenticity());
        if (message.getErrorId() == 0 && !messageKey.getAuthenticity()) {
            message.setErrorId(ErrorType.AUTHENTICATION_FAILED);
        }

        return message;
    }

    private List<Device> getRecipientDevices(boolean local) {
        if (local) {
            Log.d(this.getClass().getSimpleName(),"Get local stored devices");
            List<Device> devices = new ArrayList<>();
            for (User user : chat.getParticipants()) {
                for (Device device : DatabaseManager.INSTANCE.getDeviceDAO().getAll(user)) {
                    Log.d(this.getClass().getSimpleName(),"Local-device: " + device.getId());
                    devices.add(device);
                }
            }
            return devices;
        } else {
            try {
                Log.d(this.getClass().getSimpleName(),"Get devices from Server");
                return ChatTask.getInstance().getAllDevicesForChat(chat.getId());
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(),e.getMessage());
                return new ArrayList<>();
            }

        }
    }

    public MessageKey sendKey(String key, String iv, boolean local) {
        try {
            long deviceId = DatabaseManager.INSTANCE.getDeviceId();
            Device sender = new Device(deviceId);
            ArrayList<MessageKey> messageKeys = new ArrayList<MessageKey>();
            List<Device> devices = getRecipientDevices(local);
            if (local && devices.size() == 0) {
                getRecipientDevices(false);
            }

            for (Device recipientDevice : getRecipientDevices(local)) {
                Log.d(this.getClass().getSimpleName(),"Send Key for Device" + recipientDevice.getId() + " with pubKey: " + recipientDevice.getPublicKey());


                // Do not store the key on the server for the creating device
                if (recipientDevice.getId() == deviceId) {
                    continue;
                }

                Log.d(this.getClass().getSimpleName(),"Generate Key for Device" + recipientDevice.getId());

                MessageKey messageKey = new MessageKey(0, sender, recipientDevice, chat, key, iv);
                KeyEncryption keyEncryption = new KeyEncryption();
                MessageKey messageKeyEncrypted = keyEncryption.encrypt(messageKey);
                Log.d(this.getClass().getSimpleName(), "MessageKey has successfully been encrypted.");
                MessageKey messageKeySigned = keyEncryption.sign(messageKeyEncrypted);
                Log.d(this.getClass().getSimpleName(), "MessageKey has successfully been signed.");

                messageKeys.add(messageKeySigned);
                Log.d(this.getClass().getSimpleName(),"Key from " + deviceId + " for device " + recipientDevice.getId() + "created");
            }

            MessageKey result = MessageKeyTask.getInstance().saveKeys(messageKeys);

            if (result == null) {
                Log.d(this.getClass().getSimpleName(),"Error sending key to server");
                return null;
            }

            MessageKey messageKey = new MessageKey(result.getId(),sender,sender,chat,key,iv);
            messageKey.setCreated(result.getCreated());

            Log.d(this.getClass().getSimpleName(),"Key was send to server, id is: "+ messageKey.getId());

            // If you can trust yourself
            messageKey.setAuthenticity(true);
            db.getMessageKeyDAO().addIfNotExists(messageKey);
            return messageKey;

        } catch (IncompleteKeyException e) {
            if (local) {
                Log.e(this.getClass().getSimpleName(),"Create key");
                return sendKey(key,iv,false);
            } else {
                Log.e(this.getClass().getSimpleName(),"Key was not sent to server");
                return null;
            }
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(),"Failed to send key: "+e.getMessage());
            return null;
        }
    }

    public static class ErrorType {
        public static final int OK = 0;
        public static final int DECRYPTION_FAILED = 10;
        public static final int AUTHENTICATION_FAILED = 20;
    }
}

