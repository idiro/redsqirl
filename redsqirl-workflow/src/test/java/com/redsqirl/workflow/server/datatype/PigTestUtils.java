package com.redsqirl.workflow.server.datatype;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import com.idiro.hadoop.NameNodeVar;

public class PigTestUtils {

	private static Logger logger = Logger.getLogger(PigTestUtils.class);
	
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
	
	public static void createStringIntString_text_file(Path p) throws IOException {
		String content = "A|1|A\n";
		content += "B|2|B\n";
		content += "C|3|C\n";
		content += "D|4|D\n";
		content += "E|5|E\n";
		content += "F|6|F\n";
		content += "G|7|G\n";

		createHDFSFile(p, content);
	}
	
	public static void createStringIntString_ctrl_a_file(Path p) throws IOException {
		String content = "A\0011\001A\n";
		content += "B\0012\001B\n";
		content += "C\0013\001C\n";
		content += "D\0014\001D\n";
		content += "E\0015\001E\n";
		content += "F\0016\001F\n";
		content += "G\0017\001G\n";

		createHDFSFile(p, content);
	}
}
