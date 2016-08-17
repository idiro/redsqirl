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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.idiro.hadoop.NameNodeVar;
import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.DataOutput;
import com.redsqirl.workflow.server.InputInteraction;
import com.redsqirl.workflow.server.ListInteraction;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.connect.hcat.HCatStore;
import com.redsqirl.workflow.server.connect.hcat.HCatalogType;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.enumeration.PathType;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.enumeration.TimeTemplate;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFEInteractionChecker;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.utils.LanguageManagerWF;

/**
 * Action that read a source file. For now, Hive and HDFS types are supported.
 * 
 * @author etienne
 * 
 */
public class SyncSource extends AbstractSource {

	private static final long serialVersionUID = 7519928238030041208L;

	private static Logger logger = Logger.getLogger(SyncSource.class);
	public final static String out_template = "template",
			inter_template = "template_path",
			inter_unit = "unit",
			inter_freq = "frequency";
	
	protected Page page4,page5;
	protected InputInteraction templatePath;

	private ListInteraction unit;

	private InputInteraction frequency;
	
	/**
	 * Constructor containing the pages, page checks and interaction
	 * Initialization
	 * 
	 * @throws RemoteException
	 */
	public SyncSource() throws RemoteException {
		super(null);

		addTypePage();
		addSubTypePage();
		addSourcePage();
		
		page4 = addPage(LanguageManagerWF.getText("sync_source.page4.title"),
				LanguageManagerWF.getText("sync_source.page4.legend"), 1);
		
		//Data Set Type
		templatePath = new InputInteraction(
				inter_template, 
				LanguageManagerWF.getText("sync_source.template_path.title"),
				LanguageManagerWF.getText("sync_source.template_path.legend")
				, 0, 0);
		
		templatePath.setChecker(new DFEInteractionChecker() {
			
			@Override
			public String check(DFEInteraction interaction) throws RemoteException {
				return checkTemplatePath(output.get(out_name).getPath(), templatePath.getValue());
			}
		});
		
		page4.addInteraction(templatePath);
		
		page5 = addPage(LanguageManagerWF.getText("sync_source.page5.title"),
				LanguageManagerWF.getText("sync_source.page5.legend"), 1);
		
		unit = new ListInteraction(
				inter_unit, 
				LanguageManagerWF.getText("sync_source.unit.title"),
				LanguageManagerWF.getText("sync_source.unit.legend")
				, 0, 0);
		
		frequency = new InputInteraction(
				inter_freq, 
				LanguageManagerWF.getText("sync_source.frequency.title"),
				LanguageManagerWF.getText("sync_source.frequency.legend")
				, 0, 0);
		frequency.setValue("1");
		frequency.setRegex("[1-9][0-9]{0,1}");
		
		page5.addInteraction(unit);
		page5.addInteraction(frequency);
		
		
	}
	
	protected void initializeDataTypeInteraction() throws RemoteException{
		dataType = new ListInteraction(
				key_datatype,
				LanguageManagerWF.getText("source.datatype_interaction.title"),
				LanguageManagerWF.getText("source.datatype_interaction.legend"),
				0, 0);
		dataType.setReplaceDisable(true);
		dataType.setDisplayRadioButton(true);
		List<String> posValues = new LinkedList<String>();
		posValues.add(new HDFSInterface().getBrowserName());
		posValues.add(new HCatStore().getBrowserName());
		dataType.setPossibleValues(posValues);
	}
	
	protected void updateUnit() throws RemoteException{
		Pattern p = Pattern.compile("\\$\\{(.*?)\\}");
		Matcher m = p.matcher(templatePath.getValue());
		TimeTemplate minTemp = null;
		while(m.find()){
			try{
				TimeTemplate cur = TimeTemplate.valueOf(m.group(1));
				boolean isMin = false;
				if(cur != null){
					switch(cur){
					case DAY:
						isMin = minTemp == null || TimeTemplate.YEAR.equals(minTemp) || TimeTemplate.MONTH.equals(minTemp);
						break;
					case HOUR:
						isMin = minTemp == null || TimeTemplate.YEAR.equals(minTemp) || TimeTemplate.MONTH.equals(minTemp) 
							|| TimeTemplate.DAY.equals(minTemp);
						break;
					case MINUTE:
						isMin = minTemp == null || TimeTemplate.YEAR.equals(minTemp) || TimeTemplate.MONTH.equals(minTemp) 
						|| TimeTemplate.DAY.equals(minTemp) || TimeTemplate.HOUR.equals(minTemp);
						break;
					case MONTH:
						isMin = minTemp == null || TimeTemplate.YEAR.equals(minTemp);
						break;
					case YEAR:
						isMin = minTemp == null;
						break;
					default:
						break;
					}
					if(isMin){
						minTemp = cur;
					}
				}
			}catch(Exception e){
			}
		}
		String defaultVal = minTemp.toString();
		List<String> freqPos = new LinkedList<String>();
		switch(minTemp){
		case MINUTE:
			freqPos.add(TimeTemplate.MINUTE.toString());
		case HOUR:
			freqPos.add(TimeTemplate.HOUR.toString());
		case DAY:
			freqPos.add(TimeTemplate.DAY.toString());
		case MONTH:
			freqPos.add(TimeTemplate.MONTH.toString());
		case YEAR:
			freqPos.add(TimeTemplate.YEAR.toString());
		}
		unit.setPossibleValues(freqPos);
		unit.setValue(defaultVal);
	}
	
