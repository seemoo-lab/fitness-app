package com.fitbit.api;

public class FitbitAPISecurityException extends FitbitAPIException {

	private static final long serialVersionUID = 647984493835994436L;

    public FitbitAPISecurityException(String msg) {
        super(msg);
    }

    public FitbitAPISecurityException(Exception cause) {
        super(cause);
    }

    public FitbitAPISecurityException(String msg, int statusCode) {
        super(msg, statusCode);
    }

    public FitbitAPISecurityException(String msg, Exception cause) {
        super(msg, cause);
    }

    public FitbitAPISecurityException(String msg, Exception cause, int statusCode) {
        super(msg, cause, statusCode);

    }

}
