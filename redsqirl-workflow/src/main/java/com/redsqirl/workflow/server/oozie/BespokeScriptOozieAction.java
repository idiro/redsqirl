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

package com.redsqirl.workflow.server.oozie;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.redsqirl.workflow.server.DataflowAction;
import com.redsqirl.workflow.server.OozieUniqueActionAbs;
import com.redsqirl.workflow.server.connect.hcat.HCatStore;
import com.redsqirl.workflow.server.connect.hcat.HCatalogType;
import com.redsqirl.workflow.server.interfaces.DFEOutput;

public class BespokeScriptOozieAction extends OozieUniqueActionAbs{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7568122712803414272L;
	private static Logger logger = Logger.getLogger(BespokeScriptOozieAction.class);
	
	private DataflowAction act;
	
	private String fileExtension;
	
	private String xmlContent;

	public BespokeScriptOozieAction() throws RemoteException {
		super();
	}

	public BespokeScriptOozieAction(DataflowAction act) throws RemoteException {
		super();
		this.act = act;
	}

	@Override
	public String[] getFileExtensions() throws RemoteException {
		if(fileExtension != null && !fileExtension.isEmpty()){
			return new String[]{fileExtension};
		}
		return new String[]{};
	}

	@Override
	public void createOozieElement(Document oozieXmlDoc, Element action, String[] fileNames) throws RemoteException {
		try{
			action.appendChild(readOozieInt(oozieXmlDoc));
		}catch(Exception e){
			logger.error(e,e);
		}
	}
	

	
	public String replaceRSVariables(String content) throws RemoteException{
		String ans = content;
		Iterator<Entry<String,String>> it = getRSVariablesAvailable().entrySet().iterator();
		while(it.hasNext()){
			Entry<String,String> cur = it.next();
			if(cur.getValue() != null){
				if(logger.isDebugEnabled()){
					logger.debug("Replace variable "+cur.getKey()+" by "+cur.getValue());
				}
				ans = ans.replaceAll(Pattern.quote(cur.getKey()), cur.getValue());
			}
		}
		
		return ans;
	}
	
