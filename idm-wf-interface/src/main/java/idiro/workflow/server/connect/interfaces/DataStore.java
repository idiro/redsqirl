package idiro.workflow.server.connect.interfaces;

import idiro.workflow.server.enumeration.FeatureType;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * Interface for browsing data. An historic of actions is kept in order to go on
 * previous location. Also some properties can be change on objects.
 * 
 * @author etienne
 * 
 */
public interface DataStore extends Remote {

	/**
	 * Associate to a property information. It is needed for editing or create
	 * an element
	 * 
	 * @author etienne
	 * 
	 */
	public interface ParamProperty extends Remote {

		/**
		 * Get a description of the property.
		 * 
		 * @return help description of property
		 * @throws RemoteException
		 */
		String getHelp() throws RemoteException;

		/**
		 * True if the property cannot be change.
		 * 
		 * @return <code>true</code> if the property cannot be changed else
		 *         <code>false</code>
		 * @throws RemoteException
		 */
		boolean isConst() throws RemoteException;

		/**
		 * True if the property can be edited.
		 * 
		 * @return <code>true</code> if the property can be edited else
		 *         <code>false</code>
		 * @throws RemoteException
		 */
		boolean editOnly() throws RemoteException;

		/**
		 * Property that appears only for creating
		 * 
		 * @return <code>true</code> if property is only for creating else <code>false</code>
		 * @throws RemoteException
		 */
		boolean createOnly() throws RemoteException;

		/**
		 * Type associated to the property
		 * 
		 * @return {@link idiro.workflow.server.enumeration.FeatureType} of property
		 * @throws RemoteException
		 */
		FeatureType type() throws RemoteException;
	}
	
	/**
	 * Name of the browser, it has to be unique for each class.
	 * @return
	 * @throws RemoteException
	 */
	public String getBrowserName() throws RemoteException;

	/**
	 * Open the connection to the datastore
	 * 
	 * @return null if OK or the error
	 * @throws RemoteException
	 */
	String open() throws RemoteException;

	/**
	 * Close the connection to the datastore
	 * 
	 * @return null if OK or the error
	 * @throws RemoteException
	 */
	String close() throws RemoteException;

	/**
	 * Current Path
	 * 
	 * @return path that is stored currently
	 * @throws RemoteException
	 */
	String getPath() throws RemoteException;

	/**
	 * Default Path where to start from next time
	 * 
	 * @return path default
	 * @throws RemoteException
	 */
	void setDefaultPath(String path) throws RemoteException;

	/**
	 * Go to the given path if exists
	 * 
	 * @param path
	 * @return <code>true</code> if successful else <code>false</code>
	 * @throws RemoteException
	 */
	boolean goTo(String path) throws RemoteException;

	/**
	 * True if there is at least one previous path
	 * 
	 * @return <code>true</code> if there is a previous path else <code>false</code>
	 * @throws RemoteException
	 */
	boolean havePrevious() throws RemoteException;

	/**
	 * Go to the previous selected path
	 * 
	 * @throws RemoteException
	 */
	void goPrevious() throws RemoteException;

	/**
	 * True if there is at least one next path
	 * 
	 * @return @return <code>true</code> if there is a next path else <code>false</code>
	 * @throws RemoteException
	 */
	boolean haveNext() throws RemoteException;

	/**
	 * Go to the next selected path
	 * 
	 * @throws RemoteException
	 */
	void goNext() throws RemoteException;

	/**
	 * Get the properties. Get the properties associated with the children of
	 * the current path.
	 * 
	 * @return {@link java.util.Map<String, ParamProperty>} of paramater properties
	 * @throws RemoteException
	 */
	Map<String, ParamProperty> getParamProperties() throws RemoteException;

	/**
	 * Create a datastore element.
	 * 
	 * @param path
	 *            path to create
	 * @param properties
	 *            properties of the new element
	 * @return true if created successfully
	 * @throws RemoteException
	 */
	String create(String path, Map<String, String> properties)
			throws RemoteException;

	/**
	 * Move a datastore element
	 * 
	 * @param old_path
	 * @param new_path
	 * @return Error message
	 * @throws RemoteException
	 */
	String move(String old_path, String new_path) throws RemoteException;

