package com.redsqirl.utils;


import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
/**
 * 
 * Generic tree representation that is used to store options and configurations
 *
 * @param <T>
 */
public class TreeNonUnique<T> extends UnicastRemoteObject implements Tree<T>{

	private static final long serialVersionUID = 7567996074131392134L;
	
	/**
	 * Logger for class
	 */
	private static Logger logger = Logger.getLogger(TreeNonUnique.class);

	/**
	 * Head of the tree
	 */
	private T head;
	/**
	 * Parent tree
	 */
	private TreeNonUnique<T> parent = null;
	/**
	 * List of SubTree List
	 */
	private LinkedList<Tree<T>> subTreeList = 
			new LinkedList<Tree<T>>();

	/**
	 * Constructor
	 * @param head
	 * @throws RemoteException
	 */
	public TreeNonUnique(T head) throws RemoteException{
		super();
		this.head = head;
	}

	/**
	 * Constructor with parent
	 * @param head
	 * @param parent
	 * @throws RemoteException
	 */
	public TreeNonUnique(T head,TreeNonUnique<T> parent) throws RemoteException{
		super();
		this.head = head;
		this.parent = parent;
	}

	@Override
	/**
	 * Creates a String representation of the tree
	 * @return tree String representation of the tree
	 * 
	 */
	public String toString(){
		String ans = "";
		if(head != null){
			ans = head.toString();
		}
		Iterator<Tree<T>> it = subTreeList.iterator();
		while(it.hasNext()){
			ans = ans + "\n\t" + it.next().toString().replaceAll("\n", "\n\t");
		}

		return ans;

	}
	
	/**
	 * Write the tree to a file
	 * @param fw FileWriter to write to file
	 * @throws IOException
	 */
	public void write(FileWriter fw) throws IOException{
		String root = head.toString();
		Tree<T> ancestor = parent;
		while(ancestor != null){
			root = ancestor.getHead().toString()+"/"+root;
			ancestor = ancestor.getParent();
		}

		if(subTreeList.size() > 0){
			String listChildren = "";

			Iterator<Tree<T>> it = subTreeList.iterator();
			while(it.hasNext()){
				listChildren += " "+it.next().getHead().toString();
			}
			if(!listChildren.isEmpty()){
				fw.write(root+":"+listChildren+"\n");
				it = subTreeList.iterator();
				while(it.hasNext()){
					it.next().write(fw);
				}
			}
		}

		if(parent == null){
			if(subTreeList.size() == 0){
				fw.write(head.toString()+":\n");
			}
			fw.write("\n");
		}

	}
	/**
	 * @param 
	 */
	public boolean comparePath(T[] path){
		boolean ok = true;
		int i = path.length -1;
		TreeNonUnique<T> ancestor = this;
		while(ok && i >= 0){
			logger.debug("compare : "+ancestor.head+" "+path[i]);
			if(ancestor == null || !ancestor.head.equals(path[i])){
				ok = false;
			}
			ancestor = ancestor.getParent();
			--i;
		}
		if(!ok){
			logger.debug("The path is different");
		}else{
			logger.debug("Same path");
		}
		return ok;
	}

	public int getDepth() throws RemoteException{
		int i = 1;
		Tree<T> ancestor = parent;
		while(ancestor != null){
			ancestor = ancestor.getParent();
			++i;
		}
		return i;
	}

	public List<Tree<T>> findInTree(T element) throws RemoteException{
		TreeNonUnique<T> root = this;
		while(root.getParent() != null){
			root = root.getParent();
		}
		return root.getChildren(element);
	}

	public List<Tree<T>> getChildren(T element) throws RemoteException{
		List<Tree<T>> ans = new LinkedList<Tree<T>>();
		Iterator<Tree<T>> it = subTreeList.iterator();
		while(it.hasNext()){
			Tree<T> cur = it.next();
			if(cur.getHead().equals(element)){
				ans.add(cur);
			}
		}
		return ans;
	}

