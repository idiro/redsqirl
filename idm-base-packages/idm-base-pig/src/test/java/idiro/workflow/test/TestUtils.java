package idiro.workflow.test;

import idiro.hadoop.NameNodeVar;

import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

public class TestUtils {

	static protected Logger logger = Logger.getLogger(TestUtils.class);

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
	
	public void createHDFSFile(Path p, String containt) throws IOException{
        FileSystem fileSystem = NameNodeVar.getFS();
           
        // Check if the file already exists
        if (fileSystem.exists(p)) {
            logger.warn("File " + p.toString() + " already exists");
            return;
        }

        // Create a new file and write data to it.
        fileSystem.mkdirs(p);
        FSDataOutputStream out = fileSystem.create(new Path(p,"part-0000")); 
        out.write(containt.getBytes());
        out.close();
        fileSystem.close();
    }
}
