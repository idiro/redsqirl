package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.DataOutput;
import idiro.workflow.server.DataflowAction;
import idiro.workflow.server.ListInteraction;
import idiro.workflow.server.Page;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.datatype.HiveType;
import idiro.workflow.server.datatype.MapRedTextType;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.enumeration.SavingState;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFELinkProperty;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.server.interfaces.DFEPage;
import idiro.workflow.server.interfaces.PageChecker;
import idiro.workflow.utils.LanguageManagerWF;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Action that read a source file. For now, only Hive type is supported.
 * 
 * @author etienne
 * 
 */
public class Source extends DataflowAction {

	private static final long serialVersionUID = 7519928238030041208L;

	private static Map<String, DFELinkProperty> input = new LinkedHashMap<String, DFELinkProperty>();

	public static final String out_name = "source";

	public static final String key_datatype = "data_type";
	public static final String key_datasubtype = "data_subtype";
	public static final String key_dataset = "data_set";

	protected ListInteraction dataType;
	protected ListInteraction dataSubtype;

	public Source() throws RemoteException {
		super(null);

		Page page1 = addPage(LanguageManagerWF.getText("source.page1.title"),
				LanguageManagerWF.getText("source.page1.legend"), 1);

		dataType = new ListInteraction(key_datatype,
				LanguageManagerWF.getText("source.datatype_interaction.title"),
				LanguageManagerWF.getText("source.datatype_interaction.legend"), 
				0, 0);

		dataType.setDisplayRadioButton(true);
		List<String> posValues = new LinkedList<String>();
		posValues.add("Hive");
		posValues.add("HDFS");
		dataType.setPossibleValues(posValues);

		page1.addInteraction(dataType);

		Page page2 = addPage(LanguageManagerWF.getText("source.page2.title"),
				LanguageManagerWF.getText("source.page2.legend"), 1);

		dataSubtype = new ListInteraction(key_datasubtype,
				LanguageManagerWF.getText("source.datasubtype_interaction.title"),
				LanguageManagerWF.getText("source.datasubtype_interaction.legend"),
				0, 0);

		dataSubtype.setDisplayRadioButton(true);

		page2.addInteraction(dataSubtype);

		page2.setChecker(new PageChecker() {

			@Override
			public String check(DFEPage page) throws RemoteException {
				String error = dataSubtype.check();
				if(error == null){
					try {
						
						//Get the subtype
						String subtype = dataSubtype.getValue();
						logger.info("output type : " + subtype);

						logger.info("Getting CheckDirectory output type ");
						DFEOutput outNew = DataOutput.getOutput(subtype);

						//Set the instance as output if necessary
						if (outNew != null) {
							if (output.get(out_name) == null ||
									!output.get(out_name).getTypeName().equalsIgnoreCase(subtype)) {
								logger.info("output set");
								output.put(out_name, (DFEOutput) outNew);
								//Set the Output as RECORDED ALWAYS
								output.get(out_name).setSavingState(
										SavingState.RECORDED);
							}
						}

					} catch (Exception e) {
						error = LanguageManagerWF.getText("source.outputnull",new Object[]{e.getMessage()});
					}
				}
				return error;
			}

		});

		Page page3 = addPage(
				LanguageManagerWF.getText("source.page3.title"), 
				LanguageManagerWF.getText("source.page3.legend"),
				1);

		DFEInteraction browse = new UserInteraction(key_dataset,
				LanguageManagerWF.getText("source.browse_interaction.title"),
				LanguageManagerWF.getText("source.browse_interaction.legend"),
				DisplayType.browser, 0, 0);

		page3.addInteraction(browse);

		page3.setChecker(new PageChecker() {

			@Override
			public String check(DFEPage page) throws RemoteException {

				String error = null;
				DataOutput out = null;

				try{
					out = (DataOutput) output.get(out_name);
				}catch(Exception e){
					error = LanguageManagerWF.getText("source.outputchecknull");
				}
				try {
					logger.info("tree is : "+((TreeNonUnique<String>)getInteraction(key_dataset).getTree()).toString());

					//Properties
					Map<String,String> props = new LinkedHashMap<String,String>();
					if (error == null) {
						try{
							Iterator<Tree<String>> itProp = getInteraction(key_dataset).getTree()
									.getFirstChild("browse").
									getFirstChild("output")
									.getFirstChild("property").getSubTreeList().iterator();

							while(itProp.hasNext()){
								Tree<String> prop = itProp.next();
								String name = prop.getHead();
								String value = prop.getFirstChild().getHead();

								logger.info("out addProperty " + name + " " + value);

								props.put(name, value);
							}
						}catch(Exception e){
							logger.debug("No properties");
						}
					}


					//Features
					FeatureList outF = new OrderedFeatureList();
					if(error == null){
						try{
							List<Tree<String>> features =  getInteraction(key_dataset)
									.getTree().getFirstChild("browse").getFirstChild("output")
									.getChildren("feature");
							if(features == null || features.isEmpty()){
								logger.warn("The list of features cannot be null or empty, could be calculated automatically from the path");
							}else{

								for (Iterator<Tree<String>> iterator =features.iterator(); iterator.hasNext();) {
									Tree<String> cur = iterator.next();

									String name = cur.getFirstChild("name").getFirstChild()
											.getHead();
									String type = cur.getFirstChild("type").getFirstChild()
											.getHead();

									logger.info("updateOut name " + name);
									logger.info("updateOut type " + type);

									try {
										outF.addFeature(name, FeatureType.valueOf(type));
									} catch (Exception e) {
										error = "The type " + type + " does not exist";
									}

								} 
							}
						}catch(Exception e){
							error = LanguageManagerWF.getText("source.treeerror");
						}
					}

					//Path
					String path = null;
					if(error == null){
						try{
							path = getInteraction(key_dataset).getTree()
									.getFirstChild("browse").getFirstChild("output")
									.getFirstChild("path").getFirstChild().getHead();

							if (path.isEmpty()) {
								error = LanguageManagerWF.getText("source.pathempty");
							}
						}catch(Exception e){
							error = LanguageManagerWF.getText("source.setpatherror",new Object[]{e.getMessage()});
						}
					}



					if(error == null){
						if(!out.compare(path, outF, props)){
							logger.info("The output need to be changed in source "+componentId);
							try{
								out.setPath(null);
								out.setFeatures(null);
								out.removeAllProperties();
							}catch(Exception e){}
							Iterator<String> propsIt = props.keySet().iterator();
							while(propsIt.hasNext()){
								String cur = propsIt.next();
								out.addProperty(cur, props.get(cur));
							}

							//Update the feature list only if it looks good
							out.setFeatures(outF);

							logger.info("Setpath : " + path);
							out.setPath(path);

						}
						getInteraction(key_dataset).getTree().removeAllChildren();
						getInteraction(key_dataset).getTree().add(out.getTree());
					}


					//Check path
					if(error == null){
						try{
							if(!out.isPathExists()){
								error = LanguageManagerWF.getText("source.pathnotexist");
							}else if(out.isPathValid() != null){
								error = LanguageManagerWF.getText("source.pathinvalid",new Object[]{out.isPathValid()});
							}
						}catch(Exception e){
							error = LanguageManagerWF.getText("source.pathexceptions",new Object[]{e.getMessage()});
							logger.error(error);
						}

					}
				} catch (Exception e) {
					error = LanguageManagerWF.getText("source.exception",new Object[]{e.getMessage()});
				}


				logger.info("checkpage3 "+error);

				return error;
			}

		});
	}

