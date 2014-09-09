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

/**
 * Created by Marco Eberl <mfrankie89@aol.de> on 22.07.2014.
 */

public class MessageEncryption {
    Chat chat;
    User creator;
    private DatabaseManager db = DatabaseManager.INSTANCE;

    /**
     * initialize Encryption for the given chat
     *
     * @param chat chat the encryption should be initialized for
     * @param creator user who is using the chat
     */
    public MessageEncryption(Chat chat, User creator) {
        this.chat = chat;
        this.creator = creator;
    }

    /**
     * For Encryption the latest generated key is needed
     * This method load the latest key from database
     *
     * @return MessageKey-Object containing the latest AES-Key with the corresponding InitalVector
     */
    public MessageKey getCurrentKey() {
        try {
            //Log.d(this.getClass().getSimpleName(),"Try to use a local Key");
            MessageKey key = db.getMessageKeyDAO().getCurrentKeyByChat(chat.getId());
            if (key != null) {
                //Log.d(this.getClass().getSimpleName(),"... success");
                return key;
            } else {
                //Log.d(this.getClass().getSimpleName(),"... failed");
                return generateKey();
            }
        }
        catch (Exception e){
            Log.e(this.getClass().getSimpleName(),"... failed with exception");
            return null;
        }
    }

    /**
     * generate a random AES-Key/InitialVector and encode it to base64
     * call method sendKey: send it to the server and save it locally
     *
     * @return MessageKey containing the generated AES-Key/Inital Vector and the Keyid the server has assigned
     */
    public MessageKey generateKey() {
        //Log.d(this.getClass().getSimpleName(),"Generate Key");
        Toaster.getInstance().toast(R.string.generate_key, Toast.LENGTH_LONG);

        // Generate key
        AESEncryption aes = new AESEncryption();
        String key = aes.getKeyinBase64();
        String iv = aes.getIVinBase64();

        return sendKey(key, iv, true);
    }

    /**
     * load AES-Key and Initial Vector for given KeyId
     *
     * @param keyId KeyId of the key that is needed
     * @return MessageKey-Object containing the needed AES-Key/InitalVector
     */
    public MessageKey getKey(long keyId) {
        //Log.d(getClass().getSimpleName(), "Get key from DB: " + keyId);
        return db.getMessageKeyDAO().get(keyId);
    }

    /**
     * encrypt a Message with the latest generated AES-Key loaded from database
     *
     * @param message message that should be encrypted
     * @return message-object containing the encrypted message and the used KeyId
     */
    public Message encrypt(Message message) {
        return encrypt(message,getCurrentKey());
    }

    /**
     * encrypt a Message and force to generate a new AES-Key
     *
     * @param message message that should be encrypted
     * @return message-object containing the encrypted message and the used KeyId
     */
    public Message encryptGenerated(Message message) {
        return encrypt(message,generateKey());
    }

    /**
     * encrypt a message with a given MessageKey
     *
     * @param message message that should be encrypted
     * @param messageKey messageKey that should be used for encryption
     * @return message-object containing the encrypted message and the used KeyId
     */
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

    /**
     * decrypt a message using the KeyId that is stored in the message-object
     * check, if the used Key is confidable
     *
     * @param message message that should be decrypted
     * @return message-object containing the decrypted message
     */
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

    /**
     * get all devices the generated messagekey need to be sent to (all devices from all participants in the chat)
     * that is necessary because a messagekey needs to be encrypted using RSA for every single device the key is sent to
     *
     * @param local define, if the devices should be loaded from local storage or if the devices should be loaded from the server
     * @return list of all devices
     */
    private List<Device> getRecipientwDevices(boolean local) {
        if (local) {
            //Log.d(this.getClass().getSimpleName(),"Get local stored devices");
            List<Device> devices = new ArrayList<>();
            for (User user : chat.getParticipants()) {
                for (Device device : DatabaseManager.INSTANCE.getDeviceDAO().getAll(user)) {
                    //Log.d(this.getClass().getSimpleName(),"Local-device: " + device.getId());
                    devices.add(device);
                }
            }
            return devices;
        } else {
            try {
                //Log.d(this.getClass().getSimpleName(),"Get devices from Server");
                return ChatTask.getInstance().getAllDevicesForChat(chat.getId());
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(),e.getMessage());
                return new ArrayList<>();
            }

        }
    }


    /**
     * this method send a generated Key/InitalVector to all devices of the chat participants
     * for every device the key needs to be encrypted using the public RSA-Key from the recipientDevice
     * this encrypted key also is signed by the private key of the creatorDevice (sender)
     *
     * when the server received the encrypted key for all known devices, he returns a messageKey-Object with an assigned KeyId
     * now the method saves the generated Key with this KeyId on the local database
     *
     * @param key generated AES-Key encoded in base64
     * @param iv generated InitalVector encoded in base64
     * @param local define, if the devices should be loaded from local storage or if the devices should be loaded from the server
     * @return  MessageKey-Object containing the assigned KeyId and Timestamp for the generated Key
     *          null, if sending/saving key was not successful
     */
    public MessageKey sendKey(String key, String iv, boolean local) {
        try {
            long deviceId = DatabaseManager.INSTANCE.getDeviceId();
            Device sender = new Device(deviceId);
            ArrayList<MessageKey> messageKeys = new ArrayList<MessageKey>();
            List<Device> devices = getRecipientwDevices(local);
            if (local && devices.size() == 0) {
                getRecipientwDevices(false);
            }

            for (Device recipientDevice : getRecipientwDevices(local)) {
                //Log.d(this.getClass().getSimpleName(),"Send Key for Device" + recipientDevice.getId() + " with pubKey: " + recipientDevice.getPublicKey());


                // Do not store the key on the server for the creating device
                if (recipientDevice.getId() == deviceId) {
                    continue;
                }

                //Log.d(this.getClass().getSimpleName(),"Generate Key for Device" + recipientDevice.getId());

                MessageKey messageKey = new MessageKey(0, sender, recipientDevice, chat, key, iv);
                KeyEncryption keyEncryption = new KeyEncryption();
                MessageKey messageKeyEncrypted = keyEncryption.encrypt(messageKey);
                //Log.d(this.getClass().getSimpleName(), "MessageKey has successfully been encrypted.");
                MessageKey messageKeySigned = keyEncryption.sign(messageKeyEncrypted);
                //Log.d(this.getClass().getSimpleName(), "MessageKey has successfully been signed.");

                messageKeys.add(messageKeySigned);
                //Log.d(this.getClass().getSimpleName(),"Key from " + deviceId + " for device " + recipientDevice.getId() + "created");
            }

            MessageKey result = MessageKeyTask.getInstance().saveKeys(messageKeys);

            if (result == null) {
                //Log.d(this.getClass().getSimpleName(),"Error sending key to server");
                return null;
            }

            MessageKey messageKey = new MessageKey(result.getId(),sender,sender,chat,key,iv);
            messageKey.setCreated(result.getCreated());

            //Log.d(this.getClass().getSimpleName(),"Key was send to server, id is: "+ messageKey.getId());

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

