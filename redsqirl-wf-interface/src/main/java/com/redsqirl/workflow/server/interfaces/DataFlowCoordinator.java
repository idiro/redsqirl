package com.redsqirl.workflow.server.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Object that stores:
 * - a set of DataFlowElement
 * - a set of variables used by those elements
 * - time constraint related to repetitive jobs
 * @author etienne
 *
 */
public interface DataFlowCoordinator extends Remote{

	/**
	 * Object returned by getDefaultTimeConstraint
	 * @author etienne
	 *
	 */
	public interface DefaultConstraint{
		CoordinatorTimeConstraint getConstraint();
		
		void setConstraint(CoordinatorTimeConstraint constraint);
		
		int getOffset();
		
		void setOffset(int offset);
	}

	/**
	 * All the coordinator elements
	 * @return
	 * @throws RemoteException
	 */
	List<DataFlowElement> getElements() throws RemoteException;
	
	/**
	 * List of ids of the coordinator elements
	 * @return
	 * @throws RemoteException
	 */
	List<String> getComponentIds() throws RemoteException;
	
	/**
	 * Element with the given id
	 * @param componentId
	 * @return
	 * @throws RemoteException
	 */
	DataFlowElement getElement(String componentId) throws RemoteException;
	
	/**
	 * Add an element to the coordinator
	 * @param dfe
	 * @return
	 * @throws RemoteException
	 */
	String addElement(DataFlowElement dfe) throws RemoteException;
	
	/**
	 * Remove an element to the coordinator
	 * @param dfe
	 * @return
	 * @throws RemoteException
	 */
	String removeElement(DataFlowElement dfe) throws RemoteException;
	
	/**
	 * Get the coordinator name
	 * @return
	 * @throws RemoteException
	 */
	String getName() throws RemoteException;
	
	/**
	 * Set the coordinator name
	 * @param name
	 * @throws RemoteException
	 */
	void setName(String name)  throws RemoteException;
	
	/**
	 * Get teh time condition of the coordinator
	 * @return
	 * @throws RemoteException
	 */
	CoordinatorTimeConstraint getTimeCondition() throws RemoteException;

	/**
	 * Get the coordinator variables
	 * @return
	 * @throws RemoteException
	 */
	DataFlowCoordinatorVariables getVariables() throws RemoteException;

	/**
	 * Merge a coordinator into this
	 * @param coord
	 * @throws RemoteException
	 */
	void merge(DataFlowCoordinator coord) throws RemoteException;

	/**
	 * Create a coordinator with the given elements
	 * @param dfe
	 * @return
	 * @throws RemoteException
	 */
	DataFlowCoordinator split(List<DataFlowElement> dfe) throws RemoteException;
	
	/**
	 * Read only the meta info of a coordinator (name, variables, constraints)
	 * @param doc
	 * @param parent
	 * @throws RemoteException
	 */
	void readInMeta(Document doc, Element parent) throws RemoteException;
	
	/**
	 * Read and Load the meta data as well as the list of the elements
	 * @param doc
	 * @param parent
	 * @param wf
	 * @return
	 * @throws RemoteException
	 * @throws Exception
	 */
	String readInXml(Document doc, Element parent, DataFlow wf) throws RemoteException, Exception;
	
	/**
	 * Save into an xml document all the data coordinator (element list, links, meta-data...)
	 * @param doc
	 * @param rootElement
	 * @return
	 * @throws RemoteException
	 */
	String saveInXml(Document doc, Element rootElement) throws RemoteException;
	
	/**
	 * Read and load the links between the elements
	 * @param doc
	 * @param parent
	 * @param df
	 * @param pathInUse
	 * @param runs
	 * @return
	 * @throws Exception
	 */
	String readInXmlLinks(Document doc, Element parent, DataFlow df, List<String> pathInUse,boolean runs) throws Exception;
	
	/**
	 * Get an example of execution time
	 * @return
	 * @throws RemoteException
	 */
	Date getExecutionTime() throws RemoteException;

	/**
	 * Set the execution time
	 * @param executionTime
	 * @throws RemoteException
	 */
	void setExecutionTime(Date executionTime) throws RemoteException;

	/**
	 * Get the default time constraint. 
	 * 
	 * This method is used if none are set by the user.
	 * @param df
	 * @return
	 * @throws RemoteException
	 */
	DefaultConstraint getDefaultTimeConstraint(DataFlow df) throws RemoteException;
}