	@Override
	public String getName() throws RemoteException {
		return "Source";
	}

	@Override
	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		return input;
	}

	// Override default static methods
	@Override
	public String getHelp() throws RemoteException {
		return "../help/" + getName().toLowerCase() + ".html";
	}

	@Override
	public String getImage() throws RemoteException {
		return "../image/" + getName().toLowerCase() + ".gif";
	}

	@Override
	public void update(DFEInteraction interaction) throws RemoteException {

		logger.info("updateinteraction Source ");
		String interId = interaction.getId(); 
		logger.info("interaction : "+interId);
		if (interId.equals(key_datatype)) {
			updateDataType(interaction.getTree());
		} else if (interId.equals(key_datasubtype)) {
			updateDataSubType(interaction.getTree());
		} else {
			updateDataSet(interaction.getTree());
		}
	}

	public void updateDataType(Tree<String> treeDatatype)
			throws RemoteException {
	}

	public void updateDataSubType(Tree<String> treeDatasubtype)
			throws RemoteException {
		logger.info("updating data subtype");
		String type = dataType.getValue();
		logger.info("data type : "+type);
		if (type != null) {
			String setValue = null;
			List<String> posValues = new LinkedList<String>();
			List<String> dataOutputClassName = DataOutput.getAllClassDataOutput();
			for (String className : dataOutputClassName) {
				logger.debug(className);
				DataOutput wa = null;
				try {
					wa = (DataOutput) Class.forName(className).newInstance();
				} catch (Exception e) {
					e.printStackTrace();
				}
				logger.info("wa type : "+ wa.getTypeName());
				if (wa.getBrowser() != null && wa.getBrowser().toString().equalsIgnoreCase(type)) {
					posValues.add(wa.getTypeName());
					if ((wa.getTypeName().equalsIgnoreCase(
							(new HiveType()).getTypeName()) || wa.getTypeName()
							.equalsIgnoreCase(
									(new MapRedTextType()).getTypeName()))
									&& dataSubtype.getValue() == null) {
						setValue = wa.getTypeName();
					}
				}
			}
			logger.debug("set possibilities...");
			logger.info(" is "+posValues.toString());
			dataSubtype.setPossibleValues(posValues);
			if(setValue != null){
				logger.debug("set value...");
				dataSubtype.setValue(setValue);
			}
		}else{
			logger.error("No type specified");
		}
	}

	public void updateDataSet(Tree<String> treeDataset) throws RemoteException {

		String newType = dataType.getValue();
		logger.info("type : "+ newType);
		String newSubtype = dataSubtype.getValue();
		logger.info("subtype : "+ newSubtype);

		if (treeDataset.getSubTreeList().isEmpty()) {
			treeDataset.add("browse").add("output");
			treeDataset.getFirstChild("browse").add("subtype").add(newSubtype);
			treeDataset.getFirstChild("browse").add("type").add(newType);
		} else {
			Tree<String> oldType = treeDataset.getFirstChild("browse")
					.getFirstChild("type").getFirstChild();

			if (oldType != null && !oldType.getHead().equals(newType)) {
				treeDataset.getFirstChild("browse").remove("type");
				treeDataset.getFirstChild("browse").remove("output");
				treeDataset.getFirstChild("browse").add("output");
				treeDataset.getFirstChild("browse").add("type").add(newType);
				treeDataset.getFirstChild("browse").add("subtype")
				.add(newSubtype);
			}
		}
	}

	@Override
	public String updateOut() throws RemoteException {
		String error = checkIntegrationUserVariables();
		return error;
	}

	@Override
	public boolean writeOozieActionFiles(File[] files) throws RemoteException {
		return false;
	}
}
