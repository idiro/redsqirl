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
			if(fileSystem.listStatus(p).length>0){
				logger.warn("File " + p.toString() + " already exists");
				return;
			}
		}else{
			fileSystem.mkdirs(p);
		}

		// Create a new file and write data to it.
		
		
		FSDataOutputStream out = fileSystem.create(new Path(p, "part-0000"));
		out.write(containt.getBytes());
		out.close();
		fileSystem.close();
	}
	
	public static void writeContent(Path p ,String file, String content) throws IOException{
		FileSystem fs = NameNodeVar.getFS();
		
		if(fs.exists(p)){
			FSDataOutputStream out = fs.create(new Path(p, file));
			out.write(content.getBytes());
			out.close();
			fs.close();
		}
	}
	
	public static void createHFDSdir(String path ) throws IOException{
		FileSystem fs = NameNodeVar.getFS();
		Path p = new Path(path);
		if(fs.exists(p)){
			logger.warn("Dir " + p.toString() + " already exists");
			return;
		}
		fs.mkdirs(p);
		fs.close();
	}
	
	public static int getPathSize(Path p) throws IOException{
		FileSystem fs  = NameNodeVar.getFS();
		int psize = 0;
		if (fs.exists(p)){
			psize = fs.listStatus(p).length;
		}
		
		return psize;
		
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
