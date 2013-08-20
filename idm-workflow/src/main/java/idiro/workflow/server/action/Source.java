package idiro.workflow.server.action;

import idiro.utils.Tree;
import idiro.workflow.server.DataOutput;
import idiro.workflow.server.DataflowAction;
import idiro.workflow.server.Page;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.WorkflowPrefManager;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.datatype.HiveType;
import idiro.workflow.server.datatype.MapRedTextType;
import idiro.workflow.server.enumeration.DataBrowser;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.enumeration.FeatureType;
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

	public static final String key_datatype = "Data type"; 
	public static final String key_datasubtype = "Data subtype"; 
	public static final String key_dataset = "Data set"; 

	public Source() throws RemoteException {
		super(null);

		Page page1 = addPage("Source",
				"Loads a data source",
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
				try{
					if( getInteraction(key_datatype).getTree()
							.getFirstChild("list").getFirstChild("output")
							.getFirstChild().getHead().isEmpty()
							){
						error = "Data type cannot be empty";
					}
					else{
						String dataType = getInteraction(key_datatype).getTree()
								.getFirstChild("list").getFirstChild("output")
								.getFirstChild().getHead();
						
						if (dataType.equalsIgnoreCase("hdfs")){
							try{
								if( getInteraction(key_datasubtype).getTree()
										.getFirstChild("list").getFirstChild("output")
										.getFirstChild().getHead().isEmpty()
										){
									error = "Data subtype cannot be empty";
								}
							}catch(Exception e){
								error = "Data subtype cannot be empty";
							}
						}
					}
				}catch(Exception e){
					error = "Data type cannot be empty";
				}
				return error;
			}

		});
		
		
		Page page2 = addPage("Source",
				"Loads a data source",
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
						error = "Data type cannot be empty";
					}
					else{
						String dataType = getInteraction(key_datatype).getTree()
								.getFirstChild("list").getFirstChild("output")
								.getFirstChild().getHead();
						
						if (dataType.equalsIgnoreCase("hdfs")){
							try{
								if( getInteraction(key_datasubtype).getTree()
										.getFirstChild("list").getFirstChild("output")
										.getFirstChild().getHead().isEmpty()
										){
									error = "Data subtype cannot be empty";
								}
							}catch(Exception e){
								error = "Data subtype cannot be empty";
							}
						}
					}
					
					
					
						
						String type = getInteraction(key_datatype).getTree()
								.getFirstChild("list").getFirstChild("output")
								.getFirstChild().getHead();
						if(type.equalsIgnoreCase("hive")){
							output.put(out_name, new HiveType());
						}
						else if(type.equalsIgnoreCase("hdfs")){
							
							String subtype = getInteraction(key_datasubtype).getTree()
									.getFirstChild("list").getFirstChild("output")
									.getFirstChild().getHead();
							
							Iterator<String> dataOutputClassName = 
									WorkflowPrefManager.getInstance().getNonAbstractClassesFromSuperClass(
											DataOutput.class.getCanonicalName()).iterator();
							
							Class<?> klass = null;
							while (dataOutputClassName.hasNext()){
								String className = dataOutputClassName.next();
								String[] classNameArray = className.split("\\.");
								if (classNameArray[classNameArray.length-1].equals(subtype)){
									klass = Class.forName(className);
									break;
								}
							}
							
							DFEOutput dataOutput = (DFEOutput)(klass.getConstructor(Map.class).newInstance());
							
							output.put(out_name, dataOutput);
							
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
						
						
					
					
					
				}catch(Exception e){
					error = "Data type cannot be empty";
				}
				return error;
			}

		});
		

		Page page3 = addPage("Browse Source",
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
						
						boolean exist = false;
						if(type.equalsIgnoreCase("Hive")){
							HiveInterface hInt = new HiveInterface(); 
							exist = hInt.exists(path);
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
										error = "The output does not contains the feature "+desc[i];
									}
											
								}
							}
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
						if(!exist){
							error = "The file does not exist";
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

			Tree<String> value = list.add("value");
			value.add("Hive");
			value.add("HDFS");
		}
	}
	
	public void updateDataSubType(Tree<String> treeDatasubtype) throws RemoteException{
		Tree<String> list = null;
		if(treeDatasubtype.getSubTreeList().isEmpty()){
			list = treeDatasubtype.add("list");
			list.add("output");

			Tree<String> value = list.add("value");
			
			
			
			DFEInteraction interaction = getInteraction(key_datatype);
			if(interaction.getTree().getFirstChild("list").getFirstChild("output").getFirstChild() != null &&
					interaction.getTree().getFirstChild("list").getFirstChild("output").getFirstChild().getHead().equalsIgnoreCase("hive")){
				treeDatasubtype.getFirstChild("list").getFirstChild("output").add("Hive");
			}
			
			
			Iterator<String> dataOutputClassName = 
					WorkflowPrefManager.getInstance().getNonAbstractClassesFromSuperClass(
							DataOutput.class.getCanonicalName()).iterator();
				
			while(dataOutputClassName.hasNext()){
				String className = dataOutputClassName.next();
				DataOutput wa = null;
				try {
					wa = (DataOutput) Class.forName(className).newInstance();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if (wa.getBrowser().equals(DataBrowser.HDFS)){
					value.add(DataOutput.class.getSimpleName());
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
			String oldType = treeDataset.getFirstChild("browse")
					.getFirstChild("type").getFirstChild().getHead();

			if(oldType != newType){
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

			Map<String,FeatureType> out = new LinkedHashMap<String,FeatureType>();
			while(it.hasNext()){
				Tree<String> cur = it.next();
				String name = cur.getFirstChild("name").getFirstChild().getHead();
				String type = cur.getFirstChild("type").getFirstChild().getHead();
				try{
					out.put(name, FeatureType.valueOf(type));
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
			}
			else if(type.equalsIgnoreCase("hdfs")){
				
				String subtype = getInteraction(key_datasubtype).getTree()
						.getFirstChild("list").getFirstChild("output")
						.getFirstChild().getHead();
				
				Iterator<String> dataOutputClassName = 
						WorkflowPrefManager.getInstance().getNonAbstractClassesFromSuperClass(
								DataOutput.class.getCanonicalName()).iterator();
				
				Class<?> klass = null;
				while (dataOutputClassName.hasNext()){
					String className = dataOutputClassName.next();
					String[] classNameArray = className.split("\\.");
					if (classNameArray[classNameArray.length-1].equals(subtype)){
						klass = Class.forName(className);
						break;
					}
				}
				
				DFEOutput dataOutput = (DFEOutput)(klass.getConstructor(Map.class).newInstance(out));
				
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

