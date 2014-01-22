package idiro.workflow.server;

import idiro.workflow.server.enumeration.DisplayType;

import java.rmi.RemoteException;

public class BrowserInteraction extends UserInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4244649045214883613L;

	public BrowserInteraction(String id, String name, String legend,
			int column, int placeInColumn) throws RemoteException {
		super(id, name, legend, DisplayType.browser, column, placeInColumn);
	}

}
