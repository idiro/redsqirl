package com.redsqirl.workflow.server.action;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.DataOutput;
import com.redsqirl.workflow.server.DataProperty;
import com.redsqirl.workflow.server.EditorInteraction;
import com.redsqirl.workflow.server.InputInteraction;
import com.redsqirl.workflow.server.ListInteraction;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.action.superaction.FieldDefinitionTableInteraction;
import com.redsqirl.workflow.server.action.utils.ScriptTemplate;
import com.redsqirl.workflow.server.connect.hcat.HCatalogType;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.enumeration.PathType;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.interfaces.DFEPage;
import com.redsqirl.workflow.server.interfaces.PageChecker;
import com.redsqirl.workflow.server.oozie.BespokeScriptOozieAction;
import com.redsqirl.workflow.utils.LanguageManagerWF;

public abstract class AbstractScript extends AbstractMultipleSources{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6379896737651362061L;
	private static Logger logger = Logger.getLogger(Script.class);
	private static Map<String, DFELinkProperty> input = null;
	private static boolean init = false;

	public static final String key_input = "",
						  key_headerInt = "header",
						  key_fieldDefInt = "field_def",
						  key_template = "template",
						  key_oozie = "oozie",
						  key_oozie_warn = "ooziewarns",
						  key_extensionInt = "extension",
						  key_script = "script",
						  key_script_warn = "scriptwarns",
						  key_outputname = "outputname";
	
	private ListInteraction templateInt;
	private EditorInteraction oozieXmlInt;
	private ListInteraction ignoreWarningsOozieInt;
	private InputInteraction extensionInt;
	private EditorInteraction scriptInt;
	//private ListInteraction ignoreWarningsScriptInt;
	
	protected Page page0;
	protected int numberOfOutput;
	
	protected ScriptTemplate scriptTemplate = null;
	
	public AbstractScript(final int numberOfOutput) throws RemoteException {
		super(null);
		oozieAction = new BespokeScriptOozieAction(this); 
		init();
		this.numberOfOutput = numberOfOutput;
		
		page0 = addPage(LanguageManagerWF.getText("script.page0.title"),
				LanguageManagerWF.getText("script.page0.legend"), 1);
		
		templateInt = new ListInteraction(
				key_template, 
				LanguageManagerWF.getText("script.template_inter.title"), 
				LanguageManagerWF.getText("script.template_inter.legend"), 0, 0);
		updateTemplate();
		
		page0.addInteraction(templateInt);
		
		if(numberOfOutput > 1){
			for(int i=0; i < numberOfOutput;++i){
				addOutputName(page0, i);
			}
			
			page0.setChecker(new PageChecker() {
				
				/**
				 * 
				 */
				private static final long serialVersionUID = 2089334529348966249L;

				@Override
				public String check(DFEPage page) throws RemoteException {
					String error = null;
					Set<String> outputNames = new LinkedHashSet<String>();
					Iterator<DFEInteraction> it = page.getInteractions().iterator();
					while(it.hasNext()){
						DFEInteraction cur = it.next();
						if(cur.getId().startsWith(key_outputname)){
							outputNames.add(((InputInteraction)cur).getValue());
						}
					}
					if(outputNames.size() != numberOfOutput){
						error = "The output should have different names";
					}
					
					return error;
				}
			});
		}
		
		if(numberOfOutput > 0){
			for(int i=0; i < numberOfOutput;++i){
				addTypePage(i);
				addSubTypePage(i);
				addDefFieldPage(i);
			}
		}
		
		Page ooziePage = addPage(LanguageManagerWF.getText("script.page4.title"),
				LanguageManagerWF.getText("script.page4.legend"), 1);
		
		oozieXmlInt = new EditorInteraction(
				key_oozie, 
				LanguageManagerWF.getText("script.oozie.title"),
				LanguageManagerWF.getText("script.oozie.legend"),
				0, 
				0);
		
		ignoreWarningsOozieInt = new ListInteraction(
				key_oozie_warn, 
				LanguageManagerWF.getText("script.oozie_warns.title"), 
				LanguageManagerWF.getText("script.oozie_warns.legend"), 0, 1);
		
		extensionInt = new InputInteraction(key_extensionInt, 
				LanguageManagerWF.getText("script.extension.title"), 
				LanguageManagerWF.getText("script.extension.legend"), 
				0, 
				1);
		extensionInt.setRegex("(\\.[\\w]*)?");
		extensionInt.setValue(".sh");
		ooziePage.setChecker(new PageChecker() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 4519871986913624387L;

			@Override
			public String check(DFEPage page) throws RemoteException {
				return checkOozieInteraction();
			}
		});
		
