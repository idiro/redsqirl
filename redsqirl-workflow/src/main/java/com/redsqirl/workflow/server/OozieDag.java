/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

package com.redsqirl.workflow.server;


import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.redsqirl.workflow.utils.LanguageManagerWF;

/**
 * Transform a workflow to be runnable on Oozie.
 * 
 * The Oozie Dag takes a data flow as input, with a start and an end node. The
 * transform method will add join and fork nodes, their names starting by
 * 'join_' and 'fork_'. These nodes represent fork and join Oozie nodes.
 * 
 * @author etienne
 * 
 */
public class OozieDag {
	/** Graph in */
	Map<String, Set<String>> graphOut;
	/** Graph out */
	Map<String, Set<String>> graphIn;
	/** Sorted List of elements */
	List<String> elementSorted;
	
	private static Logger logger = Logger.getLogger(OozieDag.class);

	/**
	 * Default Constructor
	 */
	public OozieDag() {
		graphOut = new LinkedHashMap<String, Set<String>>();
		graphIn = new LinkedHashMap<String, Set<String>>();
	}

	/**
	 * Init the DAG with Out Graph
	 * 
	 * @param outGraph
	 */

	public void initWithOutGraph(Map<String, Set<String>> outGraph) {
		Iterator<String> keys = outGraph.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			Iterator<String> elIt = outGraph.get(key).iterator();
			while (elIt.hasNext()) {
				addLink(key, elIt.next());
			}
		}
		removeAlliregularLinks();
	}
	
	protected void removeIregularLinks(Iterable<String> nodes){
		//Remove irregular links if needed....
		Iterator<String> elsIt = nodes.iterator();
		while(elsIt.hasNext()){
			String el = elsIt.next();
			Iterator<String> linkIt = graphOut.get(el).iterator();
			while(linkIt.hasNext()){
				removeIregularLink(el,linkIt.next());
			}
		}
	}
	protected void removeAlliregularLinks(){
		//Remove irregular links if needed....
		Iterator<String> elsIt = graphOut.keySet().iterator();
		while(elsIt.hasNext()){
			String el = elsIt.next();
			Iterator<String> linkIt = graphOut.get(el).iterator();
			while(linkIt.hasNext()){
				removeIregularLink(el,linkIt.next());
			}
		}
	}

	/**
	 * Add links between elements using the Out graph and in graph to store
	 * links
	 * 
	 * @param elementFrom
	 * @param elementTo
	 */
	public void addLink(String elementFrom, String elementTo) {
		
		
		//Add a link only if there is no dependency yet
		if(!isLeafOf(elementTo,elementFrom)){
		
			if (!graphOut.containsKey(elementFrom)) {
				graphOut.put(elementFrom, new LinkedHashSet<String>());
			}
			graphOut.get(elementFrom).add(elementTo);

		
		
			if (!graphIn.containsKey(elementTo)) {
				graphIn.put(elementTo, new LinkedHashSet<String>());
			}
			graphIn.get(elementTo).add(elementFrom);
		}
		
		removeIregularLink(elementFrom,elementTo);
	}
	
	protected void removeIregularLink(String elementFrom,String elementTo){

		//Search if among the ancestor of elementFrom there is a parent of elementTo. 
		//If it is the case we remove the link
		boolean found = false;
		Iterator<String> previousElementIt = getAllBefore(elementFrom).iterator();
		String previousElement = null;
		while(previousElementIt.hasNext() && !found){
			previousElement = previousElementIt.next();
			if(graphIn.containsKey(elementTo)){
				found = graphIn.get(elementTo).contains(previousElement);
			}
		}
		
		if(found){
			removeLink(previousElement, elementTo);
		}
	}
	
	public void removeLink(String elementFrom,String elementTo){
		graphOut.get(elementFrom).remove(elementTo);
		graphIn.get(elementTo).remove(elementFrom);
	}

	/**
	 * Sort the elementSorted List using graphOut and graphIn
	 * 
	 * @return <code>true</code> it was sorted without problems else
	 *         <code>false</code>
	 */
	protected boolean sort() {
		String error = null;
		LinkedList<String> newList = new LinkedList<String>();

		LinkedList<String> queueList = new LinkedList<String>();

		Iterator<String> actionIt = graphOut.keySet().iterator();
		while (actionIt.hasNext()) {
			String cur = actionIt.next();
			if (!graphIn.containsKey(cur)) {
				queueList.add(cur);
			}
		}

		while (!queueList.isEmpty()) {

			newList.add(queueList.removeFirst());
			actionIt = graphIn.keySet().iterator();
			while (actionIt.hasNext()) {
				String cur = actionIt.next();
				if (!newList.contains(cur) && !queueList.contains(cur)) {
					Set<String> tmp = graphIn.get(cur);
					if (newList.containsAll(tmp)) {
						queueList.add(cur);
					}
				}
			}
		}

		if (newList.size() < graphOut.keySet().size()) {
			error = LanguageManagerWF.getText("ooziedag.cycleerror");
			logger.error(error);
		}

		elementSorted = newList;

		return error == null;
	}

	/**
	 * Transform DAG from sequential DAG to parralel which will include forks
	 * and joins at certain point in the DAG
	 * 
	 * @return <code>true</code> if the transform was ok else <code>false</code>
	 */
	public boolean transform() {
		logger.debug("sort...");
		boolean ok = sort();
		int iter = 0;
		int iterMax = 2*graphOut.size();
		if (ok) {
			logger.debug("graph: " + graphOut);
			logger.debug("join...");
			String firstJoin = null;
			while ((firstJoin = getFirstIregularJoin()) != null && iter < iterMax) {
				logger.debug("Add join before: " + firstJoin);
				String comesFrom = getCommonRootOf(firstJoin);

				logger.debug("Add fork after: " + comesFrom);

				moveElementAfterJoin(comesFrom, firstJoin);
				removeAlliregularLinks();
				if(graphOut.get(comesFrom).size() > 1){
					placeForkAfter(comesFrom, firstJoin, "pair_" + firstJoin);
					placeJoinBefore(firstJoin, comesFrom, firstJoin);
				}
				logger.debug(graphOut);
				++iter;
			}
			logger.debug("fork...");
			String firstFork = null;
			iter = 0;
			while ((firstFork = getFirstIregularFork()) != null && iter < iterMax) {
				logger.debug("Add fork after: " + firstFork);
				String goTo = getCommonLeafOf(firstFork);
				logger.debug("Add join before: " + goTo);
				moveElementAfterJoin(firstFork, goTo);
				removeAlliregularLinks();
				if(graphOut.get(firstFork).size() > 1){
					placeForkAfter(firstFork, goTo, firstFork);
					placeJoinBefore(goTo, firstFork, "pair_" + firstFork);
				}
				logger.debug(graphOut);
				++iter;
			}
			if(iter == iterMax){
				logger.error("Fail transforming the graph");
				ok = false;
			}
		}

		return ok;
	}

	/**
	 * Get the first irregular Join of the DAG
	 * 
	 * @return ForkName
	 */
	protected String getFirstIregularJoin() {
		Iterator<String> it = elementSorted.iterator();
		String found = null;
		while (it.hasNext() && found == null) {
			String cur = it.next();
			if (!cur.startsWith("join-")) {
				Set<String> in = graphIn.get(cur);
				if (in != null && in.size() > 1) {
					found = cur;
				}
			}
		}
		return found;
	}

	/**
	 * Get the common root of an element with a join
	 * 
	 * @param elementJoin
	 * @return root that is common
	 */

	protected String getCommonRootOf(String elementJoin) {

		boolean found = false;
		// Take the first element for root testing
		String firstEl = graphIn.get(elementJoin).iterator().next();
		Set<String> depOfDep = new LinkedHashSet<String>();
		depOfDep.addAll(graphIn.get(elementJoin));
		depOfDep.remove(firstEl);
		Set<String> toTest = new LinkedHashSet<String>();
		toTest.add(firstEl);
		String rootTest = null;
		while (!found) {
			Iterator<String> testIt = toTest.iterator();
			while (testIt.hasNext() && !found) {
				rootTest = testIt.next();
				Iterator<String> it = depOfDep.iterator();
				found = true;
				while (it.hasNext() && found) {
					found = isRootOf(rootTest, it.next());
				}
			}
			if (!found) {
				Set<String> swap = new LinkedHashSet<String>();
				testIt = toTest.iterator();
				while (testIt.hasNext()) {
					swap.addAll(graphIn.get(testIt.next()));
				}
				toTest = swap;
			}

		}
		if(!found){
			rootTest = null;
		}
		return rootTest;
	}

	/**
	 * Check if an element is a root
	 * 
	 * @param rootToTest
	 * @param element
	 * @return <code>true</code> if the element is a root of a part of the DAG
	 *         else <code>false</code>
	 */
	protected boolean isRootOf(String rootToTest, String element) {
		if (!graphIn.containsKey(element)) {
			return false;
		}

		boolean found = graphIn.get(element).contains(rootToTest);

		if (!found) {
			Iterator<String> it = graphIn.get(element).iterator();
			while (it.hasNext() && !found) {
				found = isRootOf(rootToTest, it.next());
			}
		}

		return found;
	}

	/**
	 * Get the first irregular fork of the DAG
	 * 
	 * @return ForkName
	 */
	protected String getFirstIregularFork() {
		Iterator<String> it = elementSorted.iterator();
		String found = null;
		while (it.hasNext() && found == null) {
			String cur = it.next();
			if (!cur.startsWith("fork-")) {
				Set<String> in = graphOut.get(cur);
				if (in != null && in.size() > 1) {
					found = cur;
				}
			}
		}
		return found;
	}

	/**
	 * Get the common leaf of element with a fork
	 * 
	 * @param elementFork
	 * @return name of the common leaf
	 */
	protected String getCommonLeafOf(String elementFork) {
		boolean found = false;
		// Take the first element for root testing
		String firstEl = graphOut.get(elementFork).iterator().next();
		Set<String> nextOfNext = new LinkedHashSet<String>();
		nextOfNext.addAll(graphOut.get(elementFork));
		nextOfNext.remove(firstEl);
		Set<String> toTest = new LinkedHashSet<String>();
		toTest.add(firstEl);
		String leafTest = null;
		while (!found) {
			Iterator<String> testIt = toTest.iterator();
			while (testIt.hasNext() && !found) {
				leafTest = testIt.next();
				Iterator<String> it = nextOfNext.iterator();
				found = true;
				while (it.hasNext() && found) {
					found = isLeafOf(leafTest, it.next());
				}
			}
			if (!found) {
				Set<String> swap = new LinkedHashSet<String>();
				testIt = toTest.iterator();
				while (testIt.hasNext()) {
					swap.addAll(graphOut.get(testIt.next()));
				}
				toTest = swap;
			}
		}
		if(!found){
			leafTest = null;
		}
		return leafTest;
	}

	/**
	 * Is an element a child or sub action of another part of the DAG
	 * 
	 * @param leafToTest
	 * @param element
	 * @return <code>true</code> if the element is a leaf of the another leaf
	 *         else <code>false</code>
	 */
	protected boolean isLeafOf(String leafToTest, String element) {
		if (!graphOut.containsKey(element)) {
			return false;
		}

		boolean found = graphOut.get(element).contains(leafToTest);

		if (!found) {
			Iterator<String> it = graphOut.get(element).iterator();
			while (it.hasNext() && !found) {
				found = isLeafOf(leafToTest, it.next());
			}
		}

		return found;
	}

	/**
	 * Put a join in the DAG after a fork specifying a name for join
	 * 
	 * @param element
	 *            to join at
	 * @param join
	 *            point to add the join after
	 * @param name
	 *            to be added to join to identify join
	 */
	protected void placeForkAfter(String element, String join, String name) {
		String forkName = "fork-" + name;
		Set<String> outN = new LinkedHashSet<String>();
		Set<String> inN = new LinkedHashSet<String>();


		Set<String> outEl = graphOut.get(element);
		Iterator<String> itOut = outEl.iterator();
		while (itOut.hasNext()) {
			String cur = itOut.next();
			if (isLeafOf(join, cur)) {
				outN.add(cur);
			}
		}

		
		if(outN.size() > 1){
			outEl.removeAll(outN);
			outEl.add(forkName);

			inN.add(element);

			Iterator<String> it = outN.iterator();
			while (it.hasNext()) {
				String cur = it.next();
				if (graphIn.get(cur).remove(element)) {
					graphIn.get(cur).add(forkName);
				} else {
					logger.error("Should not happened, graph not well set up");
				}
			}
			graphOut.put(forkName, outN);
			graphIn.put(forkName, inN);
			elementSorted.add(elementSorted.indexOf(element) + 1, forkName);
		}
	}

	/**
	 * Put a join in the DAG before a fork specifying a name for join
	 * 
	 * @param element
	 *            to join at
	 * @param fork
	 *            point ot add the join before
	 * @param name
	 *            to be added to join to identify join
	 */
	protected void placeJoinBefore(String element, String fork, String name) {
		String joinName = "join-" + name;
		Set<String> outN = new LinkedHashSet<String>();
		Set<String> inN = new LinkedHashSet<String>();

		Set<String> inEl = graphIn.get(element);
		Iterator<String> itIn = inEl.iterator();
		while (itIn.hasNext()) {
			String cur = itIn.next();
			if (isRootOf(fork, cur)) {
				inN.add(cur);
			}
		}
		
		if(inN.size() > 1){
			inEl.removeAll(inN);
			inEl.add(joinName);

			outN.add(element);

			Iterator<String> it = inN.iterator();
			while (it.hasNext()) {
				String cur = it.next();
				if (graphOut.get(cur).remove(element)) {
					graphOut.get(cur).add(joinName);
				} else {
					logger.error("Should not happened, graph not well set up");
				}
			}
			graphOut.put(joinName, outN);
			graphIn.put(joinName, inN);
			elementSorted.add(elementSorted.indexOf(element), joinName);
		}
	}

	/**
	 * Move elements after the join
	 * 
	 * @param fork
	 *            point to get actions before that will be moved
	 * @param join
	 *            to move after
	 */
	protected void moveElementAfterJoin(String fork, String join) {
		Set<String> actionBeforeJoin = getAllBefore(join);
		Iterator<String> it = graphOut.get(fork).iterator();
		logger.debug("Before " + join + " " + actionBeforeJoin);
		while (it.hasNext()) {
			String cur = it.next();
			if (isRootOf(cur, join)) {
				moveAfterJoin(cur, join, actionBeforeJoin);
			} else {
				logger.debug(cur + " is not a leaf of " + join);
			}
		}

	}

	/**
	 * Get the set of actions that occur before an element
	 * 
	 * @param element
	 * @return Set of actions before
	 */
	protected Set<String> getAllBefore(String element) {
		Set<String> actionBefore = new LinkedHashSet<String>();
		if (graphIn.containsKey(element)) {
			Iterator<String> itIn = graphIn.get(element).iterator();
			while (itIn.hasNext()) {
				// FIXME possible infinite loop
				String cur = itIn.next();
				actionBefore.add(cur);
				actionBefore.addAll(getAllBefore(cur));
			}
		} else {
			logger.debug(element + " does not have dependencies");
		}
		return actionBefore;
	}

	/**
	 * Set the list of action that happen before the join
	 * 
	 * @param element
	 * @param join
	 * @param actionBeforeJoin
	 */

	protected void moveAfterJoin(String element, String join,
			Set<String> actionBeforeJoin) {
		if (!element.equals(join)) {
			Set<String> elementToMove = new LinkedHashSet<String>();
			Set<String> out = graphOut.get(element);
			Iterator<String> itOut = out.iterator();
			while (itOut.hasNext()) {
				String cur = itOut.next();
				if (!actionBeforeJoin.contains(cur)) {
					if (!cur.equals(join)) {
						elementToMove.add(cur);
					}
				} else {
					moveAfterJoin(cur, join, actionBeforeJoin);
				}
			}

			if (elementToMove.size() > 0 && graphOut.containsKey(join)) {
				if (graphOut.get(join).size() == 1) {
					String el = graphOut.get(join).iterator().next();
					if (!graphOut.containsKey(el)) {
						logger.debug("Remove end element '" + el + "' from "
								+ element);
						graphOut.get(join).remove(el);
						graphIn.get(el).remove(join);
					}
				}
			}
			Iterator<String> moveIt = elementToMove.iterator();
			while (moveIt.hasNext()) {
				String cur = moveIt.next();
				logger.debug("move " + cur + " from " + element + " to " + join);
				graphOut.get(element).remove(cur);
				graphOut.get(join).add(cur);
				graphIn.get(cur).remove(element);
				graphIn.get(cur).add(join);

			}
		}

	}

	/**
	 * Get the Graph Out
	 * 
	 * @return the graphOut
	 */
	public final Map<String, Set<String>> getGraphOut() {
		return graphOut;
	}

}
