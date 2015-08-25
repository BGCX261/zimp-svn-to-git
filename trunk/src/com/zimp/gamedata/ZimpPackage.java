package com.zimp.gamedata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.zimp.GlobalPreferences;

import android.util.Log;

public class ZimpPackage {

	/*
	 * Unzip a particular zip to temporary storage
	 */
	static public boolean unzipToTemp(String zipfilename, String outfolder) {
		return false;		
	}
		
	/*
	 * Create a zip file from temporary storage
	 */
	static public boolean zipFromTemp(String zipfilename, String infolder) {
		
		return false;
	}
	
	/*
	 * Copy a file from 
	 * @infilename      Input file
	 * @filepathportion Copy to @outfolder/@filepathportion 
	 * @outfolder       Outfolder to copy the file to
	 */
	static public boolean copyFile(String infilename, String filepathportion, String outfolder, String filepath) {
		
		File d = new File(outfolder + filepathportion);
		
		// Step 1: Create the directory
		if((d.exists() != true) || (d.isDirectory() != true)) {
			if(d.mkdirs() == false)
				return false;
		}	
		
		// Step 2: Copy the file
		bulkCopyFile(infilename, outfolder + filepath);
		
		return true;
	}
	
	/*
	 * Copy a folder with all included files from 
	 * @infilename      Input file
	 * @filepathportion Copy to @outfolder/@filepathportion 
	 * @outfolder       Outfolder to copy the file to
	 */
	static public boolean copyFolder(String infolder, String filepathportion, String outfolder) {
		return false;
	}
	
	/*
	 * Check directory exists on sdcard, if not create it
	 * returns false - newly created
	 * returns true  - already existed
	 */
	static public boolean checkmakeDirectory(String directory) {
		File f = new File(directory);
		if((f.exists() == true) && (f.isDirectory() == true) ) {
			return true;
		}
		
		boolean res = f.mkdirs();
		return false;
	}
	
	/*
	 * Check if file exists
	 * returns true - file exists
	 * returns false - file does not exist
	 */
	static public boolean fileExists(String filename) {
		File f = new File(filename);
		if((f.exists() == true) && (f.isFile() == true)) {
			return true;
		}
		return false;
	}
	
	// Copies a single file from the data partition to another location
	static private void bulkCopyFile(String infile, String outfile) {

//		globalPreferences.fileAddToPreferences(filename);
		
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(infile);
			out = new FileOutputStream(outfile);

			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;
			Log.d(GlobalPreferences.TAG, "Copied File : " + outfile);
			
		} catch (Exception e) {
			Log.e(GlobalPreferences.TAG, e.getMessage());
		}
	}	
	
	/* 
	 * Zip a particular folder into the destination file
	 */
	static public void zipFolder(String srcFolder, String destZipFile) throws Exception {
	    ZipOutputStream zip = null;
	    FileOutputStream fileWriter = null;

	    fileWriter = new FileOutputStream(destZipFile);
	    zip = new ZipOutputStream(fileWriter);

	    addFolderToZip("", srcFolder, zip);
	    zip.flush();
	    zip.close();
	  }
	
	static private void addFileToZip(String path, String srcFile, ZipOutputStream zip)
	      throws Exception {

	    File folder = new File(srcFile);
	    if (folder.isDirectory()) {
	      addFolderToZip(path, srcFile, zip);
	    } else {
	      byte[] buf = new byte[1024];
	      int len;
	      FileInputStream in = new FileInputStream(srcFile);
	      zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
	      while ((len = in.read(buf)) > 0) {
	        zip.write(buf, 0, len);
	      }
	    }
	  }

	static private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip)
	  throws Exception {
	    File folder = new File(srcFolder);

	    for (String fileName : folder.list()) {
	      if (path.equals("")) {
	        addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
	      } else {
	        addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip);
	      }
	    }
	  }
	
}


