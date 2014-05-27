package idiro.workflow.server;

import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFEPage;
import idiro.workflow.server.interfaces.PageChecker;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * GUI page to display.
 * 
 * @author etienne
 * 
 */
public class Page extends UnicastRemoteObject implements DFEPage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3289925425242134517L;

	private static Logger logger = Logger.getLogger(Page.class);

	/**
	 * Title of the page
	 */
	protected String title;

	/**
	 * Number of columns
	 */
	protected int nbColumn;

	/**
	 * The image associated to the page
	 */
	protected File image;

	/**
	 * Legend associated to the page
	 */
	protected String legend;
	
	/**
	 * the text tip associated to the interaction
	 */
	protected String textTip;

	/**
	 * List of the interactions
	 */
	protected List<DFEInteraction> interactions = new LinkedList<DFEInteraction>();

	protected PageChecker checker = null;

	public Page(String title, File image, String legend, int nbColumn)
			throws RemoteException {
		super();
		init(title, image, legend, null, nbColumn);
	}
	
	public Page(String title, File image, String legend, String textTip, int nbColumn)
			throws RemoteException {
		super();
		init(title, image, legend, textTip, nbColumn);
	}

	/**
	 * Initialize a page with title , legend and number of columns
	 * 
	 * @param title
	 * @param legend
	 * @param nbColumn
	 * @throws RemoteException
	 */
	public Page(String title, String legend, int nbColumn)
			throws RemoteException {
		super();
		init(title, null, legend, null, nbColumn);
	}

	/**
	 * Initialize a page with a title , image and column size
	 * 
	 * @param title
	 * @param image
	 * @param nbColumn
	 * @throws RemoteException
	 */
	public Page(String title, File image, int nbColumn) throws RemoteException {
		super();
		init(title, image, null, null, nbColumn);
	}

	/**
	 * Intializ a page with a title and column size
	 * 
	 * @param title
	 * @param nbColumn
	 * @throws RemoteException
	 */
	public Page(String title, int nbColumn) throws RemoteException {
		super();
		init(title, null, null, null, nbColumn);
	}

	/**
	 * Initialize the page with values
	 * 
	 * @param title
	 * @param image
	 * @param legend
	 * @param textTip
	 * @param nbColumn
	 */
	private void init(String title, File image, String legend, String textTip, int nbColumn) {
		this.title = title;
		this.image = image;
		this.legend = legend;
		this.textTip = textTip;
		this.nbColumn = nbColumn;
		setChecker(new PageCheckerDefault());
	}

	/**
	 * Check if a page is correctly set up
	 * 
	 * @return <code>true</code> if ok else <code>false</code>
	 * @throws RemoteException
	 */
	public boolean checkInitPage() throws RemoteException {
		boolean ok = true;
		Map<Integer, LinkedList<Integer>> pages = new LinkedHashMap<Integer, LinkedList<Integer>>();

		Iterator<DFEInteraction> it = interactions.iterator();
		while (it.hasNext() && ok) {
			DFEInteraction ii = it.next();
			LinkedList<Integer> placeInPage = pages.get(ii.getColumn());
			if (placeInPage != null) {
				if (placeInPage.contains(ii.getPlaceInColumn())) {
					ok = false;
					logger.error("There is 2 element that have the same place");
				}
			} else {
				placeInPage = new LinkedList<Integer>();
				pages.put(ii.getColumn(), placeInPage);
			}

			if (ok) {
				placeInPage.add(ii.getPlaceInColumn());
			}
		}
		if (ok) {
			int minColumn = Integer.MAX_VALUE;
			int maxColumn = -1;
			Iterator<Integer> columnIt = pages.keySet().iterator();
			while (columnIt.hasNext()) {
				int nb = columnIt.next();
				minColumn = Math.min(minColumn, nb);
				maxColumn = Math.max(maxColumn, nb);
			}
			if (minColumn != 0) {
				logger.error("The first page number is 0");
				ok = false;
			} else if (maxColumn != pages.size() - 1) {
				logger.error("One page is empty");
				ok = false;
			}
			columnIt = pages.keySet().iterator();
			while (columnIt.hasNext() && ok) {
				int columnNb = columnIt.next();
				Iterator<Integer> placeIt = pages.get(columnNb).iterator();
				minColumn = Integer.MAX_VALUE;
				maxColumn = -1;
				while (placeIt.hasNext()) {
					int nb = placeIt.next();
					minColumn = Math.min(minColumn, nb);
					maxColumn = Math.max(maxColumn, nb);
				}
				if (minColumn != 0) {
					logger.error("The first place in a page is 0 (column "
							+ columnNb + ")");
					ok = false;
				} else if (maxColumn != pages.size() - 1) {
					logger.error("One place in a page is empty (column "
							+ columnNb + ")");
					ok = false;
				}
			}

		}

		return ok;
	}

	/**
	 * Add a user interaction to the page
	 * 
	 * @param e
	 * @return <code>true </code>if the interaction was added else
	 *         <code>false</code>
	 * @throws RemoteException
	 */
	public boolean addInteraction(DFEInteraction e) throws RemoteException {
		try {
			nbColumn = Math.max(e.getColumn() + 1, nbColumn);
			return interactions.add(e);
		} catch (RemoteException er) {
			logger.error("RemoteException error when creating an interaction");
			logger.error(er.getMessage());
		}
		return false;
	}

	/**
	 * Get the user interactions associated with a name
	 * 
	 * @param name
	 *            interaction name
	 * @return DFEInteraction
	 * @throws RemoteException
	 */
	public DFEInteraction getInteraction(String id) throws RemoteException {
		Iterator<DFEInteraction> it = getInteractions().iterator();
		DFEInteraction found = null;
		while (it.hasNext() && found == null) {
			if (!(found = it.next()).getId().equals(id)) {
				found = null;
			}
		}
		return found;
	}

	/**
	 * Get the page title
	 * 
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Get the number of columns
	 * 
	 * @return the nbColumn
	 */
	public int getNbColumn() {
		return nbColumn;
	}

	/**
	 * Get the path to the image
	 * 
	 * @return the image
	 */
	public String getImage() {
		return image.getAbsolutePath();
	}

	/**
	 * Get the legend for the page
	 * 
	 * @return the legend
	 */
	public String getLegend() {
		return legend;
	}

	/**
	 * Get the interactions that are on the page
	 * 
	 * @return the interactions
	 */
	public List<DFEInteraction> getInteractions() {
		return interactions;
	}

	@Override
	/**
	 * Check the page 
	 * @return Error message
	 */
	public String checkPage() throws RemoteException {
		String error = null;
		if (checker != null) {
			error = checker.check(this);
		}
		return error;
	}

	@Override
	/**
	 * Check if the page contains a checker
	 * @returm <code>true</code> if page has a checker else <code>false</code>
	 */
	public boolean haveChecker() {
		return checker != null;
	}

	/**
	 * Get the checker
	 * 
	 * @return the checker
	 */
	public final PageChecker getChecker() {
		return checker;
	}

	/**
	 * Set the checker for the page
	 * 
	 * @param checker
	 *            the checker to set
	 */
	public final void setChecker(PageChecker checker) {
		this.checker = checker;
	}

	@Override
	public String getTextTip() throws RemoteException {
		return textTip;
	}

}