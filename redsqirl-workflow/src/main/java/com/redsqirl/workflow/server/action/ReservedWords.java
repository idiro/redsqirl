package com.redsqirl.workflow.server.action;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

import org.apache.log4j.Logger;

public class ReservedWords {
	
	private static Logger logger = Logger.getLogger(ReservedWords.class);
	private static ReservedWords instance;
	private static final String keywordFileName = "reserved_keywords.txt";
	
	private HashSet<String> keywords = null;
	
	private static ReservedWords getInstance(){
		if(instance == null){
			instance = new ReservedWords();
		}
		return instance;
	}
	
	private ReservedWords(){
		keywords = new HashSet<String>(875);
		try{
			InputStream in = getClass().getResourceAsStream("/"+keywordFileName ); 
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line ="";
			while ((line = reader.readLine()) != null) {
				keywords.add(line);
			}
			reader.close();
		}catch(Exception e){
			logger.warn(e,e);
		}
	}
	
	public static boolean isReservedWord(String word){
		return word == null ? true: getInstance().keywords.contains(word.toLowerCase());
	}
}
