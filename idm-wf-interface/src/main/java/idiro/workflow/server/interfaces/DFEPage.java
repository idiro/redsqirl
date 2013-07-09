package idiro.workflow.server.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface DFEPage extends Remote{

	/**
	 * Check if a page is correctly implemented
	 * @return true if ok
	 */
	public String checkPage() throws RemoteException;
	
	/**
	 * Check if a page is correctly set up.
	 * @return true if ok
	 */
	public boolean checkInitPage() throws RemoteException;
	
	/**
	 * Add a user interaction
	 * @param e
	 * @return
	 */
	public boolean addInteraction(DFEInteraction e) throws RemoteException;

	/**
	 * Get the user interactions associated with a name
	 * @param name interaction name
	 * @return
	 */
	public DFEInteraction getInteraction(String name) throws RemoteException;

	/**
	 * @return the title
	 */
	public String getTitle() throws RemoteException;

	/**
	 * @return the nbColumn
	 */
	public int getNbColumn() throws RemoteException;

	/**
	 * @return the image path
	 */
	public String getImage() throws RemoteException;

	/**
	 * @return the legend
	 */
	public String getLegend() throws RemoteException;

	/**
	 * @return the interactions
	 */
	public List<DFEInteraction> getInteractions() throws RemoteException;	

	
	public boolean haveChecker() throws RemoteException;
	
}
