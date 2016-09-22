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

package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import com.redsqirl.workflow.server.BrowserInteraction;
import com.redsqirl.workflow.server.ListInteraction;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.interfaces.OozieAction;
import com.redsqirl.workflow.utils.LanguageManagerWF;

/**
 * Abstract action that read a source file. For now, Hive and HDFS types are supported.
 * This class has methods to create pages to select the Type (Hive or HDFS), Subtype and
 * the path to the file.
 * 
 * @author etienne
 * 
 */
public abstract class AbstractSource extends AbstractMultipleSources {

	private static final long serialVersionUID = 7519928238030041208L;
	
	private static Logger logger = Logger.getLogger(AbstractSource.class);
	
	/**
	 * Output name
	 */
	protected String out_name = "";
	
	protected Page page1, page2, page3;

	/**
	 * Constructor to initalize the DataFlowAction.
	 * 
	 * @throws RemoteException
	 */
	public AbstractSource(OozieAction action) throws RemoteException {
		super(action);
		
	}
	
	/**
	 * Add a page with a list interaction to select the Data Type.
	 * 
	 * @throws RemoteException
	 * 
	 */
	protected void addTypePage() throws RemoteException{
		page1 = addPage(LanguageManagerWF.getText("source.page1.title"),
				LanguageManagerWF.getText("source.page1.legend"), 1);

		addTypePage(page1,"");
	}
	
	/**
	 * Add a page with a list interaction to select the Data Sub Type
	 * 
	 * @throws RemoteException
	 * 
	 */
	protected void addSubTypePage() throws RemoteException{
		page2 = addPage(LanguageManagerWF.getText("source.page2.title"),
				LanguageManagerWF.getText("source.page2.legend"), 1);

		addSubTypePage(page2, "", out_name);
	}
	
	/**
	 * Add a page with a browser interaction to select the path to the file.
	 * 
	 * @throws RemoteException
	 * 
	 */
	protected void addSourcePage() throws RemoteException{
		page3 = addPage(LanguageManagerWF.getText("source.page3.title"),
				LanguageManagerWF.getText("source.page3.legend"), 1);

		addSourcePage(page3, "");
		
	}
	
	protected Page getSourcePage(){
		return page3;
	}

	protected BrowserInteraction getBrowser() throws RemoteException{
		return getBrowser("");
	}
	protected ListInteraction getDatatype() throws RemoteException{
		return getDataType("");
	}
	protected ListInteraction getDataSubtype() throws RemoteException{
		return getDataSubType("");
	}

	public final String getOut_name() {
		return out_name;
	}
	
}
