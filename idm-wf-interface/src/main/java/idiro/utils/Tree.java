package idiro.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Tree data structure interface.
 * @author etienne
 *
 * @param <T>
 */
public interface Tree<T> extends Serializable{
	
	
	public void write(FileWriter fw) throws IOException;

	public boolean comparePath(T[] path);

	public boolean isEmpty();
	
	public int getDepth();

	public List<Tree<T>> findInTree(T element);

	public List<Tree<T>> getChildren(T element);
	
	public List<Tree<T>> findChildren(T element);
	
	public Tree<T> getFirstChild(T element);
	
	public Tree<T> findFirstChild(T element);
	
	public Tree<T> getFirstChild();
	
	/**
	 * @param e
	 * @return
	 */
	public void add(Tree<T> e);
	
	public Tree<T> add(T element);
	
	public void remove(T element);
	
	public void removeAllChildren();

	/**
	 * @param e
	 * @return
	 */
	public void addFirst(Tree<T> e);

	/**
	 * @param arg0
	 * @return
	 * @see java.util.Set#addAll(java.util.Collection)
	 */
	public void addAll(Collection<Tree<T>> arg0);

	
	/**
	 * @return the parent
	 */
	public Tree<T> getParent();

	/**
	 * @param parent the parent to set
	 */
	public void setParent(Tree<T> parent);

	/**
	 * @return the head
	 */
	public T getHead();

	/**
	 * @return the subTreeList
	 */
	public List<Tree<T>> getSubTreeList();	
	
}
