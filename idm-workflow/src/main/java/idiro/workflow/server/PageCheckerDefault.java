package idiro.workflow.server;

import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFEPage;
import idiro.workflow.server.interfaces.PageChecker;

import java.rmi.RemoteException;
import java.util.Iterator;

/**
 * Default page checker.
 * @author etienne
 *
 */
public class PageCheckerDefault 
implements PageChecker{

	@Override
	public String check(DFEPage page) throws RemoteException {
		String error = "";
		Iterator<DFEInteraction> it = page.getInteractions().iterator();
		while(it.hasNext()){
			DFEInteraction eInt = it.next();
			String loc_error = eInt.check();
			if(loc_error != null){
				error += eInt.getName()+": "+loc_error +"\n";
			}
		}
		if(error.isEmpty()){
			error = null;
		}
		return error;
	}
	
	

}
