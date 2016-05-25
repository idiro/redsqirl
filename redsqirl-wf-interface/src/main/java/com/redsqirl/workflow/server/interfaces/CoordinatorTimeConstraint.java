package com.redsqirl.workflow.server.interfaces;

import java.rmi.Remote;

public interface CoordinatorTimeConstraint extends Remote{

	int getFrequency();
	
	String getFrequencyStr();
	
	String getUnit();
}
