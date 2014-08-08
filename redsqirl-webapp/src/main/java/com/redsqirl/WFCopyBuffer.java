package com.redsqirl;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;

import com.redsqirl.workflow.server.connect.interfaces.DataFlowInterface;

public class WFCopyBuffer implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8524531652226620238L;
	
	private String dfCloneId;
	private List<String> elementsToCopy;
	
	
	/**
	 * @param dfClone
	 * @param elementsToCopy
	 * @throws RemoteException 
	 */
	public WFCopyBuffer(DataFlowInterface dfi, String wfName, List<String> elementsToCopy) throws NullPointerException, RemoteException{
		super();
		this.dfCloneId = dfi.cloneDataFlow(wfName);
		this.elementsToCopy = elementsToCopy;
		if(dfCloneId == null){
			throw new NullPointerException("Object clone null");
		}
	}
	
	/**
	 * @return the dfClone
	 */
	public final String getDfCloneId() {
		return dfCloneId;
	}


	/**
	 * @return the elementToCopy
	 */
	public final List<String> getElementsToCopy() {
		return elementsToCopy;
	}
	
	

}