	public static Element readOozieInt(String oozieActionXml) throws ParserConfigurationException, RemoteException, SAXException, IOException{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(oozieActionXml.getBytes("utf-8"))));
		return (Element) doc.getChildNodes().item(0);
	}
	
	public static Element readOozieInt(Document doc, String oozieActionXml) throws ParserConfigurationException, RemoteException, SAXException, IOException{
		return (Element) doc.importNode(readOozieInt(oozieActionXml), true);
	}

	public Element readOozieInt() throws ParserConfigurationException, RemoteException, SAXException, IOException{
		return readOozieInt(replaceRSVariables(xmlContent));
	}
	
	protected Element readOozieInt(Document doc) throws ParserConfigurationException, RemoteException, SAXException, IOException{
		return readOozieInt(doc,replaceRSVariables(xmlContent));
	}
	
	
	public Map<String,String> getRSVariablesAvailable() throws RemoteException{
		String scriptExtension = getFileExtension();
		Map<String,String> ans = new LinkedHashMap<String,String>();
		Set<Entry<String,Entry<String,DFEOutput>>> aliases = act.getAliasesPerComponentInput().entrySet();
		Iterator<Entry<String,Entry<String,DFEOutput>>> itIn = aliases.iterator();
		if(scriptExtension != null && !scriptExtension.isEmpty()){
			ans.put("!{SCRIPT}","scripts/"+act.getComponentId()+scriptExtension);
		}
		int i = 0;
		while(itIn.hasNext()){
			Entry<String,Entry<String,DFEOutput>> inCur = itIn.next();
			DFEOutput dfeCur = inCur.getValue().getValue();
			if(dfeCur instanceof HCatalogType){
				String[] pathArr = HCatStore.getDatabaseTableAndPartition(dfeCur.getPath());
				String filter = null;
				if(pathArr.length > 2){
					filter = "";
					if(pathArr[2].contains(";")){
						filter += "(";
					}
					filter += pathArr[2].replaceAll(";", "' AND ").replaceAll("=","='")+"'";
					if(pathArr[2].contains(";")){
						filter += ")";
					}
				}
				if(aliases.size() == 1){
					ans.put("!{INPUT_DATABASE}",pathArr[0]);
					ans.put("!{INPUT_TABLE}",pathArr[1]);
					if(filter != null){
						ans.put("!{INPUT_FILTER_HIVE}",filter);
					}
				}else{
					ans.put("!{INPUT_DATABASE_"+i+"}",pathArr[0]);
					ans.put("!{INPUT_TABLE_"+i+"}",pathArr[1]);
					if(filter != null){
						ans.put("!{INPUT_FILTER_HIVE_"+i+"}",filter);
					}
				}
				ans.put("!{INPUT_DATABASE_"+inCur.getKey()+"}",pathArr[0]);
				ans.put("!{INPUT_TABLE_"+inCur.getKey()+"}",pathArr[1]);
				if(filter != null){
					ans.put("!{INPUT_FILTER_HIVE_"+inCur.getKey()+"}",filter);
				}
			}
			if(aliases.size() == 1){
				ans.put("!{INPUT_PATH}",dfeCur.getPath());
			}else{
				ans.put("!{INPUT_PATH_"+i+"}",dfeCur.getPath());
			}
			ans.put("!{INPUT_PATH_"+inCur.getKey()+"}",dfeCur.getPath());
			++i;
		}
		
		Iterator<Entry<String,DFEOutput>> itOut = act.getDFEOutput().entrySet().iterator();
		int outputSize = act.getDFEOutput().size();
		i = 0;
		while(itOut.hasNext()){
			Entry<String,DFEOutput> curOut = itOut.next();
			DFEOutput dfeCur = curOut.getValue();
			String path = dfeCur.getPath();
			if(dfeCur instanceof HCatalogType){
				String partition = null;
				String database = "TBD";
				String table = "TBD";
				if(path != null){
					String[] pathArr = HCatStore.getDatabaseTableAndPartition(path);
					database = pathArr[0];
					table =pathArr[1];
					if(pathArr.length > 2){
						partition = "("+pathArr[2].replaceAll(";", "', ").replaceAll("=","='")+"')";
					}
				}
				if(outputSize == 1){
					ans.put("!{OUTPUT_DATABASE}",database);
					ans.put("!{OUTPUT_TABLE}",table);
					ans.put("!{OUTPUT_PARTITION}",partition);
					
				}else{
					ans.put("!{OUTPUT_DATABASE_"+i+"}",database);
					ans.put("!{OUTPUT_TABLE_"+i+"}",table);
					ans.put("!{OUTPUT_PARTITION_"+i+"}",partition);
				}
				ans.put("!{OUTPUT_DATABASE_"+curOut.getKey()+"}",database);
				ans.put("!{OUTPUT_TABLE_"+curOut.getKey()+"}",table);
				ans.put("!{OUTPUT_PARTITION_"+curOut.getKey()+"}",partition);
			}
			if(path == null){
				path = "TBD";
			}
			if(outputSize == 1 || curOut.getKey().isEmpty()){
				ans.put("!{OUTPUT_PATH}",path);
			}else{
				ans.put("!{OUTPUT_PATH_"+i+"}",path);
			}
			if(!curOut.getKey().isEmpty()){
				ans.put("!{OUTPUT_PATH_"+curOut.getKey()+"}",path);
			}
			++i;
		}
		
		if(logger.isDebugEnabled()){
			logger.debug("RS Variables: "+ans.toString());
		}
		
		return ans;
	}
	
	@Override
	public boolean supportsExtraJobParameters()  throws RemoteException{
		return false;
	}

	/**
	 * @return the fileExtension
	 */
	public String getFileExtension() {
		return fileExtension;
	}

	/**
	 * @param fileExtension the fileExtension to set
	 */
	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	/**
	 * @return the xmlContent
	 */
	public String getXmlContent() {
		return xmlContent;
	}

	/**
	 * @param xmlContent the xmlContent to set
	 */
	public void setXmlContent(String xmlContent) {
		this.xmlContent = xmlContent;
	}

	public final DataflowAction getAct() {
		return act;
	}

	public final void setAct(DataflowAction act) {
		this.act = act;
	}

}
