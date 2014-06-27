package net.yasme.android.exception;

public class RestServiceException extends Exception {

	private static final long serialVersionUID = 1L;
	int statusCode;

	public RestServiceException(ErrorCode error) {

		this(error.getDescription(), error.getNumber());

	}

	public RestServiceException(String error, int statusCode) {
		super(error);
		this.statusCode = statusCode;

		System.err.println("Code: " + this.statusCode + "  " + "Message: "
				+ this.getMessage());
	}
}
