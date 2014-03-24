package idiro.workflow.server;

import idiro.workflow.server.interfaces.DFELinkProperty;
import idiro.workflow.server.interfaces.DFEOutput;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

public class DataProperty extends UnicastRemoteObject implements
		DFELinkProperty {

	/**
	 * 
	 */
	private static final long serialVersionUID = 736683643913912897L;

	private static Logger logger = Logger.getLogger(DataProperty.class);

	public static final int MAX_ALLOWED = 100;
	protected List<Class<? extends DFEOutput>> typeAccepted;
	protected int minOccurence;
	protected int maxOccurence;

	/**
	 * Constructor with one accepted type and the min and max occurrence values
	 * 
	 * @param typeAccepted
	 * @param minOccurence
	 * @param maxOccurence
	 * @throws RemoteException
	 */
	public DataProperty(Class<? extends DFEOutput> typeAccepted,
			int minOccurence, int maxOccurence) throws RemoteException {
		super();
		this.typeAccepted = new LinkedList<Class<? extends DFEOutput>>();
		this.typeAccepted.add(typeAccepted);
		init(minOccurence, maxOccurence);
	}

	/**
	 * Constructor with a list of accepted types and the min and max occurrence
	 * values
	 * 
	 * @param typeAccepted
	 * @param minOccurence
	 * @param maxOccurence
	 * @throws RemoteException
	 */
	public DataProperty(List<Class<? extends DFEOutput>> typeAccepted,
			int minOccurence, int maxOccurence) throws RemoteException {
		super();
		this.typeAccepted = typeAccepted;
		init(minOccurence, maxOccurence);
	}

	/**
	 * Initialize the Data property with min and max occurence values
	 * 
	 * @param minOccurence
	 * @param maxOccurence
	 */
	private void init(int minOccurence, int maxOccurence) {
		if (minOccurence > maxOccurence) {
			int swap = minOccurence;
			minOccurence = maxOccurence;
			maxOccurence = swap;
		}

		if (minOccurence < 0) {
			logger.warn("minimum Occurence cannot be < 0");
			this.minOccurence = 0;
		} else {
			this.minOccurence = Math.min(minOccurence, MAX_ALLOWED);
		}

		if (maxOccurence < 1) {
			logger.warn("maximum Occurence cannot be < 1");
			this.maxOccurence = 1;
		} else {
			this.maxOccurence = Math.min(maxOccurence, MAX_ALLOWED);
		}

	}

	/**
	 * 
	 */
	public boolean check(DFEOutput out) {
		boolean ok = false;
		Iterator<Class<? extends DFEOutput>> it = typeAccepted.iterator();
		while (it.hasNext() && !ok) {
			Class<?> cur = it.next();
			while (cur != null && !ok) {
				logger.debug("Check if " + cur + " equals " + out.getClass());
				if (cur.equals(out.getClass())) {
					ok = true;
				}
				cur = cur.getSuperclass();
			}
		}
		return ok;
	}

	/**
	 * Get the minimum allowed inputs
	 * 
	 * @return minimum inputs
	 */
	public int getMinOccurence() {
		return minOccurence;
	}

	/**
	 * Get the Max inputs for the
	 * 
	 * @return max inputs
	 */
	public int getMaxOccurence() {
		return maxOccurence;
	}

	/**
	 * Get the List of Type of classes accepted
	 * 
	 * @return typeAccepted List
	 */
	public List<Class<? extends DFEOutput>> getTypeAccepted() {
		return typeAccepted;
	}
}
