package net.yasme.android.storage;

/**
 * Created by robert on 15.06.14.
 */
public class DatabaseConstants {

    // name of the database file
    public static final String DATABASE = "net.yasme.android.DATABASE";

    //Chat
    public static final String CHAT_TABLE = "chat";
    public static final String CHAT_ID = "chatId";
    public static final String CHAT_NAME = "chatName";
    public static final String CHAT_STATUS = "chatStatus";
    public static final String MESSAGES = "messages";
    public static final String OWNER = "owner";

    //Message
    public static final String MESSAGE_ID = "messageId";
    public static final String CHAT = "chat";
    public static final String SENDER = "sender";
    public static final String DATE = "date";
    public static final String MESSAGE = "message";

    //User
    public static final String USER_ID = "userId";
    public static final String USER_MAIL = "userMail";
    public static final String USER_NAME = "userName";
    public static final String CONTACT = "contactFlag";

    //ChatUser
    public static final String USER_FIELD_NAME = "userObject";
    public static final String CHAT_FIELD_NAME = "chatObject";

}
