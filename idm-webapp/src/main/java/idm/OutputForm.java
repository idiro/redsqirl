package idm;

import idiro.workflow.server.enumeration.DataBrowser;
import idiro.workflow.server.enumeration.SavingState;
import idiro.workflow.server.interfaces.DFEOutput;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

public class OutputForm implements Serializable {
	
	private DFEOutput dfeOutput;
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
		System.out.println("get path "+path);
		return path;
	}

	public void setPath(String path) throws RemoteException {
		System.out.println("set path "+path);
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

	public void setSavingState(String savingState) {
		this.savingState = savingState;
		setRenderBrowserButton(getSavingState().equals(SavingState.RECORDED.toString()));
	}

	public List<String> getNameOutputs(){
		List<String> list = new ArrayList<String>();
		list.add("Output1");
		list.add("Output2");
		return list;
	}
	
	public String updateDFEOutput() throws RemoteException{
		String completePath = getPath();
		if (!getPath().endsWith("/")){
			completePath += "/";
		}
		completePath += getFile();
		
		dfeOutput.setSavingState(SavingState.valueOf(getSavingState()));
		dfeOutput.setPath(completePath);
		System.out.println("complete path: "+dfeOutput.getPath());
		
		return dfeOutput.isPathValid();
	}
	
}