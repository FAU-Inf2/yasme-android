package net.yasme.android.storage;

/**
 * Created by robert on 15.06.14.
 */
public class DatabaseConstants {

    //Chat
    public static final String CHAT_TABLE = "chats";
    public static final String CHAT_ID = "chatId";
    public static final String CHAT_NAME = "chatName";
    public static final String CHAT_STATUS = "chatStatus";
    public static final String MESSAGES = "messages";
    public static final String OWNER = "owner";
    public static final String CHAT_LAST_MODIFIED = "lastModified";
    public static final String CHAT_CREATED = "created";

    //Message
    public static final String MESSAGE_TABLE = "messages";
    public static final String MESSAGE_ID = "messageId";
    public static final String CHAT = "messageChat";
    public static final String SENDER = "sender";
    public static final String DATE = "date";
    public static final String MESSAGE = "message";
    public static final String MESSAGE_MESSAGEKEY_ID = "messageKeyId";

    //User
    public static final String USER_TABLE = "users";
    public static final String USER_ID = "userId";
    public static final String USER_NAME = "userName";
    public static final String USER_EMAIL = "email";
    public static final String CONTACT = "contactFlag";
    public static final String USER_CREATED = "created";
    public static final String USER_LAST_MODIFIED = "lastModified";

    //ChatUser
    public static final String CHAT_USER_TABLE = "chatUsers";
    public static final String USER_FIELD_NAME = "userObject";
    public static final String CHAT_FIELD_NAME = "chatObject";

    //MessageKey
    public static final String MESSAGE_KEY_TABLE = "messageKeys";
    public static final String VECTOR = "initialVector";
    public static final String KEY_ID = "keyId";
    public static final String KEY = "key";
    public static final String KEY_CREATED = "created";
    public static final String KEY_CHAT = "messageKeyChat";

    //RSAKey
    public static final String RSA_KEY_TABLE = "rsaKeys";
    public static final String RSA_KEY_USER = "rsaKeyUser";
    public static final String RSA_KEY_PUBLIC_KEY = "rsaKeyPublicKey";
    public static final String RSA_KEY_ID = "rsaKeyId";
    public static final String RSA_KEY_DEVICE_ID = "rsaKeyDeviceId";


}
