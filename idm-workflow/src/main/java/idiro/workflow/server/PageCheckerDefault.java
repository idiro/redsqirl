package idiro.workflow.server;

import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFEPage;
import idiro.workflow.server.interfaces.PageChecker;

import java.rmi.RemoteException;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * Default page checker.
 * @author etienne
 *
 */
public class PageCheckerDefault 
implements PageChecker{

	protected static Logger logger = Logger.getLogger(PageCheckerDefault.class);
	
	@Override
	public String check(DFEPage page) throws RemoteException {
		logger.debug("Check page "+page.getTitle());
		String error = "";
		Iterator<DFEInteraction> it = page.getInteractions().iterator();
		while(it.hasNext()){
			DFEInteraction eInt = it.next();
			logger.debug("check interaction "+eInt.getId());
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
