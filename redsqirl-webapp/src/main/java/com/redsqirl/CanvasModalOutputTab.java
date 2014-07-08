package com.redsqirl;


import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.redsqirl.dynamictable.SelectableRow;
import com.redsqirl.dynamictable.UnselectableTable;
import com.redsqirl.useful.MessageUseful;
import com.redsqirl.workflow.server.enumeration.FeatureType;
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
public class CanvasModalOutputTab implements Serializable {

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
	
	
	/**
	 * Constructor. The constructor will automatically load the first name as
	 * current name used.
	 * 
	 * @param dfe
	 * @throws RemoteException
	 */
	public CanvasModalOutputTab(Map<String, FileSystemBean> datastores,
			DataFlowElement dfe) throws RemoteException {
		this.dfe = dfe;
		this.datastores = datastores;
		try {
			resetNameOutput();
			updateDFEOutputTable();
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
		}
	}

	/**
	 * Set the name of the output to be the first one on the list
	 */
	public void resetNameOutput() {
		logger.info("resetNameOutput");
		try {
			setNameOutput(dfe.getDFEOutput().keySet().iterator().next());
			logger.info("new name output: " + getNameOutput());
			logger.info("type browser: " + typeBrowser);
			logger.info("filesystem existence: " + getFileSystem() != null);
			logger.info("filesystem names: " + datastores.keySet());
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
		if (showOutputForm.equals("Y")) {
			for (OutputForm f : getOutputFormList()) {
				if (f.getName().equals(getNameOutput())) {
					f.setPath(path);
					logger.info("Output found: " + getNameOutput() + " - path: " + path);
				}
			}
		}
		
		setSourceNode(true);

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

			error = f.updateDFEOutput();
			if (error != null) {
				logger.error(error);
				MessageUseful.addErrorMessage(error);
				HttpServletRequest request = (HttpServletRequest) FacesContext
						.getCurrentInstance().getExternalContext().getRequest();
				request.setAttribute("msnError", "msnError");
			}
			logger.info("output ok");
		}

	}

	/**
	 * Display the first rows of data available from the given output. An
	 * outputName parameter is necesseray for running this function.
	 * 
	 * @throws RemoteException
	 */
	public void displayOutput() throws RemoteException {
		String outputN = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("outputName");

		if (outputN != null && !outputN.equalsIgnoreCase("undefined")) {
			setNameOutput(outputN);
			logger.info("display out: " + nameOutput);
			updateDFEOutputTable();
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
		} else {
			DFEOutput dfeOut = dfe.getDFEOutput().get(nameOutput);
			if (dfeOut == null) {
				logger.info("no output named: " + nameOutput);
			} else {
				LinkedList<String> gridTitle = new LinkedList<String>();
				
				
				
				
				/*List<SelectItem> listExtensions = new LinkedList<SelectItem>();
				if (dfeOut.getExtensions() != null && dfeOut.getExtensions().length != 0) {
					String[] listExt = dfeOut.getExtensions();
					for (int i = 0; i < listExt.length; i++) {
						String value = listExt[i];
						listExtensions.add(new SelectItem(value, value));
					}
					listExtensions.add(new SelectItem("*", "*"));
					getFileSystem().setExtensionsSelected(listExtensions.get(0).getLabel());
				}
				getFileSystem().setListExtensions(listExtensions);
				getFileSystem().updateTable();*/
				
				
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
				getFileSystem().updateTable();
				
				
				

				if (dfeOut.getFeatures() != null) {

					try {
						List<String> outputFeatureList = dfeOut.getFeatures()
								.getFeaturesNames();
						for (String outputFeature : outputFeatureList) {

							// logger.info("outputFeatureNames " +
							// outputFeature);
							FeatureType featureType = dfeOut.getFeatures()
									.getFeatureType(outputFeature);
							// logger.info("featureType " + featureType);

							gridTitle.add(outputFeature + " "
									+ featureType.toString());
						}
						logger.info("grid titles: " + gridTitle);
					} catch (Exception e) {
						logger.info("Error when getting the features: "
								+ e.getMessage());
					}
					grid = new UnselectableTable(gridTitle);
					try {
						List<Map<String, String>> outputLines = dfeOut
								.select(10);
						logger.info("line: " + outputLines);

						if (outputLines != null) {

							for (Map<String, String> line : outputLines) {
								int i = 0;
								String[] rowCur = new String[gridTitle.size()];
								for (String feat : line.keySet()) {
									rowCur[i] = line.get(feat);
									++i;
								}
								grid.add(rowCur);
							}
						}
					} catch (Exception e) {
						logger.info("Error when getting data: "
								+ e.getMessage());
					}

				}
			}
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
	public final String getNameOutput() {
		return nameOutput;
	}

	/**
	 * @param nameOutput
	 *            the nameOutput to set
	 */
	public final void setNameOutput(String nameOutput) {
		this.nameOutput = nameOutput;
		try {
			typeBrowser = dfe.getDFEOutput().get(nameOutput).getBrowser();
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
	public List<String[]> getRows() {
		return grid == null ? null : grid.getRows();
	}

	public boolean isSourceNode() {
		return sourceNode;
	}

	public void setSourceNode(boolean sourceNode) {
		this.sourceNode = sourceNode;
	}

}