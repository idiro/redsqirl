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
	
	
	public void write(FileWriter fw) throws IOException, RemoteException;

	public boolean comparePath(T[] path) throws RemoteException;

	public boolean isEmpty() throws RemoteException;
	
	public int getDepth() throws RemoteException;

	public List<Tree<T>> findInTree(T element) throws RemoteException;

	public List<Tree<T>> getChildren(T element) throws RemoteException;
	
	public List<Tree<T>> findChildren(T element) throws RemoteException;
	
	public Tree<T> getFirstChild(T element) throws RemoteException;
	
	public Tree<T> findFirstChild(T element) throws RemoteException;
	
	public Tree<T> getFirstChild() throws RemoteException ;
	
	/**
	 * @param e
	 * @return
	 */
	public void add(Tree<T> e) throws RemoteException;
	
	public Tree<T> add(T element) throws RemoteException;
	
	public void remove(T element) throws RemoteException;
	
	public void removeAllChildren() throws RemoteException;

	/**
	 * @param e
	 * @return
	 */
	public void addFirst(Tree<T> e) throws RemoteException;

	/**
	 * @param arg0
	 * @return
	 * @see java.util.Set#addAll(java.util.Collection)
	 */
	public void addAll(Collection<Tree<T>> arg0) throws RemoteException;

	
	/**
	 * @return the parent
	 */
	public Tree<T> getParent() throws RemoteException;

	/**
	 * @param parent the parent to set
	 */
	public void setParent(Tree<T> parent) throws RemoteException;

	/**
	 * @return the head
	 */
	public T getHead() throws RemoteException;

	/**
	 * @return the subTreeList
	 */
	public List<Tree<T>> getSubTreeList() throws RemoteException;	
	
}
