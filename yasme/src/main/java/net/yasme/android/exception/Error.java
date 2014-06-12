package net.yasme.android.exception;

public enum Error implements ErrorCode {

    // To Do: global error codes must be determined
    ERROR(000), CONNECTION_ERROR(500), STORE_FAILED_EXCEPTION(501), NOT_FOUND_EXCEPTION(502), UNAUTHORIZED(503);

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
