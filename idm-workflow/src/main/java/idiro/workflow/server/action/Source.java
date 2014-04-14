package idiro.workflow.server.action;

import idiro.utils.Tree;
import idiro.workflow.server.DataOutput;
import idiro.workflow.server.WorkflowPrefManager;
import idiro.workflow.server.datatype.HiveType;
import idiro.workflow.server.datatype.MapRedTextType;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFELinkProperty;

import java.io.File;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Action that read a source file. For now, only Hive type is supported.
 * 
 * @author etienne
 * 
 */
public class Source extends AbstractSource {

	private static final long serialVersionUID = 7519928238030041208L;

	/**
	 * Constructor containing the pages, page checks and interaction
	 * Initialization
	 * 
	 * @throws RemoteException
	 */
	public Source() throws RemoteException {
		super();

		addTypePage();
		addSubTypePage();
		addSourcePage();
	}

	/**
	 * Get the name of the Action
	 * 
	 * @return name
	 * @throws RemoteException
	 */
	@Override
	public String getName() throws RemoteException {
		return "Source";
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

	// Override default static methods
	/**
	 * Get the path for the help file
	 * 
	 * @return path
	 * @throws RemoteException
	 */
	@Override
	public String getHelp() throws RemoteException {
		String absolutePath = "";
		String helpFile = "/help/" + getName().toLowerCase() + ".html";
		String path = WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_tomcat_path);
		logger.info(helpFile);
		logger.info(path);
		List<String> files = listFilesRecursively(path);
		for (String file : files) {
			if (file.contains(helpFile)) {
				absolutePath = file;
				break;
			}
		}
		String ans = "";
		if (absolutePath.contains(path)) {
			ans = absolutePath.substring(path.length());
		}
		logger.info("Source help absPath : " + absolutePath);
		logger.info("Source help Path : " + path);
		logger.info("Source help ans : " + ans);
		// absolutePath
		return absolutePath;
	}

	/**
	 * Get the path of the Image file
	 * 
	 * @return path
	 * @throws RemoteException
	 */
	@Override
	public String getImage() throws RemoteException {
		String absolutePath = "";
		String imageFile = "/image/" + getName().toLowerCase() + ".gif";
		String path = WorkflowPrefManager
						.getSysProperty(WorkflowPrefManager.sys_tomcat_path);
		List<String> files = listFilesRecursively(path);
		for (String file : files) {
			if (file.contains(imageFile)) {
				absolutePath = file;
				break;
			}
		}
		String ans = "";
		if (absolutePath.contains(path)) {
			ans = absolutePath.substring(path.length());
		}
		logger.info("Source image abs Path : " + absolutePath);
		logger.info("Source image Path : " + path);
		logger.info("Source image ans : " + ans);

		return absolutePath;
	}

	/**
	 * Update the Interaction provided
	 * 
	 * @param interaction
	 * @throws RemoteException
	 */
	@Override
	public void update(DFEInteraction interaction) throws RemoteException {

		logger.info("updateinteraction Source ");
		String interId = interaction.getId();
		logger.info("interaction : " + interId);
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

	/**
	 * Update the DataSubType Interaction
	 * 
	 * @param treeDatasubtype
	 * @throws RemoteException
	 */
	public void updateDataSubType(Tree<String> treeDatasubtype)
			throws RemoteException {
		logger.info("updating data subtype");
		String type = dataType.getValue();
		logger.info("data type : " + type);
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
				logger.info("class : " + wa.getClass().getCanonicalName());
				logger.info("wa type : " + wa.getTypeName());
				if (wa.getBrowser() != null
						&& wa.getBrowser().toString().equalsIgnoreCase(type)) {
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
			logger.info(" is " + posValues.toString());
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
	 * Update the DataSet Interaction
	 * 
	 * @param treeDataset
	 * @throws RemoteException
	 */
	public void updateDataSet(Tree<String> treeDataset) throws RemoteException {

		String newType = dataType.getValue();
		logger.info("type : " + newType);
		String newSubtype = dataSubtype.getValue();
		logger.info("subtype : " + newSubtype);

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

	/**
	 * Update the output
	 * 
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String updateOut() throws RemoteException {
		String error = checkIntegrationUserVariables();
		return error;
	}

	/**
	 * Not Supported
	 */
	@Override
	public boolean writeOozieActionFiles(File[] files) throws RemoteException {
		return false;
	}
}