	protected void updateTemplatePath() throws RemoteException{
		DFEOutput startInstance = output.get(out_name);
		String curVal = templatePath.getValue();
		if(curVal == null ||curVal.isEmpty()){
			templatePath.setValue(startInstance.getPath());
		}
		
	}
	
	
	protected static String checkTemplatePath(String realPath, String templatePathStr){
		Pattern p = Pattern.compile("\\$\\{(.*?)\\}");
		Matcher m = p.matcher(templatePathStr);
		String error = null; 
		String buildingStr = "";
		int prevIndex = 0;
		int found = 0;
		logger.debug(realPath);
		while(m.find() && error == null){
			logger.debug(buildingStr);
			++found;
			boolean match = true;
			buildingStr += templatePathStr.substring(prevIndex, m.start());
			prevIndex = m.end();
			try{
				TimeTemplate cur = TimeTemplate.valueOf(m.group(1)); 
				if(match = cur != null){
					String toAdd = "";
					int realPathSubStart = buildingStr.length();
					switch(cur){
					case DAY:
						toAdd = realPath.substring(realPathSubStart,realPathSubStart+2);
						if(!toAdd.matches("^[0-9][0-9]$") || Integer.valueOf(toAdd.startsWith("0")?toAdd.substring(1):toAdd) > 31){
							error = "A day should have a value between 0 and 31, given ("+toAdd+")";
						}
						break;
					case HOUR:
						toAdd = realPath.substring(realPathSubStart,realPathSubStart+2);
						if(!toAdd.matches("^[0-9][0-9]$") || Integer.valueOf(toAdd.startsWith("0")?toAdd.substring(1):toAdd) > 23){
							error = "An hour should have a value between 0 and 23, given ("+toAdd+")";
						}
						break;
					case MINUTE:
						toAdd = realPath.substring(realPathSubStart,realPathSubStart+2);
						if(!toAdd.matches("^[0-9][0-9]$") || Integer.valueOf(toAdd.startsWith("0")?toAdd.substring(1):toAdd) > 59){
							error = "A minute should have a value between 0 and 59, given ("+toAdd+")";
						}
						break;
					case MONTH:
						toAdd = realPath.substring(realPathSubStart,realPathSubStart+2);
						if(!toAdd.matches("^[0-9][0-9]$") || Integer.valueOf(toAdd.startsWith("0")?toAdd.substring(1):toAdd) > 12){
							error = "A month should have a value between 1 and 12, given ("+toAdd+")";
						}
						break;
					case YEAR:
						toAdd = realPath.substring(realPathSubStart,realPathSubStart+4);
						if(!toAdd.matches("^[1-2][0-9][0-9][0-9]$")){
							error = "A year should contain four digits, given ("+toAdd+")";
						}
						break;
					default:
						break;
					}
					buildingStr += toAdd;
				}
			}catch(Exception e){
				match = false;
			}
			if(!match){
				error = m.group(1)+" is not a template variable accepted, it should be YEAR, MONTH, DAY, HOUR or MINUTE";
			}
		}
		if(prevIndex < templatePathStr.length()){
			buildingStr += templatePathStr.substring(prevIndex);
		}

		logger.debug(buildingStr);
		logger.debug(realPath);
		if(error == null){
			if(found == 0){
				error = "No template specified.";
			}else if(!buildingStr.equals(realPath)){
				error = "The real path doesn't match the template";
			} 
		}
		
		return error;
	}
	
