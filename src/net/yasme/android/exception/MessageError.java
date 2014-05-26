package net.yasme.android.exception;

public enum MessageError implements ErrorCode {

	ERROR(000), SEND_MESSAGE_FAILED(100);

	private final int number;

	private MessageError(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}

	public String getDescription() {
		return name();
	}
}
