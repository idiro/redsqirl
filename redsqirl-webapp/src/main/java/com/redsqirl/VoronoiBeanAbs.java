/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

package com.redsqirl;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.redsqirl.dynamictable.VoronoiType;
import com.redsqirl.useful.MessageUseful;
import com.redsqirl.workflow.server.interfaces.DataFlowCoordinatorVariables;

public class VoronoiBeanAbs extends BaseBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(VoronoiBeanAbs.class);
	
	protected List<VoronoiType> tableList;
	protected DataFlowCoordinatorVariables dataFlowCoordinatorVariables;
	protected List<SelectItem> varFunctionsList;
	protected List<String> varFunctionsListString;
	protected String selectedFunction;
	protected String textAreaEditor;
	protected List<String[]> listFunctionsTable;
	protected String selectedRowNumber;
	
	
	public void deleteLine(){
		for (Iterator<VoronoiType> iterator = tableList.iterator(); iterator.hasNext();) {
			VoronoiType voronoiType = (VoronoiType) iterator.next();
			if(voronoiType.isSelected()){
				iterator.remove();
			}
		}
	}

	public void addNewLine(){
		tableList.add(new VoronoiType());
	}
	
	public void openVoronoiEditorModal() throws RemoteException{
		logger.info("openVoronoiEditorModal");

		FacesContext context = FacesContext.getCurrentInstance();
		String rowKey = context.getExternalContext().getRequestParameterMap().get("rowKey");
		if(rowKey != null && !rowKey.isEmpty() && tableList.size() >= Integer.parseInt(rowKey)){
			textAreaEditor = tableList.get(Integer.parseInt(rowKey)).getValue();
			setSelectedRowNumber(rowKey);
		}

		Map<String, String[][]> m = dataFlowCoordinatorVariables.getVarFunctions();
		
		varFunctionsList = new ArrayList<SelectItem>();
		varFunctionsListString = new ArrayList<String>();
		
		for (String value : m.keySet()) {
			SelectItem s = new SelectItem(value,value);
			varFunctionsList.add(s);
			varFunctionsListString.add(value);
		}

		if(varFunctionsList != null && !varFunctionsList.isEmpty()){
			setSelectedFunction(varFunctionsList.get(0).getLabel());
		}
		
		updateTableEditor(selectedFunction);
	}

	public void updateTableEditor() throws RemoteException {
		logger.info("updateTableEditor");
		FacesContext context = FacesContext.getCurrentInstance();
		String nameFunction = context.getExternalContext().getRequestParameterMap().get("nameFunction");
		setSelectedFunction(nameFunction.trim());
		updateTableEditor(nameFunction);
	}
	
	public void updateTableEditor(String selected) throws RemoteException {
		logger.info("updateTableEditor");
		listFunctionsTable = new ArrayList<String[]>();
		Map<String, String[][]> m = dataFlowCoordinatorVariables.getVarFunctions();
		String[][] ans = m.get(selected.trim());
		for (String[] value : ans) {
			listFunctionsTable.add(value);
		}
	}

	public void checkTableEditor() throws RemoteException {
		String error = dataFlowCoordinatorVariables.checkVar(textAreaEditor);
		displayErrorMessage(error, "CHECKTABLEEDITOR");
		if(error == null){
			MessageUseful.addInfoMessage(getMessageResources("success_message"));
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}
	}

	public void applyTableEditor() throws RemoteException {
		String error = dataFlowCoordinatorVariables.checkVar(textAreaEditor);
		if(error == null){
			if(getSelectedRowNumber() != null && !getSelectedRowNumber().isEmpty()){
				tableList.get(Integer.parseInt(getSelectedRowNumber())).setValue(textAreaEditor);
			}
		}else{
			displayErrorMessage(error, "APPLYTABLEEDITOR");
		}
	}
	
	
	public List<VoronoiType> getTableList() {
		return tableList;
	}
	public void setTableList(List<VoronoiType> tableList) {
		this.tableList = tableList;
	}
	public List<SelectItem> getVarFunctionsList() {
		return varFunctionsList;
	}
	public void setVarFunctionsList(List<SelectItem> varFunctionsList) {
		this.varFunctionsList = varFunctionsList;
	}
	public List<String> getVarFunctionsListString() {
		return varFunctionsListString;
	}
	public void setVarFunctionsListString(List<String> varFunctionsListString) {
		this.varFunctionsListString = varFunctionsListString;
	}
	public String getSelectedFunction() {
		return selectedFunction;
	}
	public void setSelectedFunction(String selectedFunction) {
		this.selectedFunction = selectedFunction;
	}
	public String getTextAreaEditor() {
		return textAreaEditor;
	}
	public void setTextAreaEditor(String textAreaEditor) {
		this.textAreaEditor = textAreaEditor;
	}
	public List<String[]> getListFunctionsTable() {
		return listFunctionsTable;
	}
	public void setListFunctionsTable(List<String[]> listFunctionsTable) {
		this.listFunctionsTable = listFunctionsTable;
	}
	public String getSelectedRowNumber() {
		return selectedRowNumber;
	}
	public void setSelectedRowNumber(String selectedRowNumber) {
		this.selectedRowNumber = selectedRowNumber;
	}
	public DataFlowCoordinatorVariables getDataFlowCoordinatorVariables() {
		return dataFlowCoordinatorVariables;
	}
	public void setDataFlowCoordinatorVariables(
			DataFlowCoordinatorVariables dataFlowCoordinatorVariables) {
		this.dataFlowCoordinatorVariables = dataFlowCoordinatorVariables;
	}
	
}