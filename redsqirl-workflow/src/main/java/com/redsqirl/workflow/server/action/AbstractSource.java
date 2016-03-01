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


import java.io.File;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.utils.Tree;
import com.redsqirl.utils.TreeNonUnique;
import com.redsqirl.workflow.server.BrowserInteraction;
import com.redsqirl.workflow.server.DataOutput;
import com.redsqirl.workflow.server.DataflowAction;
import com.redsqirl.workflow.server.ListInteraction;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.connect.WorkflowInterface;
import com.redsqirl.workflow.server.datatype.MapRedTextType;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.interfaces.DFEPage;
import com.redsqirl.workflow.server.interfaces.OozieAction;
import com.redsqirl.workflow.server.interfaces.PageChecker;
import com.redsqirl.workflow.utils.LanguageManagerWF;

/**
 * Abstract action that read a source file. For now, Hive and HDFS types are supported.
 * This class has methods to create pages to select the Type (Hive or HDFS), Subtype and
 * the path to the file.
 * 
 * @author etienne
 * 
 */
public abstract class AbstractSource extends DataflowAction {

	private static final long serialVersionUID = 7519928238030041208L;
	
	private static Logger logger = Logger.getLogger(AbstractSource.class);
	
	/**
	 * Map of inputs
	 */
	protected static Map<String, DFELinkProperty> input = new LinkedHashMap<String, DFELinkProperty>();
	/**
	 * Output name
	 */
	public static final String out_name = "";
	/**
	 * datatype key
	 */
	public static final String key_datatype = "data_type";
	/**
	 * datasubtype key
	 */
	public static final String key_datasubtype = "data_subtype";
	/**
	 * dataset key
	 */
	public static final String key_dataset = "data_set";
	/**
	 * Interaction for the DataType
	 */
	protected ListInteraction dataType;
	/**
	 * Interaction for the DataSubType
	 */
	protected ListInteraction dataSubtype;
	/**
	 * Interaction for the Dataset
	 */
	protected BrowserInteraction browser;
	
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

		initializeDataTypeInteraction();

