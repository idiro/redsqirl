package idiro.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;

/**
 * Tree data structure interface.
 * @author etienne
 *
 * @param <T>
 */
public interface Tree<T> extends Remote{
	
	/**
	 * Write tree to a file
	 * @param fw FileWriter to write tree with
	 * @throws IOException
	 * @throws RemoteException
	 */
	public void write(FileWriter fw) throws IOException, RemoteException;
	/**
	 * Compare Paths
	 * @param path Array of Paths to compare
	 * @return <code>true</code> if paths are equal else <code>false</code>
	 * @throws RemoteException
	 */
	public boolean comparePath(T[] path) throws RemoteException;
	/**
	 * Check if the tree is empty
	 * @return <code>true</code> if tree has no children or head else <code>false</code>
	 * @throws RemoteException
	 */
	public boolean isEmpty() throws RemoteException;
	
	/**
	 * Get the depth of the tree
	 * @return int size of the depth of the tree
	 * @throws RemoteException
	 */
	public int getDepth() throws RemoteException;
	/**
	 * Find all children that contains an element
	 * @param element element to search for 
	 * @return List of {@link idiro.utils.Tree<T>} that contains element
	 * @throws RemoteException
	 */
	public List<Tree<T>> findInTree(T element) throws RemoteException;
	/**
	 * Get a list of children with a specific head
	 * @param element head to look for
	 * @return List of {@link idiro.utils.Tree<T>} that contains the head
	 * @throws RemoteException
	 */
	public List<Tree<T>> getChildren(T element) throws RemoteException;
	/**
	 * Find a list of children with a specific head
	 * @param element head to look for
	 * @return List of {@link idiro.utils.Tree<T>} that contains the head
	 * @throws RemoteException
	 */
	public List<Tree<T>> findChildren(T element) throws RemoteException;
	/**
	 * Get the first child element with a specific head
	 * @param element head to look for
	 * @return {@link idiro.utils.Tree<T>} that contains the head
	 * @throws RemoteException
	 */
	public Tree<T> getFirstChild(T element) throws RemoteException;
	
	/**
	 * Find the first child element with a specific head
	 * @param element head to look for
	 * @return {@link idiro.utils.Tree<T>} that contains the head
	 * @throws RemoteException
	 */
	public Tree<T> findFirstChild(T element) throws RemoteException;
	/**
	 * Get the first child element of the tree
	 * @return {@link idiro.utils.Tree<T>} of the first child
	 * @throws RemoteException
	 */
	public Tree<T> getFirstChild() throws RemoteException ;
	
	/**
	 * Add an child element to a tree
	 * @param e {@link idiro.utils.Tree<T>} to be added to the parent
	 * 
	 */
	public void add(Tree<T> e) throws RemoteException;
	/**
	 * Add an head to a tree
	 * @param element
	 * @return {@link idiro.utils.Tree<T>} with added head 
	 * @throws RemoteException
	 */
	public Tree<T> add(T element) throws RemoteException;
	/**
	 * Remove a specific child element
	 * @param element child element to remove 
	 * @throws RemoteException
	 */
	public void remove(T element) throws RemoteException;
	/**
	 * Remove all Children from the tree
	 * @throws RemoteException
	 */
	public void removeAllChildren() throws RemoteException;

	/**
	 * Add a child as the first
	 * @param e child to be added
	 * @throws RemoteException
	 */
	public void addFirst(Tree<T> e) throws RemoteException;

	/**
	 * Add a collection of trees
	 * @param arg0 Collection of trees to add
	 * @see java.util.Set#addAll(java.util.Collection)
	 * @throws RemoteException
	 */
	public void addAll(Collection<Tree<T>> arg0) throws RemoteException;

	
	/**
	 * Get the parent tree
	 * @return {@link idiro.utils.Tree<T>} the parent
	 * @throws RemoteException
	 */
	public Tree<T> getParent() throws RemoteException;
	/**
	 * Set a parent tree
	 * @param parent the parent to set
	 * @throws RemoteException
	 */
	public void setParent(Tree<T> parent) throws RemoteException;
	/**
	 * Get the head of the tree
	 * @return T the head of the tree
	 * @throws RemoteException
	 */
	public T getHead() throws RemoteException;
	/**
	 * Get the List of sub trees
	 * @return List of Tree containing the subTreeList
	 * @throws RemoteException
	 */
	public List<Tree<T>> getSubTreeList() throws RemoteException;	
	
}
