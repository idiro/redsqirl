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

package com.redsqirl.workflow.server.enumeration;

/**
 * Path Type, describes what kind of operations are available for a path:
 * - REAL: a real path describes a string on which a dataset can be saved
 * - TEMPLATE: a path that includes variables that has to change at running time
 * - MATERIALIZED: a path with variables, for which every variables needed for using it as input is set.
 * 
 * @author etienne
 *
 */
public enum PathType {
	/**
	 * Real Path
	 */
	REAL,
	/**
	 * Template Path
	 */
	TEMPLATE,
	/**
	 * Materialized Path (Template path with variables set)
	 */
	MATERIALIZED
}
