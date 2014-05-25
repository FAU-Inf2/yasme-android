package net.yasme.android.exception;

public class RestServiceException extends Exception {

	private static final long serialVersionUID = 1L;
	int statusCode;

	public RestServiceException(UserError error) {

		this(error.name(), error.getNumber());

	}

	private RestServiceException(String error, int statusCode) {
		super(error);
		this.statusCode = statusCode;

		System.out.println(this.statusCode);
	}

}