	protected Date getInitialInstance(String realPath, String templatePathStr) throws RemoteException{
		Pattern p = Pattern.compile("\\$\\{(.*?)\\}");
		Matcher m = p.matcher(templatePathStr);
		Date now = new Date();
		String buildingStr = "";
		String year = new SimpleDateFormat("YYYY").format(now);
		String month = new SimpleDateFormat("MM").format(now);
		String day = new SimpleDateFormat("dd").format(now);
		String hour  = "00";
		String minute  = "00";
		int prevIndex = 0;
		while(m.find()){
			buildingStr += templatePathStr.substring(prevIndex, m.start());
			prevIndex = m.end();
			try{
				TimeTemplate cur = TimeTemplate.valueOf(m.group(1)); 
				if(cur != null){
					String toAdd = "";
					int realPathSubStart = buildingStr.length();
					switch(cur){
					case DAY:
						toAdd = realPath.substring(realPathSubStart,realPathSubStart+2);
						day = toAdd;
						break;
					case HOUR:
						toAdd = realPath.substring(realPathSubStart,realPathSubStart+2);
						hour = toAdd;
						break;
					case MINUTE:
						toAdd = realPath.substring(realPathSubStart,realPathSubStart+2);
						minute = toAdd;
						break;
					case MONTH:
						toAdd = realPath.substring(realPathSubStart,realPathSubStart+2);
						month = toAdd;
						break;
					case YEAR:
						toAdd = realPath.substring(realPathSubStart,realPathSubStart+4);
						year = toAdd;
						break;
					default:
						break;
					}
					buildingStr += toAdd;
				}
			}catch(Exception e){
			}
		}
		Calendar cl = new GregorianCalendar(
				TimeZone.getTimeZone(
						WorkflowPrefManager.getProperty(WorkflowPrefManager.sys_oozie_processing_timezone)));
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
		SimpleDateFormat formatterOut = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
		formatterOut.setTimeZone(TimeZone.getTimeZone(
				WorkflowPrefManager.getProperty(WorkflowPrefManager.sys_oozie_processing_timezone)));
		try{
			Date dateStr = formatter.parse(year+"-"+month+"-"+day+"T"+hour+":"+minute);
			cl.setTime(dateStr);
		}catch(Exception e){
			logger.error(e,e);
		}
		return cl.getTime();
	}
	
	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
		super.update(interaction);
		String interId = interaction.getId();
		logger.debug("interaction : " + interId);
		if (interId.equals(inter_template)) {
			updateTemplatePath();
		}else if(interId.equals(inter_unit)){
			updateUnit();
		}
	}
	
	public FieldList getNewFields(DFEOutput in) throws RemoteException{
		FieldList ans = null;
		FieldList inF = in.getFields();
		if(!new HCatalogType().getTypeName().equals(in.getTypeName())){
			ans = inF;
		}else{
			ans = new OrderedFieldList();
			Iterator<String> it = inF.getFieldNames().iterator();
			while(it.hasNext()){
				String cur = it.next();
				ans.addField(cur, inF.getFieldType(cur));
			}
			String[] pathOut = HCatStore.getDatabaseTableAndPartition(templatePath.getValue());
			it = HCatStore.getPartitionNames(pathOut[2]).iterator();
			while(it.hasNext()){
				ans.addField(it.next(), FieldType.STRING);
			}
		}
		return ans;
	}

	@Override
	public String updateOut() throws RemoteException {
		String error = super.updateOut();
		if (error == null){

			String templatePathStr = templatePath.getValue();
			DFEOutput startInstance = output.get(out_name);


			if (output.get(out_template) == null) {
				output.put(out_template, DataOutput.getOutput(startInstance.getTypeName()));
			}else if(!output.get(out_template).getTypeName().equals(startInstance.getTypeName())){
				output.put(out_template, DataOutput.getOutput(startInstance.getTypeName()));
			}
			DFEOutput templateOut = output.get(out_template);
			templateOut.setPath(templatePathStr);
			templateOut.setFields(getNewFields(startInstance));
			templateOut.setPathType(PathType.TEMPLATE);
			templateOut.setSavingState(SavingState.RECORDED);
			templateOut.getFrequency().setFrequency(Integer.valueOf(frequency.getValue()));
			templateOut.getFrequency().setUnit(TimeTemplate.valueOf(unit.getValue()));
			templateOut.getFrequency().setInitialInstance(getInitialInstance(output.get(out_name).getPath(), templatePath.getValue()));
		}
		return error;
	}
	
	/**
	 * Get the name of the Action
	 * 
	 * @return name
	 * @throws RemoteException
	 */
	@Override
	public String getName() throws RemoteException {
		return "synchronuous_source";
	}

	// Override default static methods
	/**
	 * Get path to help
	 * 
	 * @return path
	 * @throws RemoteException
	 */
	@Override
	public String getHelp() throws RemoteException {
		String absolutePath = "";
		String helpFile = "/help/" + getName().toLowerCase() + ".html";
		String path = WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_tomcat_path, WorkflowPrefManager.defaultTomcat);
		List<String> files = listFilesRecursively(path);
		for (String file : files) {
			if (file.contains(helpFile)) {
				absolutePath = file;
				break;
			}
		}
		if(logger.isDebugEnabled()){
			String ans = "";
			if (absolutePath.contains(path)) {
				ans = absolutePath.substring(path.length());
			}
			logger.debug("Source help absPath : " + absolutePath);
			logger.debug("Source help Path : " + path);
			logger.debug("Source help ans : " + ans);
		}
		// absolutePath
		return absolutePath;
	}

	/**
	 * Get the path to the Image
	 * 
	 * @return path
	 * @throws RemoteException
	 */
	@Override
	public String getImage() throws RemoteException {
		String absolutePath = "";
		String imageFile = "/image/" + getName().toLowerCase() + ".gif";
		String path = WorkflowPrefManager
						.getSysProperty(WorkflowPrefManager.sys_tomcat_path, WorkflowPrefManager.defaultTomcat);
		List<String> files = listFilesRecursively(path);
		for (String file : files) {
			if (file.contains(imageFile)) {
				absolutePath = file;
				break;
			}
		}
		if(logger.isDebugEnabled()){
			String ans = "";
			if (absolutePath.contains(path)) {
				ans = absolutePath.substring(path.length());
			}
			logger.debug("Source image abs Path : " + absolutePath);
			logger.debug("Source image Path : " + path);
			logger.debug("Source image ans : " + ans);
		}
		return absolutePath;
	}
}
