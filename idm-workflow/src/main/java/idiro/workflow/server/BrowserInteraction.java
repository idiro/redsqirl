package idiro.workflow.server;

import idiro.workflow.server.enumeration.DisplayType;

import java.rmi.RemoteException;
/**
 * Implent a browser interaction 
 * @author keith
 *
 */
public class BrowserInteraction extends UserInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4244649045214883613L;
	/**
	 * Constructor for the browser
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @throws RemoteException
	 */
	public BrowserInteraction(String id, String name, String legend,
			int column, int placeInColumn) throws RemoteException {
		super(id, name, legend, DisplayType.browser, column, placeInColumn);
	}

}
