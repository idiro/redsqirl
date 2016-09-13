package com.redsqirl.workflow.server.action;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.interfaces.DFEPage;
import com.redsqirl.workflow.server.interfaces.PageChecker;
import com.redsqirl.workflow.utils.LanguageManagerWF;

public class SourcePageChecker implements PageChecker{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5337261684164431981L;
	private static Logger logger = Logger.getLogger(SourcePageChecker.class);

	private DataflowAction act;
	private String outputName;
	private BrowserInteraction browserInter;
	
	public SourcePageChecker(DataflowAction act, String outputName, BrowserInteraction browserInter){
		this.act = act;
		this.outputName = outputName;
		this.browserInter = browserInter;
	}
	
	@Override
	public String check(DFEPage page) throws RemoteException {
		logger.debug("check page 3 " + page.getTitle());
		String error = null;
		DataOutput out = null;

		try {
			out = (DataOutput) act.getDFEOutput().get(outputName);
		} catch (Exception e) {
			error = LanguageManagerWF.getText("source.outputchecknull");
		}
		logger.debug("got type");
		try {
			if(logger.isDebugEnabled()){
				logger.debug("tree is : "
					+ ((TreeNonUnique<String>) browserInter.getTree()).toString());
			}
			// Properties
			Map<String, String> props = new LinkedHashMap<String, String>();
			if (error == null) {
				try {
					Iterator<Tree<String>> itProp = browserInter.getTree().getFirstChild("browse")
						.getFirstChild("output").getFirstChild("property").getSubTreeList().iterator();

					logger.debug("property list size : "	+ browserInter.getTree().getFirstChild("browse")
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
							+ browserInter.getTree());
					}
					List<Tree<String>> fields = browserInter.getTree()
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
					path = browserInter.getTree()
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
					name = browserInter.getTree()
							.getFirstChild("browse")
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
							+ act.getComponentId());
					try {
						out.setPath(null);
						out.setFields(null);
						out.removeAllProperties();
						out.clearCache();
					} catch (Exception e) {
					}
					Iterator<String> propsIt = props.keySet()
							.iterator();
					while (propsIt.hasNext()) {
						String cur = propsIt.next();
						out.addProperty(cur, props.get(cur));
					}

					// Update the field list only if it looks good
					try {
						out.setFields(outF);
						logger.debug(out.getFields().getFieldNames());
						logger.debug("Setpath : " + path);
						out.setPath(path);
						logger.debug(out.getFields().getFieldNames());
					} catch (Exception e) {
						error = e.getMessage();
					}
				}
				
				boolean updatable = false;
				if(browserInter.getTree()
						.getFirstChild("browse")
						.getFirstChild("updatable") != null){
					updatable = Boolean.valueOf(
							browserInter.getTree()
							.getFirstChild("browse")
							.getFirstChild("updatable").getFirstChild().getHead());
				}
				browserInter.getTree()
						.removeAllChildren();
				browserInter.getTree()
						.add(out.getTree());
				browserInter.getTree()
						.getFirstChild("browse")
						.add("name")
						.add(name);
				browserInter.getTree()
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

}