	public List<Tree<T>> findChildren(T element) throws RemoteException{
		List<Tree<T>> ans = new LinkedList<Tree<T>>();
		Iterator<Tree<T>> it = subTreeList.iterator();
		while(it.hasNext()){
			Tree<T> cur = it.next();
			if(cur.getHead().equals(element)){
				ans.add(cur);
			}
			ans.addAll(cur.findChildren(element));
		}
		return ans;
	}


	/**
	 * @param e
	 * @return
	 */
	public void add(Tree<T> e) throws RemoteException {
		if(e instanceof TreeNonUnique){
			e.setParent(this);
			if(e.getHead() != null){
				subTreeList.add(e);
			}
		}
	}

	/**
	 * @param e
	 * @return
	 */
	public void addFirst(Tree<T> e) throws RemoteException {
		if(e instanceof TreeNonUnique){
			e.setParent(this);
			if(e.getHead() != null){
				subTreeList.addFirst(e);
			}
		}
	}

	/**
	 * @param arg0
	 * @return
	 * @see java.util.Set#addAll(java.util.Collection)
	 */
	public void addAll(Collection<Tree<T>> arg0) throws RemoteException {
		Iterator<Tree<T>> it = arg0.iterator();
		while(it.hasNext()){
			Tree<T> e = it.next();
			if(e.getHead() != null){
				add(e);
			}
		}
	}

	/**
	 * @return the parent
	 */
	public TreeNonUnique<T> getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(Tree<T> parent) {
		if(parent instanceof TreeNonUnique){
			this.parent = (TreeNonUnique<T>) parent;
		}
	}

	/**
	 * @return the head
	 */
	public T getHead() {
		return head;
	}

	/**
	 * @return the subTreeList
	 */
	public List<Tree<T>> getSubTreeList() {
		return subTreeList;
	}

	@Override
	public boolean equals(Object o) {
		boolean equal = false;

		if(o instanceof Tree){
			Tree<?> tree = (Tree<?>)o;
			try {
				if( tree.getHead().getClass().getCanonicalName().equals(
						head.getClass().getCanonicalName())){
					@SuppressWarnings("unchecked")
					Tree<T> treeT = (Tree<T>) tree;

					if( treeT.getHead().equals(getHead())){
						equal = true;
						Iterator<Tree<T>> it = subTreeList.iterator();
						Iterator<? extends Tree<T>> it2 = treeT.getSubTreeList().iterator();
						while(it.hasNext() && equal){
							if(!it2.hasNext()){
								equal = false;
							}else{
								equal = it.next().equals(it2.next());
							}
						}
					}
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return equal;
	}

	@Override
	public Tree<T> getFirstChild(T element) throws RemoteException {
		List<Tree<T>> list = getChildren(element);
		return !list.isEmpty() ? list.get(0) : null;
	}


	public Tree<T> findFirstChild(T element) throws RemoteException{
		Tree<T> ans = null;
		Iterator<Tree<T>> it = subTreeList.iterator();
		while(it.hasNext() && ans == null){
			Tree<T> cur = it.next();
			if(cur.getHead().equals(element)){
				ans = cur;
			}else{
				ans = cur.findFirstChild(element);
			}
		}

		return ans;
	}

	@Override
	public Tree<T> getFirstChild() {
		Tree<T> ans = null;
		Iterator<Tree<T>> it = subTreeList.iterator();
		if(it.hasNext()){
			ans = it.next();
		}
		return ans;
	}


	@Override
	public Tree<T> add(T element) {
		Tree<T> ans = null;
		try {
			ans = new TreeNonUnique<T>(element);
			if(ans.getHead() != null){
				add(ans);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} 
		return ans;
	}

	@Override
	public void remove(T element) throws RemoteException{
		subTreeList.removeAll(getChildren(element));
	}

	@Override
	public void removeAllChildren() {
		subTreeList.clear();	
	}

	@Override
	public boolean isEmpty() {
		return subTreeList.isEmpty();
	}

	@Override
	public void setHead(T head) throws RemoteException {
		this.head = head;
	}
}