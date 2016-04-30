package tuwien.ldlab.statspace.model.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class FileOperation {
	
	public static void copyFolder(String sSource, String sDestination)
    {	
    	File srcFolder = new File(sSource);
    	File destFolder = new File(sDestination);
 
    	//make sure source exists
    	if(!srcFolder.exists()){ 
    		System.out.println("Directory does not exist.");          
        }else{        
           try{       	   
        	   doCopy(srcFolder,destFolder);
           }catch(IOException e){
        	   e.printStackTrace();        	
           }
        }    	
    }
	
	public static boolean findFolder(String sParent, String sname){
		File fParent = new File(sParent);
		if(fParent.isDirectory()){
			String files[] = fParent.list();	
    		for (String file : files) {
    		  if(file.equals(sname))
    			  return true;
    		} 
		}
		return false;
	}
	
	public static boolean findFile(String sParent, String sname){
		File fParent = new File(sParent);
		if(fParent.isDirectory()){
			String files[] = fParent.list();	
    		for (String file : files) {
    		  if(file.equals(sname))
    			  return true;
    		} 
		}
		return false;
	}
 
    public static void doCopy(File src, File dest)
    	throws IOException{
 
    	if(src.isDirectory()){ 
    		
    		//if directory not exists, create it
    		if(!dest.exists()){
    		   dest.mkdir();    		   
    		} 
    		//list all the directory contents
    		String files[] = src.list(); 
    		for (String file : files) {
    		   //construct the src and dest file structure
    		   File srcFile = new File(src, file);
    		   File destFile = new File(dest, file);
    		   //recursive copy
    		   doCopy(srcFile, destFile);
    		} 
    	}else{
    		//if file, then copy it
    		//Use bytes stream to support all file types
    		InputStream in = new FileInputStream(src);
    	    OutputStream out = new FileOutputStream(dest);  
    	    byte[] buffer = new byte[1024]; 
	        int length;
	        //copy the file content in bytes 
	        while ((length = in.read(buffer)) > 0){
	    	   out.write(buffer, 0, length);
	        } 
	        in.close();
	        out.close();	        
    	}
    }
    
    public static void readFileIndex(String file_dest, String file_template, 
			String sTitle, String sId, String sSparql, String sQuery, String sSize, String sRowsi, String sCComponent, String sDComponent, String sBody, String sBody_Dimension, String sBody_Measure){
		
		try{			  	
	    	//read index template and copy to destination file
	    	   	
			FileOutputStream f_out = new FileOutputStream(file_dest);	    	
			FileInputStream f_in   = new FileInputStream(file_template);
	  
	        OutputStreamWriter out = new OutputStreamWriter(f_out, "UTF-8");
	     	InputStreamReader in = new InputStreamReader(f_in, "UTF-8");
	        
			BufferedWriter bw = new BufferedWriter(out);
			BufferedReader br = new BufferedReader (in);
			
			String strLine;
			
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {	
				if(strLine.contains("INPUT_Title")) 		strLine = "\t" + "<title>"+ sTitle +"</title>";
				else if(strLine.contains("INPUT_2Title")) 	strLine = "\t\t\t" + "'label': '"+ sTitle +"',";
				else if(strLine.contains("INPUT_Id")) 	strLine = "\t\t\t" +  sId ;
				else if(strLine.contains("INPUT_Sparql")) 	strLine = "\t\t\t" + "url: " + sSparql +",";	
				else if(strLine.contains("INPUT_2Sparql"))					
					strLine = "\t\t\t\t" + "var url = \"http://linkedwidgets.org/statisticaldata/sparql?\""+ 
							"+\"endpoint=\"+" + sSparql +"+\"&query=\"+ squery;";							
				else if(strLine.contains("INPUT_3Sparql")) 	{
					if(!sSparql.equalsIgnoreCase("\"http://cofog01.data.scotland.gov.uk/sparql\""))
						strLine = "\t\t\t\t\t\t" + "var url = " + sSparql + " + \"?query=\" + squery + \"&format=application%2Fsparql-results%2Bjson\";";
					else
						strLine = "\t\t\t\t\t\t" + "var url = " + sSparql + " + \".json?_per_page=20&query=\" + squery;";
				}
				else if(strLine.contains("INPUT_Query"))  	strLine = "\t\t" + sQuery;							
				else if(strLine.contains("INPUT_Rowsi")) 	strLine =  sRowsi;
				else if(strLine.contains("INPUT_CComponent")) 	strLine = sCComponent;	
				else if(strLine.contains("INPUT_DComponent")) 	strLine = sDComponent;
				else if(strLine.contains("INPUT_Body")) 	strLine = sBody;
				else if(strLine.contains("INPUT_Dimension_Body")) 	strLine = sBody_Dimension;	
				else if(strLine.contains("INPUT_Measure_Body")) 	strLine = sBody_Measure;	
				
				bw.write(strLine);
				bw.newLine();
			}
			// Close the input stream
			br.close();
			bw.close();			
	        	    
	        
    	}catch(Exception e){           
            e.printStackTrace();
    	}
	}
	
}
