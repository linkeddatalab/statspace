package tuwien.ldlab.statspace.model.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFolder {
	private static ArrayList<String> filesListInDir = new ArrayList<String>();	
	
	public static void doZip(String sFolder, String sZipFile) {     
	        File dir = new File(sFolder);
	        zipDirectory(dir, sZipFile);
	}
	 
	private static void zipDirectory(File dir, String zipDirName) {
        try {
            populateFilesList(dir);
            
            //now zip files one by one
            //create ZipOutputStream to write to the zip file
            FileOutputStream fos = new FileOutputStream(zipDirName);
            ZipOutputStream zos = new ZipOutputStream(fos);
            for(String filePath : filesListInDir){               
                //for ZipEntry we need to keep only relative file path, so we used substring on absolute path
            	String sPath= dir.getName()+ File.separator + filePath.substring(dir.getAbsolutePath().length()+1, filePath.length());
               // ZipEntry ze = new ZipEntry(filePath.substring(dir.getAbsolutePath().length()+1, filePath.length()));
                ZipEntry ze = new ZipEntry(sPath);
                zos.putNextEntry(ze);
                //read the file and write to ZipOutputStream
                FileInputStream fis = new FileInputStream(filePath);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                zos.closeEntry();
                fis.close();
            }
            zos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        filesListInDir.clear();
    }
	
	private static void populateFilesList(File dir) throws IOException {
	        File[] files = dir.listFiles();
	        for(File file : files){
	            if(file.isFile())  filesListInDir.add(file.getAbsolutePath());	            
	            else {
	            	if(file.getName().equals("metadata_area")) continue;
	            	populateFilesList(file);
	            }
	        }
	 }
}
