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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

import org.apache.log4j.Logger;

public class ReservedWords {
	
	private static Logger logger = Logger.getLogger(ReservedWords.class);
	private static ReservedWords instance;
	private static final String keywordFileName = "reserved_keywords.txt";
	
	private HashSet<String> keywords = null;
	
	private static ReservedWords getInstance(){
		if(instance == null){
			instance = new ReservedWords();
		}
		return instance;
	}
	
	private ReservedWords(){
		keywords = new HashSet<String>(875);
		try{
			InputStream in = getClass().getResourceAsStream("/"+keywordFileName ); 
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line ="";
			while ((line = reader.readLine()) != null) {
				keywords.add(line);
			}
			reader.close();
		}catch(Exception e){
			logger.warn(e,e);
		}
	}
	
	public static boolean isReservedWord(String word){
		return word == null ? true: getInstance().keywords.contains(word.toLowerCase());
	}
}
