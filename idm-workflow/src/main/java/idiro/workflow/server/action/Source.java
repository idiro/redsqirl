package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.utils.Tree;
import idiro.workflow.server.DataOutput;
import idiro.workflow.server.DataflowAction;
import idiro.workflow.server.Page;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.WorkflowPrefManager;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.server.connect.HiveInterface;
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

import java.io.File;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hdfs.web.resources.DoAsParam;
import org.apache.log4j.Logger;

/**
 * Action that read a source file.
 * For now, only Hive type is supported.
 * 
 * @author etienne
 *
 */
public class Source extends DataflowAction{


	private static final long serialVersionUID = 7519928238030041208L;

	private static Map<String,DFELinkProperty> input = new LinkedHashMap<String,DFELinkProperty>();

	private Map<String,DFEOutput> output = new LinkedHashMap<String,DFEOutput>();

	public static final String out_name = "source";

	public static final String key_datatype = "Data_type"; 
	public static final String key_datasubtype = "Data_subtype"; 
	public static final String key_dataset = "Data_set"; 

	public Source() throws RemoteException {
		super(null);

		Page page1 = addPage("Source: Data type",
				"Choose the data type that you would like to load",
				1);

		DFEInteraction dataType = new UserInteraction(
				key_datatype,
				"Please specify a data type",
				DisplayType.list,
				0,
				0); 

		page1.addInteraction(dataType);


		page1.setChecker(new PageChecker(){

			@Override
			public String check(DFEPage page) throws RemoteException {
				String error = null;
				logger = Logger.getRootLogger();
				try{
					if( getInteraction(key_datatype).getTree()
							.getFirstChild("list").getFirstChild("output")
							.getFirstChild().getHead().isEmpty()
							){
						error = "Data type cannot be empty";
					}
				}catch(Exception e){
					error = "Data type cannot be empty";
				}
				return error;
			}

		});


		Page page2 = addPage("Source: Data sub-type",
				"Choose the data sub-type that you would like to load",
				1);


		DFEInteraction dataSubtype = new UserInteraction(
				key_datasubtype,
				"Please specify a data subtype",
				DisplayType.list,
				0,
				0); 

		page2.addInteraction(dataSubtype);

		page2.setChecker(new PageChecker(){

			@Override
			public String check(DFEPage page) throws RemoteException {
				String error = null;
				try{

					if( getInteraction(key_datatype).getTree()
							.getFirstChild("list").getFirstChild("output")
							.getFirstChild().getHead().isEmpty()
							){
						error = "Error : Data type cannot be empty";
					}


					String type = getInteraction(key_datatype).getTree()
							.getFirstChild("list").getFirstChild("output")
							.getFirstChild().getHead();
					if(type.equalsIgnoreCase("hive")){

						if( output.get(out_name) == null ){

							output.put(out_name, new HiveType());

						}


					}else if(type.equalsIgnoreCase("hdfs")){
						logger.info("Getting output type ");

						String subtype = getInteraction(key_datasubtype).getTree()
								.getFirstChild("list").getFirstChild("output")
								.getFirstChild().getHead();


						Iterator<String> dataOutputClassName = 
								WorkflowPrefManager.getInstance().getNonAbstractClassesFromSuperClass(
										DataOutput.class.getCanonicalName()).iterator();

						logger.info("output type : "+subtype);
						DFEOutput outNew = null;
						while (dataOutputClassName.hasNext()){
							String className = dataOutputClassName.next();
							outNew = (DFEOutput) Class.forName(className).newInstance();
							if(outNew.getTypeName().equalsIgnoreCase(subtype)){
								break;

							}else{
								outNew = null;
							}

						}
						if(outNew !=null){
							if( output.get(out_name) == null ){
								output.put(out_name, (DFEOutput) outNew);
							}
							logger.info("output set");
						}else{
							error = "The user have to make  choice";
							logger.error(error);
						}
						if(error == null){


							String delimiter = "\001";
							try{
								delimiter = getInteraction(key_dataset).getTree()
										.getFirstChild("browse").
										getFirstChild("output")
										.getFirstChild("property").
										getFirstChild(MapRedTextType.key_delimiter).
										getFirstChild().getHead();
							}catch(Exception e){
								logger.debug("Delimiter not set, using default delimiter");
							}

							output.get(out_name).addProperty(MapRedTextType.key_delimiter, delimiter);
						}
					}

					if(output.get(out_name) != null){
						output.get(out_name).setSavingState(SavingState.RECORDED);
					}


				}catch(Exception e){
					error = "Exception : Data type cannot be empty " + e.getMessage();
				}
				return error;
			}

		});


		Page page3 = addPage("Source: Browser",
				"Pick a data set",
				1);

		DFEInteraction browse = new UserInteraction(
				key_dataset,
				"Please specify a data set",
				DisplayType.browser,
				0,
				0);

		page3.addInteraction(browse);

		page3.setChecker(new PageChecker(){

			@Override
			public String check(DFEPage page) throws RemoteException {
				String error = null;
				try{
					String path = getInteraction(key_dataset).getTree()
							.getFirstChild("browse")
							.getFirstChild("output")
							.getFirstChild("path")
							.getFirstChild().getHead(); 
					if( path.isEmpty() ){
						error = "Data set cannot be empty";
					}else{
						String type = getInteraction(key_datatype).getTree()
								.getFirstChild("list").getFirstChild("output")
								.getFirstChild().getHead();

						boolean existAndValid = true;
						if(type.equalsIgnoreCase("Hive")){
							/*
							HiveInterface hInt = new HiveInterface(); 
							existAndValid = hInt.exists(path);
							if(exist){
								String[] desc = 
										hInt.getDescription(
												hInt.getTableAndPartitions(path)[0]
												).split(";");
								for(int i = 0; i < desc.length && error == null; ++i){
									String nameF = desc[i].split(",")[0];
									String typeF = desc[i].split(",")[1];

									Iterator<Tree<String>> it = getInteraction(key_dataset).getTree()
											.getFirstChild("browse")
											.getFirstChild("output")
											.getChildren("feature").iterator();
									boolean found = false;
									while(it.hasNext() && !found){
										Tree<String> cur = it.next();
										found = cur.getFirstChild("name").getFirstChild().getHead()
												.equalsIgnoreCase(nameF) &&
												cur.getFirstChild("type").getFirstChild().getHead()
												.equalsIgnoreCase(typeF);
									}
									if(!found){
										error = "The output does not contain the feature "+desc[i];
									}

								}
							}*/
						}

						else if(type.equalsIgnoreCase("Hdfs")){
							try{
								 
								getInteraction(key_dataset).getTree()
								.getFirstChild("browse").
								getFirstChild("output")
								.getFirstChild("property").
								getFirstChild(MapRedTextType.key_delimiter).
								getFirstChild().getHead();
								
							}
							catch (Exception e){
								error = "You must define a delimiter";
							}
						}
						existAndValid &= output.get(out_name).isPathExists() && output.get(out_name).isPathValid() == null;
						if(!existAndValid){
							error = "The path does not exist";
						}
					}
				}catch(Exception e){
					error = "Data set cannot be empty";
				}
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

	@Override
	public Map<String, DFEOutput> getDFEOutput() throws RemoteException {
		return output;
	}

	@Override
	public void update(DFEInteraction interaction) throws RemoteException {

		logger.info("updateinteraction Source ");

		if(interaction.getName().equals(getInteraction(key_datatype).getName())){
			updateDataType(interaction.getTree());
		}
		else if(interaction.getName().equals(getInteraction(key_datasubtype).getName())){
			updateDataSubType(interaction.getTree());
		}
		else{
			updateDataSet(interaction.getTree());
		}
	}

	public void updateDataType(Tree<String> treeDatatype) throws RemoteException{
		Tree<String> list = null;
		if(treeDatatype.getSubTreeList().isEmpty()){
			list = treeDatatype.add("list");
			list.add("output");

			Tree<String> values = list.add("values");
			values.add("value").add("Hive");
			values.add("value").add("HDFS");
		}
	}

	public void updateDataSubType(Tree<String> treeDatasubtype) throws RemoteException{
		Tree<String> list = null;
		if(treeDatasubtype.getSubTreeList().isEmpty()){
			list = treeDatasubtype.add("list");
			list.add("output");

			Tree<String> values = list.add("values");

			DFEInteraction interaction = getInteraction(key_datatype);
			if(interaction.getTree().getFirstChild("list").getFirstChild("output").getFirstChild() != null){

				String type = interaction.getTree().getFirstChild("list").getFirstChild("output").getFirstChild().getHead();

				List<String> dataOutputClassName = 
						WorkflowPrefManager.getInstance().getNonAbstractClassesFromSuperClass(
								DataOutput.class.getCanonicalName());

				for(String className : dataOutputClassName){
					DataOutput wa = null;
					try {
						wa = (DataOutput) Class.forName(className).newInstance();
					} catch (Exception e) {
						e.printStackTrace();
					}

					if (wa.getBrowser().toString().equalsIgnoreCase(type)){
						values.add("value").add(wa.getTypeName());
					}
				}
			}
		}
	}

	public void updateDataSet(Tree<String> treeDataset) throws RemoteException{
		String newType = getInteraction(key_datatype).getTree()
				.getFirstChild("list").getFirstChild("output")
				.getFirstChild().getHead();

		String newSubtype = null;
		if (newType.equalsIgnoreCase("HDFS")){
			newSubtype = getInteraction(key_datasubtype).getTree()
					.getFirstChild("list").getFirstChild("output")
					.getFirstChild().getHead();
		}

		if(treeDataset.getSubTreeList().isEmpty()){
			treeDataset.add("browse").add("output");
			treeDataset.getFirstChild("browse").add("subtype").add(newSubtype);
			treeDataset.getFirstChild("browse").add("type").add(newType);
		}else{
			Tree<String> oldType = treeDataset.getFirstChild("browse")
					.getFirstChild("type").getFirstChild();

			if(oldType != null && !oldType.getHead().equals(newType)){
				treeDataset.getFirstChild("browse").remove("type");
				treeDataset.getFirstChild("browse").remove("output");
				treeDataset.getFirstChild("browse").add("output");
				treeDataset.getFirstChild("browse").add("type").add(newType);
				treeDataset.getFirstChild("browse").add("subtype").add(newSubtype);
			}
		}
	}


	@Override
	public String updateOut() throws RemoteException {
		String error = null;

		try{
			String path = getInteraction(key_dataset).getTree()
					.getFirstChild("browse").getFirstChild("output")
					.getFirstChild("path").getFirstChild().getHead();

			Iterator<Tree<String>> it =  getInteraction(key_dataset).getTree()
					.getFirstChild("browse").getFirstChild("output")
					.getChildren("feature").iterator();

			FeatureList out = new OrderedFeatureList();
			while(it.hasNext()){
				Tree<String> cur = it.next();
				String name = cur.getFirstChild("name").getFirstChild().getHead();
				String type = cur.getFirstChild("type").getFirstChild().getHead();
				try{
					out.addFeature(name, FeatureType.valueOf(type));
				}catch(Exception e){
					error = "The type "+type+" does not exist";
				}
			}
			String type = getInteraction(key_datatype).getTree()
					.getFirstChild("list").getFirstChild("output")
					.getFirstChild().getHead();
			if(type.equalsIgnoreCase("hive")){
				output.put(out_name, new HiveType(out));
				output.get(out_name).setPath(path);
			}else if(type.equalsIgnoreCase("hdfs")){

				String subtype = getInteraction(key_datasubtype).getTree()
						.getFirstChild("list").getFirstChild("output")
						.getFirstChild().getHead();

				Iterator<String> dataOutputClassName = 
						WorkflowPrefManager.getInstance().getNonAbstractClassesFromSuperClass(
								DataOutput.class.getCanonicalName()).iterator();

				
				DFEOutput dataOutput = null;
				while (dataOutputClassName.hasNext()){
					String className = dataOutputClassName.next();
					dataOutput = (DFEOutput) Class.forName(className).newInstance();
					if(dataOutput.getTypeName().equalsIgnoreCase(subtype)){
						break;
					}else{
						dataOutput = null;
					}

				}
				
				dataOutput.setFeatures(out);

				output.put(out_name, dataOutput);
				output.get(out_name).setPath(path);

				String delimiter = "\001";
				try{
					delimiter = getInteraction(key_dataset).getTree()
							.getFirstChild("browse").
							getFirstChild("output")
							.getFirstChild("property").
							getFirstChild(MapRedTextType.key_delimiter).
							getFirstChild().getHead();
				}catch(Exception e){
					logger.debug("Delimiter not set, using default delimiter");
				}

				output.get(out_name).addProperty(MapRedTextType.key_delimiter, delimiter);
			}
			if(output.get(out_name) != null){
				output.get(out_name).setSavingState(SavingState.RECORDED);
			}

		}catch(Exception e){
			error = "Needs a data set";
		}

		return error;
	}



	@Override
	public boolean writeOozieActionFiles(File[] files) throws RemoteException {
		return false;
	}

}

