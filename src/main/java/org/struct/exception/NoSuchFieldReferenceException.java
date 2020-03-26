package org.struct.exception;

public class NoSuchFieldReferenceException extends RuntimeException {
    private static final long serialVersionUID = 19364548129304586L;

    public NoSuchFieldReferenceException() {
        super();
    }

    public NoSuchFieldReferenceException(String message) {
        super(message);
    }

    public NoSuchFieldReferenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchFieldReferenceException(Throwable cause) {
        super(cause);
    }

    protected NoSuchFieldReferenceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
