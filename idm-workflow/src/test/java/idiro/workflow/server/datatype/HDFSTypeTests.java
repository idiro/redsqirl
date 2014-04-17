package idiro.workflow.server.datatype;

import org.apache.log4j.Logger;

public class HDFSTypeTests {

	private Logger logger = Logger.getLogger(getClass());
	
	/* Method cannot run because the bin file is not available...
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
	*/

}
