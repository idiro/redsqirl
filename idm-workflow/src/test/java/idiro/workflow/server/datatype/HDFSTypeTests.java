package idiro.workflow.server.datatype;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

public class HDFSTypeTests {

	private Logger logger = Logger.getLogger(getClass());

	@Test
	public void MapRedBinTypeTest() throws RemoteException {
		try {
			MapRedBinaryType bin = new MapRedBinaryType();
			bin.setPath("/user/keith/tmp/keith/binfile");
			List<String> first = bin.select(1);
			logger.info(first.toString());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

}
