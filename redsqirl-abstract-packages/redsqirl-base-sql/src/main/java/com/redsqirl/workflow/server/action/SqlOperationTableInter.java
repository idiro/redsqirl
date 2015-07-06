package com.redsqirl.workflow.server.action;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import com.redsqirl.workflow.server.TableInteraction;
import com.redsqirl.workflow.server.action.utils.SqlDictionary;
import com.redsqirl.workflow.server.enumeration.FieldType;

public abstract class SqlOperationTableInter  extends TableInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 169938426599555676L;

	public SqlOperationTableInter(String id, String name, String legend,
			int column, int placeInColumn) throws RemoteException {
		super(id, name, legend, column, placeInColumn);
	}

	public SqlOperationTableInter(String id, String name, String legend,
			String texttip, int column, int placeInColumn)
			throws RemoteException {
		super(id, name, legend, texttip, column, placeInColumn);
	}

	protected abstract SqlDictionary getDictionary();
}
