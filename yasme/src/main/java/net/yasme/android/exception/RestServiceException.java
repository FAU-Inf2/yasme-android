package net.yasme.android.exception;

public class RestServiceException extends Exception {

	private static final long serialVersionUID = 1L;
	private int code;

	public RestServiceException(ErrorCode error) {

		this(error.getDescription(), error.getNumber());

	}

	public RestServiceException(String message, int code) {
		super(message);
		this.code = code;

		System.err.println("Code: " + this.code + "  " + "Message: "
				+ this.getMessage());
	}

    public int getCode() {
        return code;
    }
}
