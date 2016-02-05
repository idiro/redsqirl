/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

package com.redsqirl;


import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

import com.redsqirl.auth.UserInfoBean;

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
	//@PostConstruct
	public void openCanvasScreen() {
		logger.info("HiveOpenCanvasScreen");

		try {
			setDataStore(getHiveInterface());

			if(getTableGrid() != null && 
					getTableGrid().getRows() != null &&
					getTableGrid().getRows().isEmpty()){
				mountTable();
			}

			/*FacesContext context = FacesContext.getCurrentInstance();
			UserInfoBean userInfoBean = (UserInfoBean) context.getApplication()
					.evaluateExpressionGet(context, "#{userInfoBean}",
							UserInfoBean.class);
			
			userInfoBean.setValueProgressBar(userInfoBean.getValueProgressBar()+24);*/

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