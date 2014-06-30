package idiro.workflow.server;

import static org.junit.Assert.assertTrue;
import idiro.workflow.test.TestUtils;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.workflow.server.OozieDag;

public class OozieDagTests {

	protected Logger logger = Logger.getLogger(getClass());


	@Test
	public void basic(){
		int i = 0;
		TestUtils.logTestTitle("OozieDagTests#basic");
		{
			OozieDag dag = new OozieDag();
			dag.addLink("start", "act_1");
			dag.addLink("act_1", "end");
			dag.transform();
			
			List<String> sortedEl = new LinkedList<String>();
			sortedEl.add("start");
			sortedEl.add("act_1");
			sortedEl.add("end");
			logger.debug(dag.elementSorted);
			assertTrue(i+" elementNotSorted",sortedEl.equals(dag.elementSorted));
			
			Map<String,Set<String>> inG = new LinkedHashMap<String,Set<String>>();
			Set<String> set = new LinkedHashSet<String>();
			set.add("start");
			inG.put("act_1", set);
			set = new LinkedHashSet<String>();
			set.add("act_1");
			inG.put("end", set);
			logger.debug(dag.graphIn);
			assertTrue(i+" in map incorrect",inG.equals(dag.graphIn));
			
			Map<String,Set<String>> outG = new LinkedHashMap<String,Set<String>>();
			set = new LinkedHashSet<String>();
			set.add("act_1");
			outG.put("start", set);
			set = new LinkedHashSet<String>();
			set.add("end");
			outG.put("act_1", set);
			logger.debug(dag.getGraphOut().toString());
			logger.debug(dag.getGraphOut().toString());
			assertTrue(i+" out map incorrect",outG.equals(dag.graphOut));
		}
		++i;
		//1
		TestUtils.logTestTitle(Integer.toString(i));
		{
			OozieDag dag = new OozieDag();
			dag.addLink("start", "act_1");
			dag.addLink("act_1", "act_2");
			dag.addLink("act_2", "end");
			dag.transform();
			
			List<String> sortedEl = new LinkedList<String>();
			sortedEl.add("start");
			sortedEl.add("act_1");
			sortedEl.add("act_2");
			sortedEl.add("end");
			logger.debug(dag.elementSorted);
			assertTrue(i+" elementNotSorted",sortedEl.equals(dag.elementSorted));
			
			Map<String,Set<String>> inG = new LinkedHashMap<String,Set<String>>();
			Set<String> set = new LinkedHashSet<String>();
			set.add("start");
			inG.put("act_1", set);
			set = new LinkedHashSet<String>();
			set.add("act_1");
			inG.put("act_2", set);
			set = new LinkedHashSet<String>();
			set.add("act_2");
			inG.put("end", set);
			logger.debug(dag.graphIn);
			assertTrue(i+" in map incorrect",inG.equals(dag.graphIn));
			
			Map<String,Set<String>> outG = new LinkedHashMap<String,Set<String>>();
			set = new LinkedHashSet<String>();
			set.add("act_1");
			outG.put("start", set);
			set = new LinkedHashSet<String>();
			set.add("act_2");
			outG.put("act_1", set);
			set = new LinkedHashSet<String>();
			set.add("end");
			outG.put("act_2", set);
			logger.debug(dag.getGraphOut().toString());
			assertTrue(i+" out map incorrect",outG.equals(dag.graphOut));
		}
		++i;
		//2
		TestUtils.logTestTitle(Integer.toString(i));
		{
			OozieDag dag = new OozieDag();
			dag.addLink("start", "act_1");
			dag.addLink("act_1", "act_2");
			dag.addLink("act_1", "act_3");
			dag.addLink("act_2", "end");
			dag.addLink("act_3", "end");
			dag.transform();
			
			Map<String,Set<String>> inG = new LinkedHashMap<String,Set<String>>();
			Set<String> set = new LinkedHashSet<String>();
			set.add("start");
			inG.put("act_1", set);
			set = new LinkedHashSet<String>();
			set.add("fork_pair_end");
			inG.put("act_2", set);
			inG.put("act_3", set);
			set = new LinkedHashSet<String>();
			set.add("join_end");
			inG.put("end", set);
			set = new LinkedHashSet<String>();
			set.add("act_1");
			inG.put("fork_pair_end", set);
			set = new LinkedHashSet<String>();
			set.add("act_2");
			set.add("act_3");
			inG.put("join_end", set);
			logger.debug(dag.graphIn);
			assertTrue(i+" in map incorrect",inG.equals(dag.graphIn));
			
			Map<String,Set<String>> outG = new LinkedHashMap<String,Set<String>>();
			set = new LinkedHashSet<String>();
			set.add("act_1");
			outG.put("start", set);
			set = new LinkedHashSet<String>();
			set.add("fork_pair_end");
			outG.put("act_1", set);
			set = new LinkedHashSet<String>();
			set.add("join_end");
			outG.put("act_2", set);
			outG.put("act_3", set);
			set = new LinkedHashSet<String>();
			set.add("act_2");
			set.add("act_3");
			outG.put("fork_pair_end", set);
			set = new LinkedHashSet<String>();
			set.add("end");
			outG.put("join_end", set);
			logger.debug(outG.toString());
			logger.debug(dag.getGraphOut().toString());
			assertTrue(i+" out map incorrect",outG.equals(dag.graphOut));
		}
		++i;
		//3
		TestUtils.logTestTitle(Integer.toString(i));
		{
			OozieDag dag = new OozieDag();
			dag.addLink("start", "act_1");
			dag.addLink("act_1", "act_2");
			dag.addLink("act_2", "act_3");
			dag.addLink("act_3", "act_4");
			dag.addLink("act_2", "del_1");
			dag.addLink("act_3", "del_2");
			dag.addLink("act_4", "del_3");
			dag.addLink("del_1", "end");
			dag.addLink("del_2", "end");
			dag.addLink("del_3", "end");
			dag.transform();
			logger.debug(dag.getGraphOut().toString());
		}
		++i;
		//4
		TestUtils.logTestTitle(Integer.toString(i));
		{
			OozieDag dag = new OozieDag();
			dag.addLink("start", "1");
			dag.addLink("start", "2");
			dag.addLink("1", "3");
			dag.addLink("2", "3");
			dag.addLink("start", "5");
			dag.addLink("3", "4");
			dag.addLink("5", "4");
			dag.addLink("4", "end");
			dag.transform();
			logger.debug(dag.getGraphOut().toString());
		}
		++i;
		//5
		TestUtils.logTestTitle(Integer.toString(i));
		{
			OozieDag dag = new OozieDag();
			dag.addLink("start", "1");
			dag.addLink("start", "2");
			dag.addLink("start", "3");
			dag.addLink("start", "4");
			dag.addLink("1", "5");
			dag.addLink("2", "5");
			dag.addLink("3", "6");
			dag.addLink("4", "6");
			dag.addLink("5", "7");
			dag.addLink("6", "7");
			dag.addLink("7", "end");
			dag.transform();
			logger.debug(dag.getGraphOut().toString());
		}
		++i;
		//6
		TestUtils.logTestTitle(Integer.toString(i));
		{
			OozieDag dag = new OozieDag();
			dag.addLink("start", "1");
			dag.addLink("start", "2");
			dag.addLink("1", "3");
			dag.addLink("2", "4");
			dag.addLink("3", "5");
			dag.addLink("3", "6");
			dag.addLink("4", "6");
			dag.addLink("4", "7");
			dag.addLink("5", "8");
			dag.addLink("7", "8");
			dag.addLink("6", "end");
			dag.addLink("8", "end");
			dag.transform();
			logger.debug(dag.getGraphOut().toString());
		}
		assertTrue(true);
	}
}
