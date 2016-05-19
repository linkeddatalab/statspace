package tuwien.ldlab.statspace.model.widgetgeneration;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import javax.servlet.ServletContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WidgetDeletion implements Runnable{
	/**
	 * 
	 */
	private static Log log = LogFactory.getLog(WidgetDeletion.class);
	private static String folder_download="";	
	private static ServletContext servlet_context;
	
	public WidgetDeletion (String sFolder, ServletContext sc){
		folder_download = sFolder;
		servlet_context = sc;
		Enumeration<String> names = sc.getAttributeNames();
		log.info(names.toString());		
	}
	
	@Override
	public void run() {	
		File directory = new File(folder_download);
    	if(!directory.exists()){
           log.info("Directory does not exist.");           
           System.exit(0); 
        }else{ 
           try{ 
               //delete(directory);
        	   String files[] = directory.list();	 
        	   for (String temp : files) {
        	      //construct the file structure
        	      File fileDelete = new File(directory, temp);	 
        	      //recursive delete
        	     delete(fileDelete);
        	   }	
           }catch(IOException e){
               e.printStackTrace();
               System.exit(0);
           }
        }    	
	}	
	public static void delete(File file)throws IOException{	    	
		if(file.isDirectory()){			
			String sname = file.getName();
			if(sname.compareToIgnoreCase("list_endpoint")!=0){			
				Date d1 = new Date(file.lastModified());
				Date d2 = new Date();				
				long diff = d2.getTime() - d1.getTime();
				long diffDays = diff /(24 * 60 * 60 * 1000);
				if(diffDays>=1){			
		    		//directory is empty, then delete it
		    		if(file.list().length==0){	 
		    		   file.delete();	 
		    		}else{	 
		    		   //list all the directory contents
		        	   String files[] = file.list();	 
		        	   for (String temp : files) {
		        	      //construct the file structure
		        	      File fileDelete = new File(file, temp);	 
		        	      //recursive delete
		        	     delete(fileDelete);
		        	   }		 
		        	   //check the directory again, if empty then delete it
		        	   if(file.list().length==0){
		           	     file.delete();	
		           	     
		           	     //get Id of folder
		           	     String name = file.getName();		           	   
		           	     int i = name.length()-1;
		           	     while(i>=0 && name.charAt(i)!='_') i--;
		           	     String id = name.substring(i+1);		           	     
		           	     if(servlet_context.getAttribute(id)!=null){
		           	    	servlet_context.removeAttribute(id);
		           	    	log.info("Removed idRequest " + id);
		           	     }else
		           	    	 log.info("Can not find " + id);
		        	   }
		    		} 
				}
			}
    	}else{
    		//if file, delete it  
    		Date d1 = new Date(file.lastModified());
			Date d2 = new Date();				
			long diff = d2.getTime() - d1.getTime();
			long diffDays = diff /(24* 60 * 60 * 1000);
//			long diffDays = diff /(20 * 1000);
			if(diffDays>=1)
				file.delete();			
    	}
    }
}
