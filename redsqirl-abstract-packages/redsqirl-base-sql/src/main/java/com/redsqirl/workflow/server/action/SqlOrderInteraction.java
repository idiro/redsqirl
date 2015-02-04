package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.AppendListInteraction;

public abstract class SqlOrderInteraction extends AppendListInteraction{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7913845575238427401L;
	/**
	 * Action that the query belongs to
	 */
	protected SqlElement el;

	/**
	 * Constructor
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @throws RemoteException
	 */
	public SqlOrderInteraction(String id, String name, String legend,
			int column, int placeInColumn, SqlElement el) throws RemoteException {
		super(id, name, legend, column, placeInColumn, true);
		this.el = el;
	}
	
	/**
	 * Update the interaction 
	 * @throws RemoteException
	 */
	public void update() throws RemoteException{
		logger.info("update start ");
		FieldList fields = el.getNewFields();
		logger.info(fields.toString());
		setPossibleValues(fields.getFieldNames());
		logger.info(fields.getFieldNames().toString());
		logger.info("update end ");
	}
}
