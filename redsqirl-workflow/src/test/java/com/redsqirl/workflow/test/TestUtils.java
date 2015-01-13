package com.redsqirl.workflow.test;

import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import com.idiro.hadoop.NameNodeVar;

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
		logger.info(message);
	}
	
	static public String getTableName(int id){
		return "test_redsqirl_"+user+id;
	}
	
	static public String getTablePath(int id){
		return "/"+getTableName(id);
	}
	
	static public String getPath(int id) {
		return "/user/" + user + "/tmp/test_redsqirl_" + id;
	}
	
	public static void createStringIntIntfile(Path p) throws IOException {
		String content = "A\0012\0013\n";
		content += "B\0013\0014\n";
		content += "C\0014\0015\n";

		createHDFSFile(p, content);
	}
	
	public static void createHDFSFile(Path p, String containt) throws IOException {
		FileSystem fileSystem = NameNodeVar.getFS();

		// Check if the file already exists
		if (fileSystem.exists(p)) {
			logger.warn("File " + p.toString() + " already exists");
			return;
		}

		// Create a new file and write data to it.
		fileSystem.mkdirs(p);
		FSDataOutputStream out = fileSystem.create(new Path(p, "part-0000"));
		out.write(containt.getBytes());
		out.close();
		fileSystem.close();
	}
	
}