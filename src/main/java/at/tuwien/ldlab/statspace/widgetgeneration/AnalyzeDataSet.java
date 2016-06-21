/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.tuwien.ldlab.statspace.widgetgeneration;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import at.tuwien.ldlab.statspace.metadata.MetaDataForSPARQL;
import at.tuwien.ldlab.statspace.util.*;
import java.util.*;

public class AnalyzeDataSet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private String folderDownload = "";
	private String folderTemplate = "";
	private static Log log = LogFactory.getLog(AnalyzeDataSet.class);		

	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {		

		String folderWebApp, folderWidgetCache, folderId, sEndpointForQuery, sId, dsName;
		int i, j, n;		
		boolean bEndpoint, bDataset;
		File fId, fEndpoint;	 
		
		folderWebApp =  getServletContext().getRealPath("/");	
		sId= request.getParameter("idRequest");
      	Object objRequest = request.getServletContext().getAttribute(sId);
      	
        if(!sId.isEmpty() && objRequest!=null){      		
        	int idRequest = Integer.parseInt(sId);
         	Request req = (Request) objRequest;   
           	Endpoint endpoint = req.getEndpoint();
            Boolean bMetaDataPurpose = req.getMetaDataPurpose();
         	
         	String names = request.getParameter("chkValue");
         	String[] valueList = names.split(";");    	
         	List<String> list = Arrays.asList(valueList); 
         	n = list.size();
         	try{
     	    	if(n>0){  	    		
     	    		String sErrorList="";     	    	    		
     	    		
     	    		//create metadata
     	    		if(bMetaDataPurpose){
     	    			MetaDataForSPARQL md = new MetaDataForSPARQL(endpoint, list, folderWebApp, sId);
     	    			md.analyzeEndpoint();
     	    			md.createMetaData();
     	    			
     	    			sEndpointForQuery = endpoint.getEndpointForQuery();       	   
     	    			sEndpointForQuery = Support.extractFolderName(sEndpointForQuery); 
         	        	folderId =  folderWebApp  + "download" + File.separator +  sEndpointForQuery + "_" + idRequest;  
         	        	
 	    			}
     	    		//create widget
     	    		else{     	    			
     	    			folderDownload 	  = folderWebApp + "download";		
     	    			folderWidgetCache = folderWebApp.substring(0, folderWebApp.length()-1) + "_cache" + File.separator + "widget";     	    	
     	    			folderTemplate 	  = folderWidgetCache + File.separator + "template";			
     	    			
     	    			//create a folder to store this sparql endpoint if it has not analyzed previously 
         	    		sEndpointForQuery = endpoint.getEndpointForQuery();       	    	 	    		
         	    		sEndpointForQuery = Support.extractFolderName(sEndpointForQuery); 
         	        	folderId = folderDownload + File.separator + sEndpointForQuery + "_" + idRequest;	    		
         				fId = new File(folderId);
         				fId.mkdir();  		
         	    		
     	    			bEndpoint = FileOperation.findFile(folderWidgetCache, sEndpointForQuery);
 	     	    		if(bEndpoint == false){
 	     	    			fEndpoint = new File(folderWidgetCache + File.separator + sEndpointForQuery);
 	         	    		fEndpoint.mkdir(); 	       
 	         	    		
 	         	    		//add this endpoint to list	         	      	
 	         	      		File fList =new File(folderWidgetCache + File.separator + "list.csv");
 	         	      		if(!fList.exists()){
 	         	      			fList.createNewFile();
 	         	      		}     	      		
 	         	      		FileWriter fWriter = new FileWriter(folderWidgetCache + File.separator + "list.csv",true);
         	      	        BufferedWriter buf = new BufferedWriter(fWriter);
         	      	        buf.write(endpoint.getEndpointForQuery());
         	      	        buf.newLine();
         	      	        buf.close(); 	         	      	
 	     	    		}
 	    				for(i=0; i<n; i++){
	     	    			j = Integer.parseInt(list.get(i));
	     		    		dsName = endpoint.getDataSet(j).getUri();	     		    		
	     		    		dsName = Support.extractFileName(dsName);	
	     		    		bDataset = FileOperation.findFile(folderWidgetCache + File.separator + sEndpointForQuery, dsName + ".html");     	
	     		    		
	 		    			if(bEndpoint && bDataset){
	 		    				FileOperation.copyFolder(folderWidgetCache + File.separator + sEndpointForQuery + File.separator + dsName+".html",
	 		    						folderId + File.separator + dsName+".html");
	 		    			}else{
	 		    				endpoint.getDataSet(j).queryComponent(endpoint.getEndpointForQuery(), endpoint.getHTTP(), endpoint.getUseDistict());
	     		     	     	endpoint.getDataSet(j).queryValue(endpoint.getEndpointForQuery(), endpoint.getHTTP(), endpoint.getFindOther(), endpoint.getRemove());	
	     		    			Widget widget = new Widget(endpoint.getDataSet(j), endpoint.getEndpointForWidget(), folderId, folderTemplate);	
	     		    			widget.createWidgetFile();
	     		    			
	     		    			//copy this dataset to folder of its endpoint
	 	     		    		FileOperation.copyFolder(folderId + File.separator + dsName+".html", 
	 	     		    					folderWidgetCache + File.separator + sEndpointForQuery + File.separator + dsName+".html");
		     		    			
	 	     		    		if(endpoint.getDataSet(j).getDimensionSize()==0 || endpoint.getDataSet(j).getMeasureSize()==0 || endpoint.getDataSet(j).getDimension().allEmpty()){
	 	     		    			log.debug("Not found value at "+ endpoint.getDataSet(j).getUri());	 	     		    			
	 	     		    			if(sErrorList.isEmpty())
	 	     		    				sErrorList = endpoint.getDataSet(j).getUri();
	 	     		    			else
	 	     		    				sErrorList = sErrorList + "<br>" + endpoint.getDataSet(j).getUri();
	 	     		    		}
	 		    			}
	     	    			
	     		    	}
 	    			}     	 
     	    		
     	    		ZipFolder.doZip(folderId, folderId+".zip");     	    		
     	    	 	
     	    		//store download link to array Request, then set into session
     	    		req.setDownload(folderId + ".zip");
     	    		request.getServletContext().removeAttribute(sId);
     	    		request.getServletContext().setAttribute(sId,req);
     	    		
     	    		//redirect to download page     	    		
     	    		if(!sErrorList.isEmpty())
     	    			request.setAttribute("errorList", sErrorList);     	    		
     	    		request.setAttribute("idRequest", idRequest);
     				RequestDispatcher view = request.getRequestDispatcher("/generation/download.jsp");
     				view.forward(request, response);
     				  	    			    		
     	    	}
         	}
         	catch(Exception e){           
                log.info(e.toString());
                request.setAttribute("errorPage", e.toString());
  				RequestDispatcher view = request.getRequestDispatcher("/generation/error.jsp");
  				view.forward(request, response);  	
         	}
         }else{
        	 request.setAttribute("errorPage", "This session is not authenticated");
        	 RequestDispatcher view = request.getRequestDispatcher("/generation/error.jsp");
        	 view.forward(request, response);  	
         }
    }
}
