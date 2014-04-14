package idiro.workflow.server.action;

import idiro.workflow.server.WorkflowPrefManager;

import java.rmi.RemoteException;
import java.util.List;

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
		logger.debug("Source image abs Path : " + absolutePath);
		logger.debug("Source image Path : " + path);
		logger.debug("Source image ans : " + ans);

		return absolutePath;
	}
}
