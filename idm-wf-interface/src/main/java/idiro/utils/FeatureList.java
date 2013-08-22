package idiro.utils;

import idiro.workflow.server.enumeration.FeatureType;

import java.util.List;

public interface FeatureList {
	
	public boolean containsFeature(String name);
	
	public FeatureType getFeatureType(String name);
	
	public void addFeature(String name, FeatureType type);
	
	public List<String> getFeaturesNames();
	
	public int getSize();
	
	
}
