package idiro.workflow.server.action;

import idiro.utils.TreeNonUnique;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.Test;

public class PigDelimiterTest {
	private Logger logger = Logger.getLogger(getClass());
	
	@Test
	public void DelimiterTest(){
		try {
			PigSelect select = new PigSelect();
			select.updateDelimiterOutputInt();
			logger.info(((TreeNonUnique<String>)select.delimiterOutputInt.getTree()).toString());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
