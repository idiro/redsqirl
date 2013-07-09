package idiro.workflow.server;

import static org.junit.Assert.assertTrue;
import idiro.workflow.test.TestUtils;

import org.apache.log4j.Logger;
import org.junit.Test;

public class OozieDagTests {

	protected Logger logger = Logger.getLogger(getClass());


	@Test
	public void basic(){
		TestUtils.logTestTitle("OozieDagTests#basic");
		{
			OozieDag dag = new OozieDag();
			dag.addLink("start", "act_1");
			dag.addLink("act_1", "end");
			dag.transform();
			logger.debug(dag.elementSorted);
			logger.debug(dag.graphIn);
			logger.debug(dag.getGraphOut().toString());
		}
		TestUtils.logTestTitle("1");
		{
			OozieDag dag = new OozieDag();
			dag.addLink("start", "act_1");
			dag.addLink("act_1", "act_2");
			dag.addLink("act_2", "end");
			dag.transform();
			logger.debug(dag.getGraphOut().toString());
		}
		TestUtils.logTestTitle("2");
		{
			OozieDag dag = new OozieDag();
			dag.addLink("start", "act_1");
			dag.addLink("act_1", "act_2");
			dag.addLink("act_1", "act_3");
			dag.addLink("act_2", "end");
			dag.addLink("act_3", "end");
			dag.transform();
			logger.debug(dag.getGraphOut().toString());
		}
		TestUtils.logTestTitle("3");
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
		TestUtils.logTestTitle("4");
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
		TestUtils.logTestTitle("5");
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
		TestUtils.logTestTitle("6");
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