		ooziePage.addInteraction(oozieXmlInt);
		ooziePage.addInteraction(ignoreWarningsOozieInt);
		ooziePage.addInteraction(extensionInt);
		
		Page scriptPage = addPage(LanguageManagerWF.getText("script.page5.title"),
				LanguageManagerWF.getText("script.page5.legend"), 1);
		
		scriptInt = new EditorInteraction(
				key_script,
				LanguageManagerWF.getText("script.script.title"),
				LanguageManagerWF.getText("script.script.legend"),
				0, 0);
		/*
		ignoreWarningsScriptInt = new ListInteraction(
				key_script_warn, 
				LanguageManagerWF.getText("script.script_warns.title"), 
				LanguageManagerWF.getText("script.script_warns.legend"), 0, 1);
		*/
		scriptPage.setChecker(new PageChecker() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = -5239247928776309756L;

			@Override
			public String check(DFEPage page) throws RemoteException {
				return checkScriptInteraction();
			}
		});
		scriptPage.addInteraction(scriptInt);
		//page5.addInteraction(ignoreWarningsScriptInt);
		
	}
	
	protected void addTypePage(int id) throws RemoteException{
		Page page = addPage(LanguageManagerWF.getText("source.page1.title"),
				LanguageManagerWF.getText("source.page1.legend"), 1);

		addTypePage(page,"");
	}
	
	/**
	 * Add a page with a list interaction to select the Data Sub Type
	 * 
	 * @throws RemoteException
	 * 
	 */
	protected void addSubTypePage(int id) throws RemoteException{
		Page page = addPage(LanguageManagerWF.getText("source.page2.title"),
				LanguageManagerWF.getText("source.page2.legend"), 1);

		addSubTypePage(page, "", "");

		
		InputInteraction headerInt = new InputInteraction(key_headerInt+id, 
				LanguageManagerWF.getText("superactioninput.header.title"), 
				LanguageManagerWF.getText("superactioninput.header.legend"), 
				0, 
				1);
		
		page.addInteraction(headerInt);
	}
	
	protected void addOutputName(Page page, int id) throws RemoteException{
		InputInteraction outputName = new InputInteraction(key_outputname+id,
				LanguageManagerWF
						.getText("source.datasubtype_interaction.title"),
				LanguageManagerWF
						.getText("source.datasubtype_interaction.legend"), 0, id);
		outputName.setRegex("[a-z]{0,15}");
		
		page.addInteraction(outputName);
	}
	
	protected void addDefFieldPage(int id) throws RemoteException{
		
		FieldDefinitionTableInteraction defFieldInt = new FieldDefinitionTableInteraction(
				key_fieldDefInt+id, 
				LanguageManagerWF.getText("superactioninput.field_def.title"), 
				LanguageManagerWF.getText("superactioninput.field_def.legend"), 
				0, 
				0);

		Page page = addPage(LanguageManagerWF.getText("superactioninput.page3.title"),
				LanguageManagerWF.getText("superactioninput.page3.legend"), 1);
		page.addInteraction(defFieldInt);
		
	}
	
	
	protected String getDefFieldUniqueId(String defFieldId) throws RemoteException{
		return defFieldId.substring(key_fieldDefInt.length());
	}
	
	protected String getHeaderUniqueId(String headerIntId) throws RemoteException{
		return headerIntId.substring(key_headerInt.length());
	}
	
	protected String checkScriptInteraction() throws RemoteException {
		String error = null;
		if(extensionInt.getValue().isEmpty()){
			if(!scriptInt.getValue().trim().isEmpty()){
				error = "Please give an extension to your script.";
			}
		}else if(scriptInt.getValue().trim().isEmpty()){
			error = "As an extension have been given, a script is expected.";
		}
		return error;
	}

	protected String checkOozieInteraction() throws RemoteException {
		String error = null;
		String oozieVal = oozieXmlInt.getValue();
		try{
			BespokeScriptOozieAction.readOozieInt(oozieVal);
		}catch(IOException e){
			error = "Fail reading the oozie xml extract: "+e.getMessage();
			logger.error(e,e);
		} catch (ParserConfigurationException e) {
			error = "Fail reading the oozie xml extract: "+e.getMessage();
			logger.error(e,e);
		} catch (SAXException e) {
			error = "Fail reading the oozie xml extract: "+e.getMessage();
			logger.error(e,e);
		}
		
		if(error == null && ignoreWarningsOozieInt.getValue().isEmpty()){
			error = "";
			Iterator<Entry<String,Entry<String,DFEOutput>>> it = getAliasesPerComponentInput().entrySet().iterator();
			while(it.hasNext()){
				Entry<String,Entry<String,DFEOutput>> inCur = it.next();
				DFEOutput dfeCur = inCur.getValue().getValue();
				if(dfeCur instanceof HCatalogType){
					if(!oozieVal.contains("TABLE_"+inCur.getKey() )){
						error = "TABLE_"+inCur.getKey()+ " variable is not used.\n";
					}
				}else{
					if(!oozieVal.contains(inCur.getKey())){
						error = inCur.getKey()+ " variable is not used.\n";
					}
				}
			}
			
			if(error.isEmpty()){
				error = null;
			}
		}
		
		return error;
	}

	private void updateTemplate() throws RemoteException {
		Set<String> posVals = ScriptTemplate.getTemplateNames();
		templateInt.setPossibleValues(posVals);
	}

	private static void init() throws RemoteException {
		if(!init){
			Map<String, DFELinkProperty> in = new LinkedHashMap<String, DFELinkProperty>();
			in.put(key_input, new DataProperty(new LinkedList<Class<? extends DFEOutput>>(), 
					0, Integer.MAX_VALUE,PathType.MATERIALIZED));
			input = in;
			init = true;
		}
	}

	@Override
	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		return input;
	}

	@Override
	public String updateOut() throws RemoteException {
		String error = null;
		BespokeScriptOozieAction oozieAction = (BespokeScriptOozieAction) getOozieAction();
		oozieAction.setFileExtension(extensionInt.getValue());
		String oozieVal = oozieXmlInt.getValue();
		try{
			oozieAction.setXmlContent(oozieVal);
		}catch(Exception e){
			error = "Fail reading the oozie xml extract: "+e.getMessage();
		}
		
		if(numberOfOutput == 1){
			updateOutput("", 
					(FieldDefinitionTableInteraction) getInteraction(key_fieldDefInt+0), 
					((InputInteraction)getInteraction(key_datasubtype+0)).getValue());
		}else if(numberOfOutput > 1){
			for(int i=0; i < numberOfOutput;++i){
				updateOutput(((InputInteraction)getInteraction(key_outputname+i)).getValue(), 
						(FieldDefinitionTableInteraction) getInteraction(key_fieldDefInt+i), 
						((InputInteraction)getInteraction(key_datasubtype+i)).getValue());
			}
		}
		
		return error;
	}
	
	public void updateOutput(String outputName, FieldDefinitionTableInteraction defFieldInt, String dataSubType) throws RemoteException{
		FieldList fl = new OrderedFieldList();
		Iterator<Map<String,String>> it = defFieldInt.getValues().iterator();
		while(it.hasNext()){
			Map<String,String> cur = it.next();
			fl.addField(cur.get(FieldDefinitionTableInteraction.table_field_title), 
					FieldType.valueOf(cur.get(FieldDefinitionTableInteraction.table_type_title)));
		}
		if(output.get(outputName) == null){
			output.put(outputName, DataOutput.getOutput(dataSubType));
		}else if(!output.get(outputName).getTypeName().equals(dataSubType)){
			output.get(outputName).clean();
			output.put(outputName, DataOutput.getOutput(dataSubType));
		}
		output.get(outputName).setFields(fl);
	}

	@Override
	public boolean writeOozieActionFiles(File[] files) throws RemoteException {
		if(files.length > 0){
			BespokeScriptOozieAction oozieAction = (BespokeScriptOozieAction) getOozieAction();
			return writeFile(oozieAction.replaceRSVariables(scriptInt.getValue()), files[0]);
		}
		return true;
	}

	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
		logger.info("update");
		String interId = interaction.getId();
		if (interId.startsWith(key_headerInt)) {
			String id = getHeaderUniqueId(interId);
			String out_name = "";
			if(numberOfOutput > 1){
				out_name = ((InputInteraction) getInteraction(key_outputname+id)).getValue();
			}
			InputInteraction headerInt = (InputInteraction) interaction;
			
			
			if(output.get(out_name) != null){
				FieldList fl = output.get(out_name).getFields();
				if(fl != null && checkIntegrationUserVariables() == null){

					logger.info("checkIntegrationUserVariables pass");

					String newVal = "";
					Iterator<String> it = fl.getFieldNames().iterator();
					while(it.hasNext()){
						String cur = it.next();
						newVal += cur+" "+fl.getFieldType(cur).toString()+", ";
					}
					headerInt.setValue(newVal.substring(0,newVal.length()-2));

					logger.info("checkIntegrationUserVariables finesh");

				}
			}else{
				String header = null;
				if(out_name.isEmpty()){
					header = getScriptTemplate().getScriptProp().getProperty(ScriptTemplate.output_header);
				}else{
					header = getScriptTemplate().getScriptProp().getProperty(ScriptTemplate.output_header+"."+out_name);
				}
				if( (header == null || header.isEmpty()) && !getDFEInput().get(key_input).isEmpty()){
					header = getDFEInput().get(key_input).get(0).getFields().toString().replaceAll("[\\{\\}:]", "");
				}
				if(header != null && !header.isEmpty()){
					headerInt.setValue(header);
				}
			}
		} else if (interId.startsWith(key_fieldDefInt)) {
			String id = getHeaderUniqueId(interId);
			InputInteraction headerInt = ((InputInteraction) getInteraction(key_headerInt+id));
			((FieldDefinitionTableInteraction) interaction).update(headerInt.getValue());
		} else if(interId.equals(key_template)){
			updateTemplate();
		} else if (interId.equals(key_oozie)) {
			if(oozieXmlInt.getValue() == null && getScriptTemplate() != null){
				BespokeScriptOozieAction oozieAction = (BespokeScriptOozieAction) getOozieAction();
				oozieXmlInt.setValue(getScriptTemplate().readOozie());
				String inVars = oozieAction.getRSVariablesAvailable().toString();
				oozieXmlInt.setTextTip(LanguageManagerWF.getText("script.oozie.texttip",new Object[]{inVars.substring(1,inVars.length()-1).replaceAll(",", "=").replaceAll(";", "\n")}));
			}
		} else if (interId.equals(key_script)) {
			String extension = extensionInt.getValue();
			if(scriptInt.getValue() == null && getScriptTemplate() != null && (extension != null && !extension.isEmpty())){
				BespokeScriptOozieAction oozieAction = (BespokeScriptOozieAction) getOozieAction();
				scriptInt.setValue(getScriptTemplate().readScript());
				String inVars = oozieAction.getRSVariablesAvailable().toString();
				scriptInt.setTextTip(LanguageManagerWF.getText("script.script.texttip",new Object[]{inVars.substring(1,inVars.length()-1).replaceAll(",", "\n")}));
			}
		} else if (interId.equals(key_extensionInt)) {
			//Cannot specify field extension
		}else{
			super.update(interaction);
		}
	}

	public ScriptTemplate getScriptTemplate() throws RemoteException{
		String templateStr = templateInt.getValue();
		if(templateStr == null || templateStr.isEmpty()){
			return null;
		}else if(scriptTemplate == null || !templateStr.equals(scriptTemplate.getName())){
			scriptTemplate = new ScriptTemplate(templateStr); 
		}
		return scriptTemplate;
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

		return absolutePath;
	}
	
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
		
		return absolutePath;
	}
}
