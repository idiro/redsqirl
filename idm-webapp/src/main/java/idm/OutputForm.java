package idm;

import idiro.workflow.server.enumeration.DataBrowser;
import idiro.workflow.server.enumeration.SavingState;
import idiro.workflow.server.interfaces.DFEOutput;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;

public class OutputForm implements Serializable {

	private static Logger logger = Logger.getLogger(OutputForm.class);

	private DFEOutput dfeOutput;
	private String componentId;
	private String name;
	private List<SelectItem> savingStateList = new ArrayList<SelectItem>();
	private boolean renderBrowserButton = false;
	private String savingState;
	private String path;
	private String file;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<SelectItem> getSavingStateList() {
		return savingStateList;
	}

	public void setSavingStateList(List<SelectItem> outputList) {
		this.savingStateList = outputList;
	}

	public boolean isRenderBrowserButton() {
		return renderBrowserButton;
	}

	public void setRenderBrowserButton(boolean renderButton) {
		this.renderBrowserButton = renderButton;
	}

	public String getPath() throws RemoteException {
		return path;
	}

	public void setPath(String path) throws RemoteException {
		this.path = path;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public DFEOutput getDfeOutput() {
		return dfeOutput;
	}

	public void setDfeOutput(DFEOutput dfeOutput) {
		this.dfeOutput = dfeOutput;
	}

	public boolean isHiveBrowser() throws RemoteException{
		return dfeOutput.getBrowser().equals(DataBrowser.HIVE);
	}

	public boolean isHdfsBrowser() throws RemoteException{
		return dfeOutput.getBrowser().equals(DataBrowser.HDFS);
	}

	public String getSavingState() {
		return savingState;
	}

	public void setSavingState(String savingState) throws RemoteException {
		if (this.savingState == null || !this.savingState.equals(savingState)){
			this.savingState = savingState;
			if (savingState.equals(SavingState.RECORDED.toString())){
				setRenderBrowserButton(true);
				setPath(null);
			}
			else if (savingState.equals(SavingState.BUFFERED.toString()) ||
					savingState.equals(SavingState.TEMPORARY.toString())){
				setRenderBrowserButton(false);
				if(getDfeOutput().isPathValid() != null){
					getDfeOutput().generatePath(
							System.getProperty("user.name"), 
							getComponentId(), 
							getName());
				}
				setPath(getDfeOutput().getPath());
			}
		}
	}

	public String getComponentId() {
		return componentId;
	}

	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}

	public List<String> getNameOutputs(){
		List<String> list = new ArrayList<String>();
		list.add("Output1");
		list.add("Output2");
		return list;
	}

	public String updateDFEOutput() throws RemoteException{

		if (getSavingState().equals(SavingState.RECORDED.toString())) {
			if (getPath() == null || getPath().isEmpty() || getFile() == null
					|| getFile().isEmpty()) {
				return "Path cannot be null";
			}

			String completePath = getPath();
			if (!getPath().endsWith("/")) {
				completePath += "/";
			}
			completePath += getFile();
			logger.info("path: " + completePath);
			dfeOutput.setPath(completePath);
		}

		dfeOutput.setSavingState(SavingState.valueOf(getSavingState()));

		return dfeOutput.isPathValid();
	}

}