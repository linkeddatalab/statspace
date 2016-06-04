/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tuwien.ldlab.statspace.controller.widgetgeneration;
import tuwien.ldlab.statspace.model.metadata.MetaDataForSPARQL;
import tuwien.ldlab.statspace.model.util.*;
import tuwien.ldlab.statspace.model.widgetgeneration.*;
import java.io.File;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.*;

public class AnalyzeDataSet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String folderDownload = "";
	private String folderTemplate = "";
	private static Log log = LogFactory.getLog(AnalyzeDataSet.class);		

	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
		
//		log.info("Calling AnalyzeDataSet class");		
		
		String folder, folderList, folderId, sEndpointForQuery, sId, dsName;
		int i, j, n;		
		boolean bEndpoint, bDataset;
		File fId, fEndpoint;	 
		
		folder =  getServletContext().getRealPath("/");	
		sId= request.getParameter("idRequest");
      	Object objRequest = request.getServletContext().getAttribute(sId);
      	
        if(! sId.isEmpty() && objRequest!=null){  
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
     	    		Date d1 = new Date();     	    		
     	    		
     	    		//create metadata
     	    		if(bMetaDataPurpose){
     	    			MetaDataForSPARQL md = new MetaDataForSPARQL(endpoint, list, folder, sId);
     	    			md.analyzeEndpoint();
     	    			md.createMetaData();
     	    			
     	    			sEndpointForQuery = endpoint.getEndpointForQuery();       	   
     	    			sEndpointForQuery = Support.removeSpecialCharacterInFileName(sEndpointForQuery); 
         	        	folderId =  folder  + "download" + File.separator +  sEndpointForQuery + "_" + idRequest;     				
 	    			}
     	    		//create widget
     	    		else{
     	    			
     	    			folderDownload = folder + "download";		
     	    			folderList = folderDownload + File.separator + "list_endpoint";
     	    			folderTemplate = folderList + File.separator + "template";			
     	    			
     	    			//create a folder to store this sparql endpoint if it has not analyzed previously 
         	    		sEndpointForQuery = endpoint.getEndpointForQuery();       	    	 	    		
         	    		sEndpointForQuery = Support.removeSpecialCharacterInFileName(sEndpointForQuery); 
         	        	folderId = folderDownload + File.separator + sEndpointForQuery + "_" + idRequest;	    		
         				fId = new File(folderId);
         				fId.mkdir();  		
         	    		
     	    			bEndpoint = FileOperation.findFolder(folderList, sEndpointForQuery);
 	     	    		if(bEndpoint == false){
 	     	    			fEndpoint = new File(folderList + File.separator + sEndpointForQuery);
 	         	    		fEndpoint.mkdir(); 	         	    	
 	     	    		}
 	    				for(i=0; i<n; i++){
	     	    			j = Integer.parseInt(list.get(i));
	     		    		dsName = endpoint.getDataSet(j).getUri();
	     		    		dsName = Support.getName(dsName);
	     		    		dsName = j + "_"+ dsName;  
	     		    		bDataset = FileOperation.findFile(folderList + File.separator + sEndpointForQuery, dsName + ".html");     	
	     		    		
	 		    			if(bEndpoint && bDataset){
	 		    				FileOperation.copyFolder(folderList + File.separator + sEndpointForQuery + File.separator + dsName+".html",
	 		    						folderId + File.separator + dsName+".html");
	 		    			}else{
	 		    				endpoint.getDataSet(j).queryComponent(endpoint.getEndpointForQuery(), endpoint.getHTTP(), endpoint.getUseDistict());
	     		     	     	endpoint.getDataSet(j).queryValue(endpoint.getEndpointForQuery(), endpoint.getHTTP(), endpoint.getFindOther(), endpoint.getRemove());	
	     		    			Widget widget = new Widget(endpoint.getDataSet(j), j, endpoint.getEndpointForWidget(), folderId, folderTemplate);	
	     		    			widget.createWidgetFile();
	     		    			
	     		    			//copy this dataset to folder of its endpoint
	 	     		    		FileOperation.copyFolder(folderId + File.separator + dsName+".html", 
	 	     		    					folderList + File.separator + sEndpointForQuery + File.separator + dsName+".html");
		     		    			
	 	     		    		if(endpoint.getDataSet(j).getDimensionSize()==0 || endpoint.getDataSet(j).getMeasureSize()==0 || endpoint.getDataSet(j).getDimension().allEmpty()){
	 	     		    			log.debug("--------------------------");
	 	     		    			log.debug("Not found value at "+ endpoint.getDataSet(j).getUri());
	 	     		    			log.debug("--------------------------");
	 	     		    			if(sErrorList.isEmpty())
	 	     		    				sErrorList = endpoint.getDataSet(j).getUri();
	 	     		    			else
	 	     		    				sErrorList = sErrorList + "<br>" + endpoint.getDataSet(j).getUri();
	 	     		    		}
	 		    			}
	     	    			
	     		    	}
 	    			}
     	    		Date d2 = new Date();
     	    		long diff = d2.getTime() - d1.getTime();
     				long diffMin = diff /(1000);
     				log.info("Time to finish: " + diffMin + " seconds");
     	    		log.info(folderId);
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
                e.printStackTrace();
                request.setAttribute("errorPage", e.toString());
  				RequestDispatcher view = request.getRequestDispatcher("/generation/error.jsp");
  				view.forward(request, response);  	
         	}
         }    	
    }
}
