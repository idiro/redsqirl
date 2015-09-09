package com.redsqirl.workflow.server.interfaces;

import java.rmi.RemoteException;

public interface DFELinkOutput extends DFEOutput {

	public String getLink() throws RemoteException;
	
}
