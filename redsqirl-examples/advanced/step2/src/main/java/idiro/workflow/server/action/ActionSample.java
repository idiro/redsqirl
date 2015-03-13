package idiro.workflow.server.action;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Map;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;

public class ActionSample extends DemoAction {
	
	
	
	public ActionSample() throws RemoteException {
		super();
	}

	public String getName() throws RemoteException {
		
		return "sample";
	}

	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		return null;
	}

	public String updateOut() throws RemoteException {
		return null;
	}

	@Override
	public boolean writeOozieActionFiles(File[] files) throws RemoteException {
		return false;
	}

	@Override
	public void update(DFEInteraction interaction) throws RemoteException {

	}

	@Override
	public String getQuery() throws RemoteException {
		return null;
	}

	@Override
	public FieldList getNewFeatures() throws RemoteException {
		return null;
	}

	@Override
	public FieldList getInFeatures() throws RemoteException {
		return null;
	}

}
