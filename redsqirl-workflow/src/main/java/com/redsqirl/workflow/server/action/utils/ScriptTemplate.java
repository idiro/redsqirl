package com.redsqirl.workflow.server.action.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.DataOutput;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.interfaces.DFEOutput;

public class ScriptTemplate implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9076176595306515172L;
	private static Logger logger = Logger.getLogger(ScriptTemplate.class);
	public final String oozieXmlFileName = "oozie-action.xml";
	public final String scriptFileName = "script";
	public final String scriptPropFileName = "script.properties";
	
	//Properties key
	public static final String
	/**
	 * ',' delimited list of output
	 */
	output_names = "output.names",
	/**
	 * type of every output
	 */
	output_type = "output.type",
	/**
	 * Header of the output
	 */
	output_header = "output.header",
	/**
	 * Minimum number of input
	 */
	input_number_min = "input.number.min",
	/**
	 * Maximum number of output
	 */
	input_number_max = "input.number.max",
	/**
	 * Type of input
	 */
	input_type = "input.type",
	/**
	 * Input Header
	 */
	input_header = "input.header",
	/**
	 * Script Extension
	 */
	script_ext = "script.extension";
	
	protected String name;
	protected Properties scriptProp;
	protected File oozieActionXml;
	protected File script;
	
	public static File getSysScriptFile(){
		return new File(WorkflowPrefManager.getPathSysScripts());
	}
	
	public static File getUserScriptFile(){
		return new File(WorkflowPrefManager.getPathUserScripts());
	}
	
	public static Set<String> getSysTemplateNames(){
		return getTemplates(getSysScriptFile(),false);
	}
	
	public static Set<String> getUserTemplateNames(){
		return getTemplates(getUserScriptFile(),false);
	}
	
	public static Set<String> getTemplateNames(){
		Set<String> ans = getSysTemplateNames();
		ans.addAll(getUserTemplateNames());
		return ans;
	}
	
	public static Set<String> getBespokeNames(){
		Set<String> ans = getTemplates(getSysScriptFile(),true);
		ans.addAll(getTemplates(getUserScriptFile(),true));
		return ans;
	}
	
	protected static Set<String> getTemplates(File confDir, final boolean bespoke){
		
		File[] files = confDir.listFiles(new FileFilter() {
				
				@Override
				public boolean accept(File pathname) {
					return pathname.isDirectory() && pathname.getName().matches("[\\w-]*") && (!bespoke || new File(pathname,"bespoke").exists());
				}
			});
		if(files == null){
			return new HashSet<String>();
		}
		
		Set<String> ans = new HashSet<String>(files.length);
		for(int i=0; i<files.length;++i){
			ans.add(files[i].getName());
		}
		if(logger.isDebugEnabled()){
			if(bespoke){
				logger.debug("Template scripts found: "+ans.toString());
			}else{
				logger.debug("Bespoke scripts found: "+ans.toString());
			}
		}
		
		return ans;
	}
	
	public ScriptTemplate(String name){
		File f = null;
		if(getUserTemplateNames().contains(name)){
			f = new File(getUserScriptFile(),name);
		}else if(getSysTemplateNames().contains(name)){
			f = new File(getSysScriptFile(),name);
		}
		
		if(f != null && f.exists()){
			this.name = name;
			oozieActionXml = new File(f,oozieXmlFileName);
			script = new File(f,scriptFileName);
			File prop = new File(f,scriptPropFileName);
			if(prop.exists()){
				scriptProp = new Properties();
				try {
					scriptProp.load(new FileInputStream(prop));
				} catch (FileNotFoundException e) {
					logger.warn(e,e);
				} catch (IOException e) {
					logger.warn(e,e);
				}
			}
		}
	}
	
	public String readOozie(){
		return readFile(oozieActionXml);
	}
	
	public String readScript(){
		return readFile(script);
	}
	
	protected String readFile(File file){
		if(file == null || !file.exists()){
			return null;
		}
		try{
			return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
		}catch(Exception e){
			logger.error(e,e);
		}
		return null;
	}
	
	public List<String> getOutputNames(){
		return Arrays.asList(scriptProp.getProperty(output_names).split(","));
	}
	
	public FieldList getOutputFieldList(String outputName) throws Exception{
		FieldList ans = null;
		String featStr = null;
		if(outputName == null || outputName.isEmpty()){
			featStr = scriptProp.getProperty(output_header);
		}else{
			featStr = scriptProp.getProperty(output_header+"."+outputName);
		}
		if(featStr != null && !featStr.isEmpty()){
			ans = getFieldListFromHeader(featStr);
		}
		return ans;
	}
	
	public FieldList getInputFieldList() throws Exception{
		FieldList ans = null;
		String featStr = null;
		featStr = scriptProp.getProperty(input_header);
		if(featStr != null && !featStr.isEmpty()){
			ans = getFieldListFromHeader(featStr);
		}
		return ans;
	}
	
	private FieldList getFieldListFromHeader(String header) throws Exception{
		String error = null;
		FieldList ans = new OrderedFieldList();
		logger.debug("Input header: "+header);
		String[] feats = header.split(",");
		for(int i =0; i < feats.length;++i){
			logger.debug("Input field "+i+": "+feats[i]);
			String[] fieldAttr = feats[i].trim().split("\\s+");
			try{
				ans.addField(fieldAttr[0].trim(), FieldType.valueOf(fieldAttr[1].trim().toUpperCase()));
			}catch(Exception e){
				error = "Fail to add field: '"+fieldAttr[0]+"', '"+fieldAttr[1]+"'";
				logger.error(error+e.getMessage(),e);
			}
		}

		if(error != null){
			throw new Exception(error);
		}
		return ans;
	}
	
	public DFEOutput getNewOutput(String outputName){
		DFEOutput ans = null;
		if(outputName == null || outputName.isEmpty()){
			ans = DataOutput.getOutput(scriptProp.getProperty(output_type));
		}else{
			ans = DataOutput.getOutput(scriptProp.getProperty(output_type+"."+outputName));
		}
		return ans;
	}
	
	public Integer getInputNumberMin(){
		Integer ans = null;
		try{
			ans = Integer.getInteger(scriptProp.getProperty(input_number_min)); 
		}catch(Exception e){}
		return ans;
	}
	
	public Integer getInputNumberMax(){
		Integer ans = null;
		try{
			ans = Integer.getInteger(scriptProp.getProperty(input_number_max)); 
		}catch(Exception e){}
		return ans;
	}
	
	public List<Class<? extends DFEOutput>> getInputTypes(){
		List<Class<? extends DFEOutput>> ans = null;
		String inputTypes = scriptProp.getProperty(input_type);
		if(inputTypes != null && !inputTypes.isEmpty()){
			String[] inputTypesArr = inputTypes.split(",");
			ans = new ArrayList<Class<? extends DFEOutput>>(inputTypesArr.length);
			for(String inputType: inputTypesArr){
				ans.add(DataOutput.getOutput(inputType).getClass());
			} 
		}
		return ans;
	}
	
	public final String getExtension() throws RemoteException{
		return scriptProp.getProperty(script_ext);
	}

	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	public final Properties getScriptProp() {
		return scriptProp;
	}

	public final void setScriptProp(Properties scriptProp) {
		this.scriptProp = scriptProp;
	}

	public final File getOozieActionXml() {
		return oozieActionXml;
	}

	public final void setOozieActionXml(File oozieActionXml) {
		this.oozieActionXml = oozieActionXml;
	}

	public final File getScript() {
		return script;
	}

	public final void setScript(File script) {
		this.script = script;
	}

}
