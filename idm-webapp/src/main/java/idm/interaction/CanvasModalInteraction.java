package idm.interaction;

import idiro.workflow.server.interfaces.DFEInteraction;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

public abstract class CanvasModalInteraction {
	
	protected DFEInteraction inter;
	protected boolean unchanged;
	
	static protected Logger logger = Logger.getLogger(CanvasModalInteraction.class);
	
	public CanvasModalInteraction(DFEInteraction dfeInter){
		this.inter = dfeInter;
	}
	
	public abstract void readInteraction() throws RemoteException;
	
	public abstract void writeInteraction() throws RemoteException;

	public abstract void setUnchanged();
	
	/**
	 * @return the inter
	 */
	public DFEInteraction getInter() {
		return inter;
	}

	/**
	 * @return the unchanged
	 */
	public boolean isUnchanged() {
		return unchanged;
	}
	
}