		page1.addInteraction(dataType);
	}
	
	/**
	 * Create the Data Type interaction and populate it with the Hive and HDFS 
	 * options.
	 * 
	 * @throws RemoteException
	 * 
	 */
	protected void initializeDataTypeInteraction() throws RemoteException{
		dataType = new ListInteraction(
				key_datatype,
				LanguageManagerWF.getText("source.datatype_interaction.title"),
				LanguageManagerWF.getText("source.datatype_interaction.legend"),
				0, 0);
		dataType.setReplaceDisable(true);
		dataType.setDisplayRadioButton(true);
		List<String> posValues = new LinkedList<String>();
		posValues.addAll(WorkflowInterface.getInstance().getBrowsersName());
		dataType.setPossibleValues(posValues);
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

		initializeDataSubtypeInteraction();

		page2.addInteraction(dataSubtype);

		
		page2.setChecker(new PageChecker(){

			/**
			 * 
			 */
			private static final long serialVersionUID = 3411961312502537625L;

			@Override
			public String check(DFEPage page) throws RemoteException {
				return checkSubType();
			}

		});
	}
	
	
	/**
	 * Create the Data Sub Type interaction.
	 * 
	 * @throws RemoteException
	 * 
	 */
	protected void initializeDataSubtypeInteraction() throws RemoteException{
		dataSubtype = new ListInteraction(key_datasubtype,
				LanguageManagerWF
						.getText("source.datasubtype_interaction.title"),
				LanguageManagerWF
						.getText("source.datasubtype_interaction.legend"), 0, 0);
		dataSubtype.setReplaceDisable(true);
		dataSubtype.setDisplayRadioButton(true);
	}
	
	
	/**
	 * Check the Sub Type selected.
	 * 
	 * @throws RemoteException
	 * @return Error Message
	 */
	protected String checkSubType() throws RemoteException{
		String error = dataSubtype.check();
		if (error == null) {
			try {

				// Get the subtype
				String subtype = dataSubtype.getValue();
				logger.debug("output type : " + subtype);

				logger.debug("Getting CheckDirectory output type ");
				DFEOutput outNew = DataOutput.getOutput(subtype);

				// Set the instance as output if necessary
				if (outNew != null) {
					if (output.get(out_name) == null
							|| !output.get(out_name).getTypeName()
									.equalsIgnoreCase(subtype)) {
						logger.debug("output set");
						output.put(out_name, (DFEOutput) outNew);
						// Set the Output as RECORDED ALWAYS
						output.get(out_name).setSavingState(
								SavingState.RECORDED);
					}
				}

			} catch (Exception e) {
				error = LanguageManagerWF.getText("source.outputnull",
						new Object[] { e.getMessage() });
			}
		}
		return error;
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

		browser = new BrowserInteraction(key_dataset,
				LanguageManagerWF.getText("source.browse_interaction.title"),
				LanguageManagerWF.getText("source.browse_interaction.legend"),
				0, 0);
		browser.setReplaceDisable(true);
		
		page3.addInteraction(browser);

		page3.setChecker(new PageChecker() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1718709208939991206L;

			@Override
			public String check(DFEPage page) throws RemoteException {
				logger.debug("check page 3 " + page.getTitle());
				String error = null;
				DataOutput out = null;

				try {
					out = (DataOutput) output.get(out_name);
				} catch (Exception e) {
					error = LanguageManagerWF.getText("source.outputchecknull");
				}
				logger.debug("got type");
				try {
					if(logger.isDebugEnabled()){
						logger.debug("tree is : "
							+ ((TreeNonUnique<String>) getInteraction(
									key_dataset).getTree()).toString());
					}
					// Properties
					Map<String, String> props = new LinkedHashMap<String, String>();
					if (error == null) {
						try {
							Iterator<Tree<String>> itProp = getInteraction(key_dataset).getTree().getFirstChild("browse")
								.getFirstChild("output").getFirstChild("property").getSubTreeList().iterator();

							logger.debug("property list size : "	+ getInteraction(key_dataset).getTree().getFirstChild("browse")
									.getFirstChild("output").getFirstChild("property").getSubTreeList().size());

							while (itProp.hasNext()) {
								Tree<String> prop = itProp.next();
								String name = prop.getHead();
								String value = prop.getFirstChild().getHead();

								logger.debug("out addProperty " + name + " "	+ value);

								props.put(name, value);
							}
						} catch (Exception e) {
							logger.debug("No properties");
						}
					}

					// Fields
					FieldList outF = new OrderedFieldList();
					if (error == null) {
						try {
							if(logger.isDebugEnabled()){
								logger.debug("tree is "
									+ getInteraction(key_dataset).getTree());
							}
							List<Tree<String>> fields = getInteraction(
									key_dataset).getTree()
									.getFirstChild("browse")
									.getFirstChild("output")
									.getChildren("field");
							if (fields == null || fields.isEmpty()) {
								logger.warn("The list of fields cannot be null or empty, could be calculated automatically from the path");
							} else {

								for (Iterator<Tree<String>> iterator = fields
										.iterator(); iterator.hasNext();) {
									Tree<String> cur = iterator.next();

									String name = cur.getFirstChild("name")
											.getFirstChild().getHead();
									String type = cur.getFirstChild("type")
											.getFirstChild().getHead();
									
									if(logger.isDebugEnabled()){
										logger.debug("updateOut name " + name);
										logger.debug("updateOut type " + type);
									}
									
									try {
										outF.addField(name,
												FieldType.valueOf(type));
									} catch (Exception e) {
										error = "The type " + type
												+ " does not exist";
										logger.debug(error);
									}

								}
							}
						} catch (Exception e) {
							error = LanguageManagerWF
									.getText("source.treeerror");
						}
					}

					// Path
					String path = null;
					if (error == null) {
						try {
							path = getInteraction(key_dataset).getTree()
									.getFirstChild("browse")
									.getFirstChild("output")
									.getFirstChild("path").getFirstChild()
									.getHead();

							if (path.isEmpty()) {
								error = LanguageManagerWF
										.getText("source.pathempty");
							}
						} catch (Exception e) {
							error = LanguageManagerWF.getText(
									"source.setpatherror",
									new Object[] { e.getMessage() });
						}
					}
					
					// Name
					String name = null;
					if (error == null) {
						try {
							name = getInteraction(key_dataset).getTree()
									.getFirstChild("browse")
									.getFirstChild("output")
									.getFirstChild("name").getFirstChild()
									.getHead();
						} catch (Exception e) {
							logger.warn(LanguageManagerWF.getText(
									"source.name_null"));
						}
					}

					if (error == null) {
						boolean ok = false;
						try {
							ok = out.compare(path, outF, props);
						} catch (Exception e) {
							ok = false;
						}
						if (!ok) {
							logger.debug("The output need to be changed in source "
									+ componentId);
							try {
								out.setPath(null);
								out.setFields(null);
								out.removeAllProperties();
							} catch (Exception e) {
							}
							Iterator<String> propsIt = props.keySet()
									.iterator();
							while (propsIt.hasNext()) {
								String cur = propsIt.next();
								out.addProperty(cur, props.get(cur));
							}

							// Update the field list only if it looks good
							out.setFields(outF);
							logger.debug(out.getFields().getFieldNames());
							logger.debug("Setpath : " + path);
							out.setPath(path);
							logger.debug(out.getFields().getFieldNames());
						}
						
						boolean updatable = false;
						if(getInteraction(key_dataset).getTree()
								.getFirstChild("browse")
								.getFirstChild("updatable") != null){
							updatable = Boolean.valueOf(
									getInteraction(key_dataset).getTree()
									.getFirstChild("browse")
									.getFirstChild("updatable").getFirstChild().getHead());
						}
						getInteraction(key_dataset).getTree()
								.removeAllChildren();
						getInteraction(key_dataset).getTree()
								.add(out.getTree());
						
						getInteraction(key_dataset).getTree()
								.getFirstChild("browse")
								.getFirstChild("output")
								.add("name")
								.add(name);
						getInteraction(key_dataset).getTree()
								.getFirstChild("browse")
								.add("updatable").add(Boolean.toString(updatable));
					}

					// Check path
					if (error == null) {
						try {
							if (!out.isPathExist()) {
								error = LanguageManagerWF
										.getText("source.pathnotexist");
							} else{
								String msg = out.isPathValid();
								logger.debug("isPathExists " + msg);
								if (msg != null) {
									error = LanguageManagerWF.getText(
											"source.pathinvalid",
											new Object[] { msg });
								}
							}
						} catch (Exception e) {
							error = LanguageManagerWF.getText(
									"source.pathexceptions",
									new Object[] { e.getMessage() });
							logger.error(error,e);
						}

					}
				} catch (Exception e) {
					logger.error("Exception in source.",e);
					error = LanguageManagerWF.getText("source.exception",
							new Object[] { e.getMessage() });
				}

				logger.debug("checkpage3 " + error);

				return error;
			}

		});
	}

	/**
	 * Get the Map of Inputs
	 * 
	 * @return Map of Inputs
	 * @throws RemoteException
	 * 
	 */
	@Override
	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		return input;
	}


	/**
	 * Update the Interaction provided
	 * 
	 * @param interaction
	 * @throws RemoteException
	 */
	@Override
	public void update(DFEInteraction interaction) throws RemoteException {

		logger.debug("updateinteraction Source ");
		String interId = interaction.getId();
		logger.debug("interaction : " + interId);
		if (interId.equals(key_datatype)) {
			updateDataType(interaction.getTree());
		} else if (interId.equals(key_datasubtype)) {
			updateDataSubType(interaction.getTree());
		} else if (interId.equals(key_dataset)){
			browser.update(dataType.getValue(), dataSubtype.getValue());
		}
	}

	public void updateDataType(Tree<String> treeDatatype)
			throws RemoteException {
	}

	/**
	 * Update the DataSubType Interaction
	 * 
	 * @param treeDatasubtype
	 * @throws RemoteException
	 */
	public void updateDataSubType(Tree<String> treeDatasubtype)
			throws RemoteException {
		logger.debug("updating data subtype");
		String type = dataType.getValue();
		logger.debug("data type : " + type);
		if (type != null) {
			String setValue = null;
			List<String> posValues = new LinkedList<String>();
			List<String> dataOutputClassName = DataOutput
					.getAllClassDataOutput();
			for (String className : dataOutputClassName) {
				logger.debug(className);
				DataOutput wa = null;
				try {
					wa = (DataOutput) Class.forName(className).newInstance();
				} catch (Exception e) {
					e.printStackTrace();
				}
				logger.debug("class : " + wa.getClass().getCanonicalName());
				logger.debug("wa type : " + wa.getTypeName());
				if (wa.getBrowserName() != null
						&& wa.getBrowserName().toString().equalsIgnoreCase(type)) {
					posValues.add(wa.getTypeName());
					if(wa.getTypeName()
							.equalsIgnoreCase(
									(new MapRedTextType()).getTypeName())
							&& dataSubtype.getValue() == null) {
						setValue = wa.getTypeName();
					}
				}
			}
			logger.debug("set possibilities...");
			logger.debug(" is " + posValues.toString());
			dataSubtype.setPossibleValues(posValues);
			if (setValue != null) {
				logger.debug("set value...");
				dataSubtype.setValue(setValue);
			}
		} else {
			logger.error("No type specified");
		}
	}

	/**
	 * Update the output
	 * 
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String updateOut() throws RemoteException {
		return null;
	}

	/**
	 * Not Supported
	 */
	@Override
	public boolean writeOozieActionFiles(File[] files) throws RemoteException {
		return false;
	}
	
	protected Page getSourcePage(){
		return page3;
	}
}
