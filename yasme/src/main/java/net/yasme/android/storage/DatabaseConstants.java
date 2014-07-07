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

    //Message
    public static final String MESSAGE_TABLE = "messages";
    public static final String MESSAGE_ID = "messageId";
    public static final String CHAT = "messageChat";
    public static final String SENDER = "sender";
    public static final String DATE = "date";
    public static final String MESSAGE = "message";

    //User
    public static final String USER_TABLE = "users";
    public static final String USER_ID = "userId";
    public static final String USER_NAME = "userName";
    public static final String CONTACT = "contactFlag";

    //ChatUser
    public static final String CHAT_USER_TABLE = "chatUsers";
    public static final String USER_FIELD_NAME = "userObject";
    public static final String CHAT_FIELD_NAME = "chatObject";

    //MessageKey
    public static final String MESSAGE_KEY_TABLE = "messageKeys";
    public static final String VECTOR = "initialVector";
    public static final String KEY_ID = "keyId";
    public static final String KEY = "key";
    public static final String TIMESTAMP = "time";
    public static final String KEY_CHAT = "messageKeyChat";

    //CurrentKey
    public static final String CURRENT_KEY_TABLE = "currentKeyTable";
    public static final String CURRENT_KEY_CHAT = "currentKeyChat";
    public static final String CURRENT_KEY = "currentKey";
    public static final String CURRENT_KEY_ID = "currentKeyId";

}
