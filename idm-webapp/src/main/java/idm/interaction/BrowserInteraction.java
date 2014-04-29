package idm.interaction;

import idiro.utils.Tree;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DataFlowElement;
import idm.CanvasModalOutputTab;
import idm.FileSystemBean;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.faces.model.SelectItem;

public class BrowserInteraction extends CanvasModalInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3020683683280306022L;

	/**
	 * List of the feature name
	 */
	private List<String> listFeatures;

	/**
	 * List of the properties
	 */
	private List<SelectItem> listProperties;

	/**
	 * Object that will manage the output and display it. 
	 * It is supposed that a browser element has only one output.
	 */
	private CanvasModalOutputTab modalOutput;

	public BrowserInteraction(DFEInteraction dfeInter,DataFlowElement dfe,CanvasModalOutputTab outputTab) throws RemoteException {
		super(dfeInter);
		this.modalOutput = outputTab;
		outputTab.resetNameOutput();
		outputTab.updateDFEOutputTable();
	}

	@Override
	public void readInteraction() throws RemoteException {
		// clean the map
		listFeatures = new LinkedList<String>();
		listProperties = new LinkedList<SelectItem>();
		logger.info(printTree(inter.getTree()));
		try{
			if (inter.getTree().getFirstChild("browse")
					.getFirstChild("output").getFirstChild("path") != null) {
				setPath(inter.getTree()
						.getFirstChild("browse")
						.getFirstChild("output").getFirstChild("path")
						.getFirstChild().getHead());
				logger.info("path mount " + getPath());
				if (!getPath().startsWith("/")) {
					setPath("/" + getPath());
				}
			}
		}catch(Exception e){
			logger.info("Exception: "+e.getMessage());
			setPath(null);
		}

		//set properties
		try{
			if (inter.getTree().getFirstChild("browse")
					.getFirstChild("output").getChildren("property") != null) {
				List<Tree<String>> props = inter.getTree().getFirstChild("browse")
						.getFirstChild("output").getFirstChild("property").getSubTreeList();
				if (props != null) {
					logger.info("properties not null: " + props.size());
					for (Tree<String> tree : props) {
						listProperties.add(new SelectItem(tree
								.getFirstChild().getHead(),tree.getHead()));
					}
				}
			}
		}catch(Exception e){
			logger.info("Exception: "+e.getMessage());
		}

		//set features
		try{
			List<Tree<String>> feats = inter.getTree().getFirstChild("browse")
					.getFirstChild("output").getChildren("feature");
			if (feats != null && !feats.isEmpty()) {
				logger.info("features not null: " + feats.size());
				for (Tree<String> tree : feats) {
					String name = tree.getFirstChild("name").getFirstChild().getHead();
					String type = tree.getFirstChild("type").getFirstChild().getHead();
					listFeatures.add(name+" "+type);
				}
			}
		}catch(Exception e){
			logger.info("Exception: "+e.getMessage());
		}

		if(modalOutput != null){
			modalOutput.resetNameOutput();
			modalOutput.updateDFEOutputTable();
		}
	}

	@Override
	public void writeInteraction() throws RemoteException {
		inter.getTree().getFirstChild("browse")
		.getFirstChild("output").removeAllChildren();
		inter.getTree().getFirstChild("browse")
		.getFirstChild("output").add("path")
		.add(getPath());

		Tree<String> myProperty = inter.getTree()
				.getFirstChild("browse").getFirstChild("output")
				.add("property");
		for (SelectItem item : listProperties) {
			logger.info("Add property: " + item.getLabel()
					+ ": " + item.getValue());
			myProperty.add(item.getLabel()).add(
					item.getValue().toString());
		}

		for (String nameValue : listFeatures) {
			Tree<String> myFeature = inter.getTree()
					.getFirstChild("browse")
					.getFirstChild("output").add("feature");
			String value[] = nameValue.split(" ");
			myFeature.add("name").add(value[0]);
			myFeature.add("type").add(value[1]);
		}
	}

	@Override
	public void setUnchanged() {
		try {
			// Check path
			String oldPath = inter.getTree().getFirstChild("browse")
					.getFirstChild("output").getFirstChild("path")
					.getFirstChild().getHead();
			logger.info("Comparaison path: " + oldPath + " , "
					+ getPath());
			unchanged = getPath().equals(
					oldPath);

			// Check properties
			if (unchanged) {
				for (SelectItem itemList : listProperties) {
					String key = itemList.getLabel();
					logger.info("Comparaison property "
							+ key
							+ ": "
							+ itemList.getValue()
							+ " , "
							+ inter.getTree()
							.getFirstChild("browse")
							.getFirstChild("output")
							.getFirstChild("property")
							.getFirstChild(key).getFirstChild()
							.getHead());
					unchanged &= inter.getTree()
							.getFirstChild("browse")
							.getFirstChild("output")
							.getFirstChild("property")
							.getFirstChild(key).getFirstChild()
							.getHead().equals(itemList.getValue());
				}
			}

			// Check features
			if (unchanged) {
				List<Tree<String>> oldFeatureList = inter.getTree()
						.getFirstChild("browse")
						.getFirstChild("output").getChildren("feature");
				logger.info("comparaison features: "
						+ oldFeatureList.size() + " , "
						+ listFeatures.size());
				if (unchanged &= oldFeatureList.size() == listFeatures.size()) {
					Iterator<Tree<String>> oldFeatureIt = oldFeatureList
							.iterator();
					for (String nameValue : listFeatures) {
						Tree<String> feature = oldFeatureIt.next();
						String value[] = nameValue.split(" ");
						logger.info("Comparaison feature: "
								+ feature.getFirstChild("name")
								.getFirstChild().getHead()
								+ " , "
								+ value[0]
										+ " | type "
										+ feature.getFirstChild("type")
										.getFirstChild().getHead()
										+ " , " + value[1]);

						if (feature.getFirstChild("name")
								.getFirstChild().getHead()
								.equals(value[0])) {
							unchanged &= feature
									.getFirstChild("type")
									.getFirstChild().getHead()
									.equals(value[1]);
						} else {
							unchanged = false;
						}
					}

				}
			}
		} catch (Exception e) {
			unchanged = false;
		}
	}

	/**
	 * @return the listFeatures
	 */
	public final List<String> getListFeatures() {
		return listFeatures;
	}

	/**
	 * @param listFeatures the listFeatures to set
	 */
	public final void setListFeatures(List<String> listFeatures) {
		this.listFeatures = listFeatures;
	}

	/**
	 * @return the listProperties
	 */
	public final List<SelectItem> getListProperties() {
		return listProperties;
	}

	/**
	 * @param listProperties the listProperties to set
	 */
	public final void setListProperties(List<SelectItem> listProperties) {
		this.listProperties = listProperties;
	}

	/**
	 * @return
	 * @see idm.CanvasModalOutputTab#getFileSystem()
	 */
	public final FileSystemBean getFileSystem() {
		return modalOutput.getFileSystem();
	}

	/**
	 * @return
	 * @see idm.CanvasModalOutputTab#getPath()
	 */
	public String getPath() {
		return modalOutput.getPath();
	}

	/**
	 * @param path
	 * @see idm.CanvasModalOutputTab#setPath(java.lang.String)
	 */
	public void setPath(String path) {
		modalOutput.setPath(path);
	}

	/**
	 * @return
	 * @see idm.CanvasModalOutputTab#getTitles()
	 */
	public List<String> getGridTitles() {
		return modalOutput != null ? modalOutput.getTitles():null;
	}

	/**
	 * @return
	 * @see idm.CanvasModalOutputTab#getRows()
	 */
	public List<String[]> getGridRows() {
		return modalOutput != null ? modalOutput.getRows():null;
	}

}
