package idm.interaction;

import idiro.utils.Tree;
import idiro.workflow.server.connect.interfaces.DataFlowInterface;
import idiro.workflow.server.connect.interfaces.DataStore;
import idiro.workflow.server.interfaces.DFEInteraction;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpSession;

public class BrowserInteraction extends CanvasModalInteraction {

	String pathBrowser;
	String typeBrowser;
	List<String> listFeatures;
	List<SelectItem> listProperties;

	static Map<String,DataStore> datastores;

	public BrowserInteraction(DFEInteraction dfeInter) throws RemoteException {
		super(dfeInter);
		if(datastores == null){
			FacesContext fCtx = FacesContext.getCurrentInstance();
			HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);
			datastores = ((DataFlowInterface) session.getAttribute("wfm")).getDatastores();
		}
	}

	@Override
	public void readInteraction() throws RemoteException {
		// clean the map
		listFeatures = new LinkedList<String>();
		listProperties = new LinkedList<SelectItem>();

		typeBrowser = inter.getTree()
				.getFirstChild("browse").getFirstChild("type")
				.getFirstChild().getHead();

		if (inter.getTree().getFirstChild("browse")
				.getFirstChild("output").getFirstChild("path") != null) {
			pathBrowser = inter.getTree()
					.getFirstChild("browse")
					.getFirstChild("output").getFirstChild("path")
					.getFirstChild().getHead();
			logger.info("path mount " + pathBrowser);
			if (!pathBrowser.startsWith("/")) {
				pathBrowser = "/" + pathBrowser;
			}
		}

		//set properties
		if (inter.getTree().getFirstChild("browse")
				.getFirstChild("output").getChildren("property") != null) {
			List<Tree<String>> props = inter.getTree().getFirstChild("browse")
					.getFirstChild("output").getChildren("property");
			if (props != null) {
				logger.info("properties not null: " + props.size());
				for (Tree<String> tree : props) {
					listProperties.add(new SelectItem(tree.getHead(), tree
							.getFirstChild().getHead()));
				}
			}
		}

		//set features
		if (inter.getTree().getFirstChild("browse")
				.getFirstChild("output").getChildren("property") != null) {
			List<Tree<String>> feats = inter.getTree().getFirstChild("browse")
					.getFirstChild("output").getChildren("feature");
			if (feats != null) {
				logger.info("properties not null: " + feats.size());
				for (Tree<String> tree : feats) {
					String name = tree.getFirstChild("name").getFirstChild().getHead();
					String type = tree.getFirstChild("type").getFirstChild().getHead();
					listFeatures.add(name+" "+type);
				}
			}
		}
	}

	@Override
	public void writeInteraction() throws RemoteException {
		inter.getTree().getFirstChild("browse")
		.getFirstChild("output").removeAllChildren();
		inter.getTree().getFirstChild("browse")
		.getFirstChild("output").add("path")
		.add(pathBrowser);

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
					+ pathBrowser);
			unchanged = pathBrowser.equals(
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
	 * @return the pathBrowser
	 */
	public final String getPathBrowser() {
		return pathBrowser;
	}

	/**
	 * @param pathBrowser the pathBrowser to set
	 */
	public final void setPathBrowser(String pathBrowser) {
		this.pathBrowser = pathBrowser;
	}

	/**
	 * @return the typeBrowser
	 */
	public final String getTypeBrowser() {
		return typeBrowser;
	}

	/**
	 * @param typeBrowser the typeBrowser to set
	 */
	public final void setTypeBrowser(String typeBrowser) {
		this.typeBrowser = typeBrowser;
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
	 * @return the datastores
	 */
	public static final Map<String, DataStore> getDatastores() {
		return datastores;
	}

	/**
	 * @param datastores the datastores to set
	 */
	public static final void setDatastores(Map<String, DataStore> datastores) {
		BrowserInteraction.datastores = datastores;
	}

}
