package com.redsqirl.workflow.test;

import org.apache.log4j.Logger;
import org.junit.Test;

public class StringTests {
	
	private Logger logger = Logger.getLogger(getClass());
	
	@Test
	public void SplitTest(){
		String a = "a45_source.A";
		String[] result = a.split("\\.");
		logger.info(result.length);
	}
	@Test
	public void ReplaceTest(){
		String a = "a45_source.A";
		String result = a.replace(".","::");
		logger.info(result);
	}
	@Test
	public void TrimEndsWith(){
		String expr ="SUM(A)";
		logger.info(expr.trim().toUpperCase());
	}
	@Test
	public void RegexTest(){
		String expr= "SUM(A)";
		logger.info(expr.contains("\\b(\\b)"));
		logger.info(expr.contains("\\b)"));
	}
	

}
