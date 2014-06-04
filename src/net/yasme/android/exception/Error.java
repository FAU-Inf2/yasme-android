package net.yasme.android.exception;

public enum Error implements ErrorCode {

    // To Do: global error codes must be determined
    CONNECTION_ERROR(500), STORE_CHAT_EXCEPTION(501);

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
