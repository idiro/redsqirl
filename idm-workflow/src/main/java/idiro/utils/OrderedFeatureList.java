package idiro.utils;

import idiro.workflow.server.enumeration.FeatureType;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/** Class that maintains a list of features in order */
public class OrderedFeatureList extends UnicastRemoteObject implements
		FeatureList {
	/**
	 * 
	 */
	private static final long serialVersionUID = 521555689061320896L;

	/**
	 * Features Map
	 */
	private Map<String, FeatureType> features;

	/**
	 * List of Positions
	 * 
	 */
	private List<String> positions;

	/**
	 * Logger for class
	 */
	private static Logger logger = Logger.getLogger(OrderedFeatureList.class);

	/** Default constructor */
	public OrderedFeatureList() throws RemoteException {
		super();
		features = new HashMap<String, FeatureType>();
		positions = new ArrayList<String>();
	}

	/**
	 * Check if a Feature name is contained in a list
	 * 
	 * @param name
	 *            String representing a feature name
	 * @return <code>true</code> if feature is contained in this list
	 * else <code>false</code>
	 */
	public boolean containsFeature(String name) {
		return features.containsKey(name);
	}

	/**
	 * Get Feature Type of a feature
	 * 
	 * @param name
	 *            String of a feature name
	 * @return FeatureType of the feature name given
	 */
	public FeatureType getFeatureType(String name) {
		return features.get(name);
	}
	
	/**
	 * Add a feature to the list
	 * @param name Name of the feature 
	 * @param type FeatureType of the feature
	 */
	public void addFeature(String name, FeatureType type) {

		logger.info("addFeature name " + name + " " + type.toString());

		if (!features.containsKey(name)) {
			logger.info("addFeature name " + name);
			positions.add(name);
		} else {
			logger.info("addFeature name no " + name);
		}

		features.put(name, type);
	}

	/**
	 * Get a list of the feature names in the list
	 * @return List of Strings with feature names
	 */
	public List<String> getFeaturesNames() {
		List<String> tmp = new LinkedList<String>();
		tmp.addAll(positions);
		return tmp;
	}

	/**
	 * Get size of the positions
	 * @return Size of the positions
	 */
	public int getSize() {
		return positions.size();
	}

	@Override
	/**
	 * Check to see if this List is equal to another list
	 * @param o Object representing another possible list
	 * @return <code>true</code> or <code>false</false> if the lists are the same
	 */
	public boolean equals(Object o) {
		boolean ok = false;
		if (o instanceof OrderedFeatureList) {
			OrderedFeatureList comp = (OrderedFeatureList) o;
			ok = comp.positions.equals(this.positions)
					&& comp.features.equals(this.features);
		}
		return ok;
	}

}
