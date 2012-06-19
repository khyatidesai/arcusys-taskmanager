package fi.arcusys.oulu.exception;

import java.util.UUID;

/**
 * TaskMgrException
 *
 * General KokuException
 *
 * @author Toni Turunen
 */
public class TaskMgrException extends Exception {

	private static final long serialVersionUID = 1L;

	public static String generateErrorCode() {
		return UUID.randomUUID().toString();
	}

	private final String uuid;

	public TaskMgrException(String message) {
		super(message);
		uuid = generateErrorCode();
	}

	public TaskMgrException(String message, Throwable cause) {
		super(message, cause);
		uuid = generateErrorCode();
	}

	private final String getErrorCode() {
		return "Unique TaskMgr error code: '" + uuid + "'. ";
	}

	@Override
	public String getMessage() {
		return getErrorCode() + super.getMessage();
	}

	public final String getErrorcode() {
		return uuid;
	}
}
