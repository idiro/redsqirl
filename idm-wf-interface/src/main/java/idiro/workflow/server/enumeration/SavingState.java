package idiro.workflow.server.enumeration;

/**
 * Enumerations to identify save state of output data
 * 
 * @author keith
 * 
 */
public enum SavingState {
	/**
	 * Stores the data temporarily
	 */
	TEMPORARY,
	/**
	 * Stores data until previous changed
	 */
	BUFFERED,
	/**
	 * Records data permanently
	 */
	RECORDED
}
