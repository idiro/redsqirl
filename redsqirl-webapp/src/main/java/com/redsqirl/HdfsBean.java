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


import java.rmi.RemoteException;

import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.richfaces.event.DropEvent;

import com.redsqirl.workflow.server.connect.SSHInterface;
import com.redsqirl.workflow.server.connect.interfaces.DataStore;

/** HdfsBean
 * 
 * Class to screen control of the File System Hadoop
 * 
 * @author Igor.Souza
 */
public class HdfsBean extends FileSystemBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3643315645652064815L;

	private static Logger logger = Logger.getLogger(HdfsBean.class);
	
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
		logger.info("HdfsOpenCanvasScreen");
		try {

			setDataStore(getRmiHDFS());

			if(getTableGrid() != null && 
					getTableGrid().getRows() != null &&
					getTableGrid().getRows().isEmpty()){
				mountTable();
			}
			
			/*FacesContext context = FacesContext.getCurrentInstance();
			UserInfoBean userInfoBean = (UserInfoBean) context.getApplication()
					.evaluateExpressionGet(context, "#{userInfoBean}",
							UserInfoBean.class);
			
			userInfoBean.setValueProgressBar(userInfoBean.getValueProgressBar() + 12);*/

		}catch(Exception e){
			logger.error(e);
			getBundleMessage("error.mount.table");
		}

	}
	
	/*public void addFileAfter() throws RemoteException{
		setNameValue(new HashMap<String, String>());
		super.addFileAfter();
	}*/
	
	public void processDrop(DropEvent dropEvent) throws RemoteException {
		logger.info("processDrop");
		
		FacesContext context = FacesContext.getCurrentInstance();
		String file = context.getExternalContext().getRequestParameterMap().get("file");
		String path = context.getExternalContext().getRequestParameterMap().get("path");
		String server = context.getExternalContext().getRequestParameterMap().get("server");
		
		logger.info("copy from "+server+":"+getPath()+"/"+file+" to "+path+"/"+file);
		try{
			getRmiHDFS().copyFromRemote(path+"/"+file, getPath()+"/"+file, 
					server);
			mountTable();
		}
		catch(Exception e){
			logger.info("", e);
		}
	}
	
	public DataStore getRmiHDFS() throws RemoteException{
		return getHDFS();
	}

	public String getTableState() {
		return tableState;
	}

	public void setTableState(String tableState) {
		this.tableState = tableState;
	}

}