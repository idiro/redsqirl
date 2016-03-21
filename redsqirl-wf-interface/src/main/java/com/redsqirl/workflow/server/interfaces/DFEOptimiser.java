package com.redsqirl.workflow.server.interfaces;

import java.util.List;

public interface DFEOptimiser extends RunnableElement {

	/**
	 * Reset the list of element to optimise
	 */
	public void resetElementList();
	
	/**
	 * True if the element can be added and is added false otherwise
	 * @param dfe
	 * @return
	 */
	public boolean addElement(DataFlowElement dfe);
	
	/**
	 * True if all the element can be added and are added false otherwise
	 * @param dfe
	 * @return
	 */
	public boolean addAllElement(List<DataFlowElement> dfe);
	
	public List<DataFlowElement> getElements();
	
}
