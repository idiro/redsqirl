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


import java.io.OutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.redsqirl.dynamictable.FileSystemHistory;
import com.redsqirl.dynamictable.UnselectableTable;
import com.redsqirl.useful.MessageUseful;
import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;

/**
 * Output tab object associated with a CanvasModal object. The output tab object
 * control and configure the data associated with a DataFlowElement
 * 
 * @author etienne
 * 
 */
public class CanvasModalOutputTab extends BaseBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7470581158384360769L;

	private static Logger logger = Logger.getLogger(CanvasModalOutputTab.class);

	/**
	 * The DataFlowElement
	 */
	private DataFlowElement dfe;

	/**
	 * The list of output to control
	 */
	private List<OutputForm> outputFormList;

	/**
	 * Flag that disable the view or not ('Y' or 'N')
	 */
	private String showOutputForm;

	/**
	 * The new path kept when there is no outputFrom (showOutputForm = 'N') used
	 * in browser interaction.
	 */
	private String path;

	/**
	 * Name of the current output
	 */
	private String nameOutput;

	/**
	 * Type of the browser of the current output
	 */
	private String typeBrowser;

	private UnselectableTable grid;

	/**
	 * List of the FileSystem available for configuring an output.
	 */
	private Map<String, FileSystemBean> datastores;

	/**
	 * True if it is a source node and hence there is no output tab
	 */
	private boolean sourceNode;

	private LinkedList<FileSystemHistory> pathHistory;
	private LinkedList<String> paths;

	private String maxRows;
	private List<SelectItem> maxNumberRows;
	private List<String> maxNumberRowsString;

	public String showGridDataOutput = "N";
	
	/**
	 * Constructor. The constructor will automatically load the first name as
	 * current name used.
	 * 
	 * @param dfe
	 * @throws RemoteException
	 */
	public CanvasModalOutputTab(Map<String, FileSystemBean> datastores,	DataFlowElement dfe) throws RemoteException {
		this.dfe = dfe;
		this.datastores = datastores;

		try {
			resetNameOutput();
			updateDFEOutputTable();
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
		}

	}
	
	public CanvasModalOutputTab(Map<String, FileSystemBean> datastores) throws RemoteException {
		this.datastores = datastores;
	}

	/**
	 * Set the name of the output to be the first one on the list
	 */
	public void resetNameOutput() {
		logger.info("resetNameOutput");
		try {
			setNameOutput(dfe.getDFEOutput().keySet().iterator().next());
			//logger.info("new name output: " + getNameOutput());
			//logger.info("type browser: " + typeBrowser);
			//logger.info("filesystem existence: " + getFileSystem() != null);
			//logger.info("filesystem names: " + datastores.keySet());
		} catch (Exception e) {
			logger.info("Error when reseting name output: " + e.getMessage());
		}
	}

	/**
	 * Generate the list of outputForm and check if the output form is ready to
	 * be displayed.
	 * 
	 * @param setShowOutputForm
	 *            Don't change the status of the output form display.
	 * @throws RemoteException
	 */
	public void mountOutputForm(boolean setShowOutputForm)
			throws RemoteException {

		logger.info("mountOutputForm");
		if (dfe != null && dfe.getOozieAction() != null
				&& dfe.getDFEOutput() != null && !dfe.getDFEOutput().isEmpty()) {

			outputFormList = new LinkedList<OutputForm>();

			for (Entry<String, DFEOutput> e : dfe.getDFEOutput().entrySet()) {
				OutputForm of = new OutputForm(datastores, e.getValue(),
						dfe.getComponentId(), e.getKey());

				List<SelectItem> outputList = new ArrayList<SelectItem>();
				for (SavingState s : SavingState.values()) {
					outputList.add(new SelectItem(s.toString(), s.toString()));
				}
				of.setSavingStateList(outputList);
				
				if(outputList != null && !outputList.isEmpty()){
					of.getSavingStateListString().add(calcString(outputList));
					of.setSavingStateListString(of.getSavingStateListString());
				}
				
				logger.info("saving state "
						+ e.getValue().getSavingState().toString());
				if (e.getValue().getSavingState() == SavingState.RECORDED &&
						e.getValue().getPath() != null) {
					int lastSlash = e.getValue().getPath().lastIndexOf('/');
					if (lastSlash != -1) {
						if (lastSlash == 0) {
							of.setPath("/");
						} else {
							of.setPath(e.getValue().getPath()
									.substring(0, lastSlash));
						}
						of.setFile(e.getValue().getPath()
								.substring(lastSlash + 1));
					}
				}

				outputFormList.add(of);
			}
		} else {
			outputFormList = new LinkedList<OutputForm>();
		}

		if (setShowOutputForm) {
			if (outputFormList.isEmpty()) {
				setShowOutputForm("N");
			} else {
				setShowOutputForm("Y");
			}
		} else {			
			setShowOutputForm("N");
		}

	}

	/**
	 * Change the path of an output
	 * 
	 * @throws RemoteException
	 */
	public void changePathOutputBrowser() throws RemoteException {
		logger.info("changePathOutputBrowser");
		path = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("pathFile");

		logger.info("Output: " + getNameOutput() + " - path: " + path);
		
		if(showOutputForm != null && showOutputForm.equals("Y")) {
			for (OutputForm f : getOutputFormList()) {
				if (f.getName().equals(getNameOutput())) {
					f.setPath(path);
					logger.info("Output found: " + getNameOutput() + " - path: " + path);
				}
			}
		}else{
			if(outputFormList != null && !outputFormList.isEmpty()){
				for (OutputForm f : getOutputFormList()) {
					if (f.getName().equals(getNameOutput())) {
						DFEOutput output = f.getDfeOutput();  
						if(output.isPathValid(path) != null){
							String parent = path.substring(0,path.lastIndexOf('/')); 
							if(output.isPathValid(parent) == null){
								path = parent;
								getFileSystem().setPath(path);
								//Display warning

								HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
								MessageUseful.addWarnMessage(getMessageResources("warn_hdfs_path"));
								request.setAttribute("msnSuccess", "msnSuccess");

							}
						}
					}
				}
			}
		}

		setSourceNode(true);
		mountPathHistory(path);
	}

	public void mountPathHistory(String path) throws RemoteException {

		String pathFolder = path.substring(0,path.lastIndexOf("/"));

		if(pathFolder.isEmpty()){
			pathFolder = path;
		}
		
		if(paths.size() > 10){
			paths.removeLast();
		}

		if(!paths.contains(pathFolder)){
			paths.addFirst(pathFolder);
		}else{
			paths.remove(pathFolder);
			paths.addFirst(pathFolder);
		}

		FileSystemHistory fsh = new FileSystemHistory();
		String alias = "";
		if(pathFolder.endsWith("/")){
			alias = pathFolder.substring(0, pathFolder.length()-1);
		}
		alias = pathFolder.substring(pathFolder.lastIndexOf("/"));
		
		if(alias.isEmpty()){
			alias = pathFolder;
		}
		
		fsh.setName(pathFolder);
		if(alias.startsWith("/")){
			alias = alias.substring(1);
		}
		fsh.setAlias(alias);

		if(pathHistory.size() > 10){
			pathHistory.removeLast();
		}

		if(!checkExist(pathHistory, pathFolder)){
			pathHistory.addFirst(fsh);
		}else{
			removePath(pathHistory, pathFolder);
			pathHistory.addFirst(fsh);
		}

		getFileSystem().getDataStore().savePathList("paths", paths);
	}

	public void createPathHistory() throws RemoteException{

		if(pathHistory == null){
			pathHistory = new LinkedList<FileSystemHistory>();
			paths = new LinkedList<String>();
			Map<String, String> mapHistory = getFileSystem().getDataStore().readPathList("paths");
			for (String path : mapHistory.keySet()) {
				FileSystemHistory fsh = new FileSystemHistory();
				fsh.setName(path);
				String alias = mapHistory.get(path);
				if(alias.startsWith("/")){
					alias = alias.substring(1);
				}
				fsh.setAlias(alias);
				pathHistory.addLast(fsh);
				paths.addLast(path);
			}
		}

	}

	public boolean checkExist(LinkedList<FileSystemHistory> list, String path) throws RemoteException {
		for (FileSystemHistory fileSystemHistory : list) {
			if(fileSystemHistory.getName().equalsIgnoreCase(path)){
				return true;
			}
		}
		return false;
	}

	public void removePath(LinkedList<FileSystemHistory> list, String path) throws RemoteException {
		for (Iterator<FileSystemHistory> iterator = list.iterator(); iterator.hasNext();) {
			FileSystemHistory fileSystemHistory = (FileSystemHistory) iterator.next();
			if(fileSystemHistory.getName().equalsIgnoreCase(path)){
				iterator.remove();
			}
		}
	}

	/**
	 * Make sure that everything is OK with the new configuration
	 * 
	 * @throws RemoteException
	 */
	public void confirmOutput() throws RemoteException {
		logger.info("confirmOutput");

		String error = null;
		for (OutputForm f : getOutputFormList()) {

			logger.info("confirmOutput path " + f.getPath());
			if(f.getPath() == null){
				error = "Path cannot be null";
			}else{
				String fileName = f.getFile();
				if(fileName == null ){
					try{
						fileName = f.getPath().substring(f.getPath().lastIndexOf('/')+1);
					}catch(Exception e){
						fileName = "";
					}
				}

				logger.info(fileName);

				String regex = "[1-9a-zA-Z]([^/]*)";
				if (!fileName.matches(regex)) {
					error = getMessageResources("msg_error_save");
				}
			}

			if(error == null){
				error = f.updateDFEOutput();
			}

			if (error != null) {
				logger.error(error);
				MessageUseful.addErrorMessage(error);
				HttpServletRequest request = (HttpServletRequest) FacesContext
						.getCurrentInstance().getExternalContext().getRequest();
				request.setAttribute("msnError", "msnError");
			}

		}

	}

	/**
	 * Display the first rows of data available from the given output. An
	 * outputName parameter is necessary for running this function.
	 * 
	 * @throws RemoteException
	 */
	public void displayOutput() throws RemoteException {
		String outputN = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("outputName");
		String resetMaxRows = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("resetMaxRows");
		String selectedComboBox = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selectedComboBox");

		if(resetMaxRows != null && resetMaxRows.equals("Y")){
			setMaxRows(null);
		}
		
		if(getNameOutput() != null && selectedComboBox != null){
			outputN = getNameOutput();
		}

		if (outputN != null && !outputN.equalsIgnoreCase("undefined") && !outputN.isEmpty()) {
			if(!getNameOutput().equals(outputN)){
				setNameOutput(outputN);
				logger.info("display out: " + nameOutput);
				updateDFEOutputTable();
			}
		}else {
			if(outputFormList != null && !outputFormList.isEmpty()){
				outputN = outputFormList.get(0).getName();
				if(getNameOutput() != null && outputN != null && !getNameOutput().equals(outputN)){
					setNameOutput(outputN);
					logger.info("display out: " + nameOutput);
					updateDFEOutputTable();
				}else if(selectedComboBox != null){
					setNameOutput(outputN);
					logger.info("display out: " + nameOutput);
					updateDFEOutputTable();
				}
			}else{
				logger.info("display out null ");
			}
		}
	}

	/**
	 * Update the rows of data from the current output. The rows of data are
	 * given in the gridTitle and outputGrid fields.
	 * 
	 * @throws RemoteException
	 */
	public void updateDFEOutputTable() throws RemoteException {
		logger.info("updateDFEOutputTable");
		if (dfe.getDFEOutput() == null) {
			logger.info("No output map");
			setShowGridDataOutput("N");
		} else {
			DFEOutput dfeOut = dfe.getDFEOutput().get(nameOutput);
			if (dfeOut == null) {
				logger.info("no output named: " + nameOutput);
				setShowGridDataOutput("N");
			} else {
				LinkedList<String> gridTitle = new LinkedList<String>();
				List<SelectItem> listExtensions = new LinkedList<SelectItem>();
				if (dfeOut.getExtensions() != null && dfeOut.getExtensions().length != 0) {
					String[] listExt = dfeOut.getExtensions();
					if(getFileSystem().getOpenOutputData() != null && getFileSystem().getOpenOutputData().equals("Y")){
						listExtensions.add(new SelectItem("(?!.\\.).", "(?!.\\.).")); 
					}
					for (int i = 0; i < listExt.length; i++) {
						String value = listExt[i];
						if(getFileSystem().getOpenOutputData() != null && getFileSystem().getOpenOutputData().equals("Y")){
							listExtensions.add(new SelectItem("(?!."+value+"$).", "(?!."+value+"$)."));
						}else{
							listExtensions.add(new SelectItem(value, value));
						}
					}
					listExtensions.add(new SelectItem("*", "*"));

					getFileSystem().setExtensionsSelected(listExtensions.get(0).getLabel());
				}
				getFileSystem().setListExtensions(listExtensions);
				getFileSystem().setAllowDirectories(dfeOut.allowDirectories());
				getFileSystem().updateTable(false);
				
				if(listExtensions != null && !listExtensions.isEmpty()){
					List<String> listExtensionsString = new LinkedList<String>();
					listExtensionsString.add(calcString(listExtensions));
					getFileSystem().setListExtensionsString(listExtensionsString);
				}

				if (dfeOut.getFields() != null) {

					try {
						List<String> outputFieldList = dfeOut.getFields().getFieldNames();
						for (String outputField : outputFieldList) {

							// logger.info("outputFieldureNames " + outputFieldure);
							FieldType fieldType = dfeOut.getFields().getFieldType(outputField);
							// logger.info("FieldureType " + FieldureType);

							gridTitle.add(outputField + " "	+ fieldType.toString());
						}
						logger.info("grid titles: " + gridTitle);
					} catch (Exception e) {
						logger.info("Error when getting the field: " + e.getMessage());
					}
					grid = new UnselectableTable(gridTitle);
					String error = dfeOut.isPathValid();
					if( error != null){
						logger.info("Path invalid: "+error);
					}else{
						try {

							int mRow = Math.max(10, Math.min(150, 1000/(gridTitle.size()+1) ));
							mountNumberRowsList(mRow);
							if(getMaxRows() != null && !getMaxRows().isEmpty()){
								mRow = Integer.parseInt(getMaxRows()); 
							}
							setMaxRows(mRow+"");

							List<Map<String, String>> outputLines = dfeOut.select(mRow);
							FieldList fl = dfeOut.getFields();
							if (outputLines == null) {
								logger.info("No dataset to display.");
							}else{
								logger.info(outputLines);
								for (Map<String, String> line : outputLines) {
									int i = 0;
									Comparable[] rowCur = new Comparable[fl.getSize()];
									Iterator<String> fieldIt = fl.getFieldNames().iterator();
									while(fieldIt.hasNext()){
										String field = fieldIt.next();
										FieldType type = fl.getFieldType(field);
										String curData = line.get(field);
										switch(type){
										case FLOAT:
											try{
												rowCur[i] = Float.parseFloat(curData);
											}catch(Exception e){
												rowCur[i] = Float.NaN;
											}
											break;
										case INT:
											try{
												rowCur[i] = Integer.parseInt(curData);
											}catch(Exception e){
												rowCur[i] = null;
											}
											break;
										case DOUBLE:
											try{
												rowCur[i] = Double.parseDouble(curData);
											}catch(Exception e){
												rowCur[i] = Double.NaN;
											}
											break;
										case LONG:
											try{
												rowCur[i] = Long.parseLong(curData);
											}catch(Exception e){
												rowCur[i] = null;
											}
											break;
										case BOOLEAN:
										case CATEGORY:
										case CHAR:
										case DATE:
										case DATETIME:
										case TIMESTAMP:
										case STRING:
										default:
											if(curData == null){
												rowCur[i] = "";
											}else{
												rowCur[i] = curData;
											}
											break;

										}
										++i;
									}
									grid.add(rowCur);
								}
								logger.info(grid.getRows());
							}

							if(grid.getRows().isEmpty()){
								String[] emptyRow = new String[gridTitle.size()];
								grid.add(emptyRow);
								setShowGridDataOutput("N");
							}else{
								setShowGridDataOutput("Y");
							}


						} catch (Exception e) {
							logger.info("Error when getting data: " + e,e);
						}

					}
				}
			}
		}
	}
	
	public String calcString(List<SelectItem> listFields){
		StringBuffer ans = new StringBuffer();
		for (SelectItem selectItem : listFields) {
			ans.append(",'"+selectItem.getLabel()+"'");
		}
		return ans.toString().substring(1);
	}

	public void mountNumberRowsList(int mRow) throws RemoteException {
		maxNumberRows = new ArrayList<SelectItem>();
		for (int i = 1; i <= 10; i++) {
			int value = mRow * i;
			maxNumberRows.add(new SelectItem(value+"", value+""));
		}
		
		if(maxNumberRows != null && !maxNumberRows.isEmpty()){
			maxNumberRowsString = new ArrayList<String>();
			maxNumberRowsString.add(calcString(maxNumberRows));
		}
		
	}

	/**
	 * Remove the data associated with a given output name. The output name is
	 * given through the context "nameOutputClean"
	 * 
	 * @throws RemoteException
	 */
	public void removeData() throws RemoteException {

		String nameOutputClean = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap()
				.get("nameOutputClean");
		String error = null;
		for (OutputForm outputForm : getOutputFormList()) {

			if (outputForm.getName().equalsIgnoreCase(nameOutputClean)) {
				error = outputForm.getDfeOutput().remove();
			}

		}

		if (error != null) {
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}

	}
	

	public String downloadFile() {

	    try {

	    	if(nameOutput == null){
	    		return "";
	    	}
	    	DFEOutput dfeOut = dfe.getDFEOutput().get(nameOutput);
	    	if(grid == null || getRows() == null || getRows().isEmpty() || dfeOut == null || !dfeOut.isPathExist()){
	    		return "";
	    	}

	    	String filename = dfe.getComponentId()+"_"+nameOutput+".csv";
	    	if(nameOutput.isEmpty()){
	    		filename = dfe.getComponentId()+".csv";
	    	}
	        FacesContext fc = FacesContext.getCurrentInstance();
	        HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();

	        response.reset();
	        response.setContentType("text/comma-separated-values");
	        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

	        OutputStream output = response.getOutputStream();

	        Iterator<String> it = grid.getColumnIds().iterator();
	        while(it.hasNext()){
	        	output.write(it.next().split(" ")[0].getBytes());
	        	if(it.hasNext()){
	        		output.write(",".getBytes());
	        	}
	        }
	        output.write("\n".getBytes());

	        for (Comparable[] objS : getRows()) {
	        	for(int i = 0; i < objS.length;++i){
	        		if(i > 0){
	        			output.write(",".getBytes());
	        		}
	        		if(objS[i] == null){
	        		}else if(objS[i] instanceof String){
	        			output.write(("\""+objS[i]+"\"").getBytes());
	        		}else{
	        			output.write(objS[i].toString().getBytes());
	        		}
	        	}
	        	output.write("\n".getBytes());
	        }

	        output.flush();
	        output.close();

	        fc.responseComplete();

	    } catch (Exception e) {
	        logger.error(e,e);
	    }
	    return "";
	}

	/**
	 * Get the file system associated with the current nameOutput.
	 * 
	 * @return
	 */
	public final FileSystemBean getFileSystem() {
		return datastores.get(typeBrowser);
	}

	/**
	 * @return the outputFormList
	 */
	public final List<OutputForm> getOutputFormList() {
		return outputFormList;
	}

	/**
	 * @param outputFormList
	 *            the outputFormList to set
	 */
	public final void setOutputFormList(List<OutputForm> outputFormList) {
		this.outputFormList = outputFormList;
	}

	/**
	 * @return the showOutputForm
	 */
	public final String getShowOutputForm() {
		return showOutputForm;
	}

	/**
	 * @param showOutputForm
	 *            the showOutputForm to set
	 */
	public final void setShowOutputForm(String showOutputForm) {
		this.showOutputForm = showOutputForm;
	}

	/**
	 * @return the nameOutput
	 */
	public String getNameOutput() {
		return nameOutput;
	}

	/**
	 * @param nameOutput
	 *            the nameOutput to set
	 */
	public void setNameOutput(String nameOutput) {
		this.nameOutput = nameOutput;
		try {
			typeBrowser = dfe.getDFEOutput().get(nameOutput).getBrowserName();

			createPathHistory();

		} catch (Exception e) {
			logger.warn(nameOutput + " not recognized by object");
		}
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the grid
	 */
	public UnselectableTable getGrid() {
		return grid;
	}

	/**
	 * @return
	 * @see com.redsqirl.dynamictable.UnselectableTable#getTitles()
	 */
	public List<String> getTitles() {
		return grid == null ? null : grid.getTitles();
	}

	/**
	 * @return
	 * @see com.redsqirl.dynamictable.UnselectableTable#getRows()
	 */
	public List<Comparable[]> getRows() {
		return grid == null ? null : grid.getRows();
	}
	
	public void sort(){
		if(grid != null){
			grid.sort();
		}
	}

	public boolean isSourceNode() {
		return sourceNode;
	}

	public void setSourceNode(boolean sourceNode) {
		this.sourceNode = sourceNode;
	}

	public LinkedList<FileSystemHistory> getPathHistory() {
		return pathHistory;
	}

	public void setPathHistory(LinkedList<FileSystemHistory> pathHistory) {
		this.pathHistory = pathHistory;
	}

	public LinkedList<String> getPaths() {
		return paths;
	}

	public void setPaths(LinkedList<String> paths) {
		this.paths = paths;
	}

	public String getMaxRows() {
		return maxRows != null ? maxRows : null;
	}

	public void setMaxRows(String maxRows) {
		this.maxRows = maxRows;
	}

	public List<SelectItem> getMaxNumberRows() {
		return maxNumberRows;
	}

	public void setMaxNumberRows(List<SelectItem> maxNumberRows) {
		this.maxNumberRows = maxNumberRows;
	}

	public List<String> getMaxNumberRowsString() {
		return maxNumberRowsString;
	}

	public void setMaxNumberRowsString(List<String> maxNumberRowsString) {
		this.maxNumberRowsString = maxNumberRowsString;
	}

	public String getShowGridDataOutput() {
		return showGridDataOutput;
	}

	public void setShowGridDataOutput(String showGridDataOutput) {
		this.showGridDataOutput = showGridDataOutput;
	}

}