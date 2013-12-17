package idiro.workflow.server.action;

import idiro.utils.TreeNonUnique;
import idiro.workflow.server.datatype.MapRedBinaryType;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.Test;

public class PigOutputTypeTest {
	private Logger logger = Logger.getLogger(getClass());
	@Test
	public void PigtestOutput() throws RemoteException, InstantiationException, IllegalAccessException{
		
		PigSelect select = new PigSelect();
		
		select.updateOutputType();
		select.savetypeOutputInt.getTree().getFirstChild("list").getFirstChild("output").removeAllChildren();
		select.savetypeOutputInt.getTree().getFirstChild("list").getFirstChild("output").add(MapRedBinaryType.class.newInstance().getTypeName());
		logger.info(((TreeNonUnique<String>)select.savetypeOutputInt.getTree()).toString());
		logger.info(select.getStoreQueryPiece(null,"|"));
	}
}
