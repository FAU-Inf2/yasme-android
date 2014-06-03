package net.yasme.android.exception;

public enum UserError implements ErrorCode {

	// To Do: global error codes must be determined
	ERROR(000), LOGIN_FAILED(100), USER_NOT_FOUND(101), CHAT_NOT_FOUND_EXCEPTION(102), PASSWORD_INCORRECT(103), REGISTRATION_FAILED(
			104);

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
