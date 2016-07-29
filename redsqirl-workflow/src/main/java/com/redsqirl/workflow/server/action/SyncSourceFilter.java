package com.redsqirl.workflow.server.action;

import java.io.File;
import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.DataProperty;
import com.redsqirl.workflow.server.DataflowAction;
import com.redsqirl.workflow.server.InputInteraction;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.connect.hcat.HCatalogType;
import com.redsqirl.workflow.server.datatype.MapRedCompressedType;
import com.redsqirl.workflow.server.enumeration.PathType;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.utils.LanguageManagerWF;

public class SyncSourceFilter extends DataflowAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6765601797425986642L;
	private static Logger logger = Logger.getLogger(SyncSourceFilter.class);
	private static Map<String, DFELinkProperty> input;

	public static final String key_input = "in",key_output="";
	
	protected InputInteraction nbUsedPath;
	
	public SyncSourceFilter(
			) throws RemoteException {
		super(null);
		init();
		
		Page page1 = addPage(LanguageManagerWF.getText("sync_source_filter.page1.title"),
				LanguageManagerWF.getText("sync_source_filter.page1.legend"), 1);
		
		//Number of dataset to use
		nbUsedPath = new InputInteraction(
				"nb_template_path_used", 
				LanguageManagerWF.getText("sync_source_filter.nb_template_path_used.title"),
				LanguageManagerWF.getText("sync_source_filter.nb_template_path_used.legend")
				, 0, 0);
		nbUsedPath.setRegex("^[1-9][0-9]*$");
		nbUsedPath.setValue("1");
		
		page1.addInteraction(nbUsedPath);
	}
	
	public int getNbPath(){
		int nbPath = 1;
		try{
			nbPath = Integer.valueOf(nbUsedPath.getValue());
		}catch(Exception e){
			logger.warn(e,e);
		}
		return nbPath;
	}
	
	/**
	 * Initialise the element
	 * @param nbInMin
	 * @param nbInMax
	 * @throws RemoteException
	 */
	protected static void init() throws RemoteException{
		if(input == null){
			Map<String, DFELinkProperty> in = new LinkedHashMap<String, DFELinkProperty>();
			List<Class< ? extends DFEOutput>> l = new LinkedList<Class< ? extends DFEOutput>>();
			l.add(MapRedCompressedType.class);
			l.add(HCatalogType.class);
			in.put(key_input, new DataProperty(l, 1, 1,PathType.TEMPLATE));
			input = in;
		}
	}

	@Override
	public String getName() throws RemoteException {
		return "synchronuous source filter";
	}

	@Override
	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		return input;
	}

	public FieldList getNewFields() throws RemoteException{
		return getDFEInput().get(key_input).get(0).getFields();
	}
	
	@Override
	public String updateOut() throws RemoteException {
		String error = null;
		FieldList new_field = getNewFields();
		logger.info("Fields "+new_field.getFieldNames());

		if(output.get(key_output) == null){
			output.put(key_output, new HCatalogType());
			output.get(key_output).setPathType(PathType.MATERIALIZED);
		}
		

		output.get(key_output).setFields(new_field);
		output.get(key_output).setNumberMaterializedPath(getNbPath());		
			
		return error;
	}

	@Override
	public boolean writeOozieActionFiles(File[] files) throws RemoteException {
		return false;
	}

	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
	}

}
