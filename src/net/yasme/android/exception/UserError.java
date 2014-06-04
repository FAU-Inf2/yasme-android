package net.yasme.android.exception;

public enum UserError implements ErrorCode {

    // To Do: global error codes must be determined
    ERROR(000), LOGIN_FAILED(100), LOGOUT_FAILED(101), USER_NOT_FOUND(102), CHAT_NOT_FOUND_EXCEPTION(103), PASSWORD_INCORRECT(104), REGISTRATION_FAILED(
            105);

    private final int number;

    private UserError(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public String getDescription() {
        return name();
    }
}
