package com.redsqirl.workflow.utils;

import java.util.List;

/**
 * Interface to create Models and do global searches.
 * @author etienne
 *
 */
public interface ModelManagerInt {
	
	/**
	 * Create a new Model
	 * @param user
	 * @param newModelName
	 * @return
	 */
	String create(String user, String newModelName);
	
	/**
	 * Get all the models available to a given user.
	 * @param user
	 * @return
	 */
	List<ModelInt> getModels(String user);
	
	/**
	 * Get a model as a user.
	 * 
	 * If the model is not found in the user scope it searches in system.
	 * @param modelName
	 * @param user
	 * @return
	 */
	ModelInt getModel(String modelName, String user);	
	
	/**
	 * Move the given subdataflow from one model to another 
	 * @param modelFrom
	 * @param modelTo
	 * @param subDataFlowName
	 * @return
	 */
	String move(ModelInt modelFrom, ModelInt modelTo, String subDataFlowName);

	/**
	 * Copy the given subdataflow from one model to another 
	 * @param modelFrom
	 * @param modelTo
	 * @param subDataFlowName
	 * @return
	 */
	String copy(ModelInt modelFrom, ModelInt modelTo, String subDataFlowName);
	
}
