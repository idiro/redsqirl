package com.redsqirl;

import org.apache.log4j.Logger;

public class HCatBean extends FileSystemBean {

	private static Logger logger = Logger.getLogger(JdbcBean.class);
	
	private String tableState = new String();

	/** openCanvasScreen
	 * 
	 * Methods to generating screen
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	//@PostConstruct
	public void openCanvasScreen() {
		logger.info("HiveOpenCanvasScreen");

		try {
			setDataStore(getHCatInterface());

			if(getTableGrid() != null && 
					getTableGrid().getRows() != null &&
					getTableGrid().getRows().isEmpty()){
				mountTable();
			}

		}catch(Exception e){
			logger.error(e,e);
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
