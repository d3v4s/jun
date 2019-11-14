package it.jun.exception;

/**
 * Class for file lock exception
 * @author Andrea Serra
 *
 */
public class FileLockException extends Exception {
	private static final long serialVersionUID = 4311572510610926146L;

	public FileLockException() {
	}

	public FileLockException(String message) {
		super(message);
	}

	public FileLockException(Throwable cause) {
		super(cause);
	}

	public FileLockException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileLockException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
