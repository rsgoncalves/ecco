package uk.ac.manchester.cs.diff.exception;


public class InconsistentOntologyException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	
	/**
	 * Constructor
	 * @see RuntimeException#RuntimeException()
	 */
	public InconsistentOntologyException() {
		super();
	}
	
	
	/**
	 * Constructor
	 * @param s	Message
	 * @see RuntimeException#RuntimeException(String s)
	 */
	public InconsistentOntologyException(String s) {
		super(s);
	}
	
	
	/**
	 * Constructor
	 * @param s	Message
	 * @param throwable	Throwable
	 * @see RuntimeException#RuntimeException(String s, Throwable throwable)
	 */
	public InconsistentOntologyException(String s, Throwable throwable) {
		super(s, throwable);
	}
	
	
	/**
	 * Constructor
	 * @param throwable	Throwable
	 * @see RuntimeException#RuntimeException(Throwable throwable)
	 */
	public InconsistentOntologyException(Throwable throwable) {
		super(throwable);
	}
}
