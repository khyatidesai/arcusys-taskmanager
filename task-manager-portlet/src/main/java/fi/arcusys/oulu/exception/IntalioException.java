package fi.arcusys.oulu.exception;

public class IntalioException extends TaskMgrException {

	private static final long serialVersionUID = 1L;

	public IntalioException(Throwable cause) {
		super("FAIL!", cause);
	}

	public IntalioException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public IntalioException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

}
