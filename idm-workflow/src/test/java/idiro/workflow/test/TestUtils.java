package idiro.workflow.test;

import java.util.Arrays;

import org.apache.log4j.Logger;

public class TestUtils {

	static protected Logger logger = Logger.getLogger(TestUtils.class);
	
	static private String user = System.getProperty("user.name");
	
	static public void logTestTitle(String title){
		logTestTitle(title,80,'#');
	}

	static public void logTestTitle(String title, int lineSize){
		logTestTitle(title,lineSize,'#');
	}

	static public void logTestTitle(String title, char repeatChar){
		logTestTitle(title,80,repeatChar);
	}

	static public void logTestTitle(String title, int lineSize, char repeatChar){
		int halfLine = (lineSize - title.length()) / 2;
		String message = "";
		if(halfLine > 0){
			if(lineSize - title.length() % 2 == 1){
				message += repeatChar;
			}
			char[] sharps = new char[halfLine];
			Arrays.fill(sharps, repeatChar);
			String repeatStr = new String(sharps);
			message += repeatStr + title + repeatStr;
		}else{
			message += title;
		}
		logger.debug(message);
	}
	
	static public String getTableName(int id){
		return "test_idm_"+user+id;
	}
	
	static public String getTablePath(int id){
		return "/"+getTableName(id);
	}
}
