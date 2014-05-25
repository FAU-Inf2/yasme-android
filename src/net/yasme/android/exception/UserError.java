package net.yasme.android.exception;

public enum UserError implements ErrorCode {

	USER_NOT_FOUND(101), PASSWORD_INCORRECT(102);

	private final int number;

	private UserError(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}
}
