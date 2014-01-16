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

public class OrderedFeatureList extends UnicastRemoteObject implements FeatureList {
	
	private Map<String, FeatureType> features;
	private List<String> positions;
	
	private static Logger logger = Logger.getLogger(OrderedFeatureList.class);
	
	public OrderedFeatureList() throws RemoteException {
		super();
		features = new HashMap<String, FeatureType>();
		positions = new ArrayList<String>();
	}
	
	public boolean containsFeature(String name){
		return features.containsKey(name);
	}
	
	public FeatureType getFeatureType(String name){
		return features.get(name);
	}
	
	public void addFeature(String name, FeatureType type){
		
		logger.info("addFeature name " + name + " " + type.toString());
		
		if (!features.containsKey(name)){
			logger.info("addFeature name " + name);
			positions.add(name);
		}else{
			logger.info("addFeature name no " + name);
		}
		
		features.put(name, type);
	}
	
	public List<String> getFeaturesNames(){
		List<String> tmp = new LinkedList<String>();
		tmp.addAll(positions);
		return tmp;
	}
	
	public int getSize(){
		return positions.size();
	}
	
	
}
