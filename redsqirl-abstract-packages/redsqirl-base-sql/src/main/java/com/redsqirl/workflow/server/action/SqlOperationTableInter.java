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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.TableInteraction;
import com.redsqirl.workflow.server.action.utils.SqlDictionary;
import com.redsqirl.workflow.server.enumeration.FieldType;

public abstract class SqlOperationTableInter  extends TableInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 169938426599555676L;

	protected Map<String,String> dictionaryCach = new LinkedHashMap<String,String>();

	public SqlOperationTableInter(String id, String name, String legend,
			int column, int placeInColumn) throws RemoteException {
		super(id, name, legend, column, placeInColumn);
		setVariableDisable(false);
	}

	public SqlOperationTableInter(String id, String name, String legend,
			String texttip, int column, int placeInColumn)
			throws RemoteException {
		super(id, name, legend, texttip, column, placeInColumn);
		setVariableDisable(false);
	}

	protected abstract SqlDictionary getDictionary() throws RemoteException;
	
	protected boolean isDifferentDictionary(String columnName) throws RemoteException{
		SqlDictionary dic = getDictionary();
		logger.debug(dic.getId()+" vs "+getDictionaryId(columnName));
		return dic.getId() == null || !dic.getId().equals(getDictionaryId(columnName));
	}
	

	protected boolean changeKeyWords(String colomnName, FieldList feats) throws RemoteException{
		boolean ans = false;
		Map<String,List<String>> keyW = getKeyWordsFromEditor(colomnName);
		Map<String,List<String>> calcW = feats.getMapList();
		Map<String,List<String>> extras = getExtraKeyWords();
		
		if(keyW == null){
			ans = true;
		}else{
			if(extras == null && !keyW.keySet().equals(calcW.keySet())){
				ans = true;
			}else if(extras != null){
				Set<String> comp = new HashSet<String>();
				comp.addAll(calcW.keySet());
				comp.addAll(extras.keySet());
				if(!keyW.keySet().equals(comp)){
					ans = true;
				}
			}
		}
		if(!ans){

			Iterator<String> it = keyW.keySet().iterator();
			while(it.hasNext() && !ans){
				String keyCur = it.next();
				List<String> keyWL = keyW.get(keyCur);
				List<String> fL = calcW.get(keyCur);
				List<String> extraL = null;
				if(fL == null){
					ans = true;
					break;
				}
				if(extras != null && (extraL = extras.get(keyCur)) != null ){
					fL.addAll(extraL);
				}
				ans = !(keyWL.containsAll(fL) && fL.containsAll(keyWL));
			}
		}

		if(ans){
			if(extras == null){
				updateEditor(colomnName, feats.getMap());
			}else{
				updateEditor(colomnName, feats.getMap(), extras);
			}
		}
		return ans;
	}
	
	protected Map<String,List<String>> getExtraKeyWords() throws RemoteException{
		return null;
	}
}
