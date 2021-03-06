package de.fau.cs.mad.yasme.android.exception;

/**
 * Created by Florian Winklmeier <f.winklmeier@t-online.de> on 08.08.2014.
 */

public enum Error implements ErrorCode {

    ERROR(000),

    GET_NO_NEW_MESSAGE(001),

    BAD_REQUEST(400),

    LOGIN_FAILED(401),

    UNAUTHORIZED(401),

    FORBIDDEN(403),

    NOT_FOUND_EXCEPTION(404),

    USER_NOT_FOUND(404),

    NOT_ACCEPTABLE(406),

    OUTDATED(409),

    INCOMPLETE_REQUEST(420),

    BAD_EMAIL(460),

    STORE_USER_FAILED(500),

    CONNECTION_ERROR(500),

    SEND_MESSAGE_FAILED(500),

    SERVER_ERROR(500),

    REGISTRATION_FAILED(500),

    STORE_FAILED_EXCEPTION(501);

    private final int number;

    private Error(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public String getDescription() {
        return name();
    }
}
