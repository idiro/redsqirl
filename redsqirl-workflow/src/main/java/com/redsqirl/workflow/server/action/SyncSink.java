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
import com.redsqirl.workflow.server.datatype.MapRedTextType;
import com.redsqirl.workflow.server.enumeration.PathType;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.interfaces.OozieAction;
import com.redsqirl.workflow.utils.LanguageManagerWF;

public class SyncSink extends DataflowAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6765601797425986642L;
	private static Logger logger = Logger.getLogger(SyncSink.class);
	private static Map<String, DFELinkProperty> input;

	public static final String key_input = "in",key_output="";
	
	protected InputInteraction templatePath;
	
	public SyncSink(OozieAction oozieAction) throws RemoteException {
		super(oozieAction);
		init();
		
		Page page1 = addPage(LanguageManagerWF.getText("sync_sink.page1.title"),
				LanguageManagerWF.getText("sync_sink.page1.legend"), 1);
		
		//Data Set Type
		templatePath = new InputInteraction(
				"template_path", 
				LanguageManagerWF.getText("sync_sink.template_path.title"),
				LanguageManagerWF.getText("sync_sink.template_path.legend")
				, 0, 0);
		page1.addInteraction(templatePath);
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
			in.put(key_input, new DataProperty(l, 1, 1));
			input = in;
		}
	}

	@Override
	public String getName() throws RemoteException {
		return "synchronuous sink";
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
		DFEOutput out = output.get(key_output);
		logger.info("Fields "+new_field.getFieldNames());

		if(output.get(key_output) == null){
			output.put(key_output, new HCatalogType());
		}

		if(out == null){
			output.put(key_output, new MapRedTextType());
			out = output.get(key_output);
		}

		output.get(key_output).setFields(new_field);
		output.get(key_output).setPathType(PathType.TEMPLATE);
			
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
