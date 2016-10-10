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

package com.redsqirl.workflow.server.connect.hcat;

import java.sql.ResultSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Logger;

public class HCatConnection extends HCatObject{

	private static Logger logger = Logger.getLogger(HCatConnection.class);
	
	@Override
	protected Set<String> listObjectsPriv() {
		Set<String> ans = new LinkedHashSet<String>();
		try{
			ResultSet rs = getHiveConnection().executeQuery("SHOW DATABASES");
			while(rs.next()){
				ans.add(rs.getString(1));
			}
			rs.close();
		}catch(Exception e){
			logger.error(e,e);
			ans = null;
		}
		if(logger.isDebugEnabled() && ans != null){
			logger.debug(ans.toString());
		}
		return ans;
	}

}
