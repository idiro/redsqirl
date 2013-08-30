package idiro.utils;

import idiro.workflow.server.enumeration.FeatureType;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface FeatureList extends Remote {
	
	public boolean containsFeature(String name) throws RemoteException;
	
	public FeatureType getFeatureType(String name) throws RemoteException;
	
	public void addFeature(String name, FeatureType type) throws RemoteException;
	
	public List<String> getFeaturesNames() throws RemoteException;
	
	public int getSize() throws RemoteException;
	
	
}