	/**
	 * Copy a datastore element
	 * 
	 * @param in_path
	 * @param out_path
	 * @return Error message
	 * @throws RemoteException
	 */
	String copy(String in_path, String out_path) throws RemoteException;

	/**
	 * Copy a datastore element from a remote server
	 * 
	 * @param in_path
	 * @param out_path
	 * @return Error message
	 * @throws RemoteException
	 */
	String copyFromRemote(String in_path, String out_path, String remoteServer)
			throws RemoteException;

	/**
	 * Copy a datastore element to a remote server
	 * 
	 * @param in_path
	 * @param out_path
	 * @return Error message
	 * @throws RemoteException
	 */
	String copyToRemote(String in_path, String out_path, String remoteServer)
			throws RemoteException;

	/**
	 * Delete a datastore element
	 * 
	 * @param path
	 *            path to delete
	 * @return true if deleted successfully
	 * @throws RemoteException
	 */
	String delete(String path) throws RemoteException;

	/**
	 * Select from the given path the n first elements with a delimiter.
	 * 
	 * @param path
	 * @param delimiter
	 * @param maxToRead
	 * @return {@link java.util.List<String>} of text from the dataset
	 * @throws RemoteException
	 */
	List<String> select(String path, String delimiter, int maxToRead)
			throws RemoteException;

	/**
	 * Select from the current path the n first elements with a delimiter.
	 * 
	 * @param delimiter
	 * @param maxToRead
	 * @return {@link java.util.List<String>} of text from the dataset
	 * @throws RemoteException
	 */
	List<String> select(String delimiter, int maxToRead) throws RemoteException;

	/**
	 * Get the properties of the path element
	 * 
	 * @param path
	 *            the path in which the properties are extracted
	 * @return {@link java.util.Map<String, String>} of properties
	 * @throws RemoteException
	 */
	Map<String, String> getProperties(String path) throws RemoteException;

	/**
	 * Get the properties of the current element.
	 * 
	 * @return {@link java.util.Map<String, String>} of properties
	 * @throws RemoteException
	 */
	Map<String, String> getProperties() throws RemoteException;

	/**
	 * Get the children properties of the current element.
	 * 
	 * @return {@link java.util.Map<String, Map<String, String>>} of properties, or null if the object cannot have children.
	 * @throws RemoteException
	 */
	Map<String, Map<String, String>> getChildrenProperties()
			throws RemoteException;

	/**
	 * Change one property of the current element
	 * 
	 * @param key
	 *            the property to change
	 * @param newValue
	 *            the new value
	 * @return null if everything is OK, or an error message
	 * @throws RemoteException
	 */
	String changeProperty(String key, String newValue) throws RemoteException;

	/**
	 * Change one property of the element in path
	 * 
	 * @param path
	 *            the element
	 * @param key
	 *            the property to change
	 * @param newValue
	 *            the new value
	 * @return null if everything is OK, or an error message
	 * @throws RemoteException
	 */
	String changeProperty(String path, String key, String newValue)
			throws RemoteException;

	/**
	 * Change the given properties of the path element
	 * 
	 * @param path
	 * @param newProperties
	 * @return newValue
	 *            the new value
	 * @throws RemoteException
	 */
	String changeProperties(String path, Map<String, String> newProperties)
			throws RemoteException;

	/**
	 * Change the given properties of the current element
	 * 
	 * @param newProperties
	 * @return newValue
	 *            the new value
	 * @throws RemoteException
	 */
	String changeProperties(Map<String, String> newProperties)
			throws RemoteException;

	/**
	 * Check if the DataStore supports create a new element
	 * 
	 * @return null if it doesn`t support or a String with the help
	 * @throws RemoteException
	 */
	String canCreate() throws RemoteException;

	/**
	 * Check if the DataStore supports delete an element
	 * 
	 * @return null if it doesn`t support or a String with the help
	 * @throws RemoteException
	 */
	String canDelete() throws RemoteException;

	/**
	 * Check if the DataStore supports move an element
	 * 
	 * @return null if it doesn`t support or a String with the help
	 * @throws RemoteException
	 */
	String canMove() throws RemoteException;

	/**
	 * Check if the DataStore supports copy of a element
	 * 
	 * @return null if it doesn`t support or a String with the help
	 * @throws RemoteException
	 */
	String canCopy() throws RemoteException;

}