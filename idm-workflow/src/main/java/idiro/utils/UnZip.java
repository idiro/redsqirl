package idiro.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

public class UnZip {

	private Logger logger = Logger.getLogger(getClass());

	/**
	 * Unzip it
	 * @param zipFile input zip file
	 * @param output zip file output folder
	 */
	public void unZipIt(File zipFile, File outputFolder){

		byte[] buffer = new byte[1024];

		try{

			if(!outputFolder.exists()){
				outputFolder.mkdirs();
			}

			//get the zip file content
			ZipInputStream zis = 
					new ZipInputStream(new FileInputStream(zipFile));
			//get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			while(ze!=null){

				String fileName = ze.getName();
				File newFile = new File(outputFolder, fileName);

				logger.debug("file unzip : "+ newFile.getAbsoluteFile());

				if(ze.isDirectory()){
					newFile.mkdirs();
				}else{
					//create all non exists folders
					//else you will hit FileNotFoundException for compressed folder
					newFile.getParentFile().mkdirs();
					FileOutputStream fos = new FileOutputStream(newFile);             

					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}

					fos.close();   
				}
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			zis.close();

		}catch(IOException ex){
			ex.printStackTrace(); 
		}
	}    
}
