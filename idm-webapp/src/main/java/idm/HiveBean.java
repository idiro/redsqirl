package idm;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;

/** HiveBean
 * 
 * Class to screen control of the File System Hive
 * 
 * @author Igor.Souza
 */
public class HiveBean extends FileSystemBean {

	private static Logger logger = Logger.getLogger(HiveBean.class);
	
	private String tableState = new String();

	/** openCanvasScreen
	 * 
	 * Methods to generating screen
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	@PostConstruct
	public void openCanvasScreen() {
		logger.info("HiveOpenCanvasScreen");

		try {
			setDataStore(getHiveInterface());

			if(getTableGrid() != null && 
					getTableGrid().getRows() != null &&
					getTableGrid().getRows().isEmpty()){
				mountTable(getDataStore());
			}
			/*else{
				for (EditFileSystem item : getListGrid()){
					item.setSelected(false);
				}
			}*/

		}catch(Exception e){
			logger.error(e);
			getBundleMessage("error.mount.table");
		}

	}
	
	public String getFormatedString(String property, String value){
		if (property.equals("describe")){
			return value.replace(",", " ").replace(";", ", ");
		}
		return value;
	}
	
	public String getTableState() {
		return tableState;
	}

	public void setTableState(String tableState) {
		this.tableState = tableState;
	}
	
}