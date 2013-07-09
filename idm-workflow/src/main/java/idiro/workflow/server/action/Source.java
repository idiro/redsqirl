package idiro.workflow.server.action;

import idiro.utils.Tree;
import idiro.workflow.server.DataflowAction;
import idiro.workflow.server.Page;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.datatype.HiveType;
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
				}catch(Exception e){
					error = "Data type cannot be empty";
				}
				return error;
			}

		});

		Page page2 = addPage("Browse Source",
				"Pick a data set",
				1);

		DFEInteraction browse = new UserInteraction(
				key_dataset,
				"Please specify a data set",
				DisplayType.browser,
				0,
				0);

		page2.addInteraction(browse);
		
		page2.setChecker(new PageChecker(){

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
						if(!exist){
							error = "The data set does not exist";
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
		if(interaction == getInteraction(key_datatype)){
			updateDataType(interaction.getTree());
		}else{
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
		}
	}

	public void updateDataSet(Tree<String> treeDataset) throws RemoteException{
		String newType = getInteraction(key_datatype).getTree()
				.getFirstChild("list").getFirstChild("output")
				.getFirstChild().getHead();
		if(treeDataset.getSubTreeList().isEmpty()){
			treeDataset.add("browse").add("output");
			treeDataset.getFirstChild("browse").add("type").add(newType);
		}else{
			String oldType = treeDataset.getFirstChild("browse")
					.getFirstChild("type").getFirstChild().getHead();

			if(oldType != newType){
				treeDataset.getFirstChild("browse").remove("type");
				treeDataset.getFirstChild("browse").remove("output");
				treeDataset.getFirstChild("browse").add("output");
				treeDataset.getFirstChild("browse").add("type").add(newType);
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

