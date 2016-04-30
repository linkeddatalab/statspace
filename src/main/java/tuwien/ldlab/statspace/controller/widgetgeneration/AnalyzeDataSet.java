/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tuwien.ldlab.statspace.controller.widgetgeneration;
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
	private String folder_download = "";
	private String folder_template = "";
	private static Log log = LogFactory.getLog(AnalyzeDataSet.class);
		

	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
		
//		log.info("Calling AnalyzeDataSet class");		
		
		String folder, folder_list, folder_id, sEndpointForQuery, sId, dsName;
		int i, j, n;		
		boolean bEndpoint, bDataset;
		File file_endpoint;	 
		
		folder =  getServletContext().getRealPath("/");
		
		folder_download = folder + "download_widgets";		
		folder_list = folder_download + File.separator +"list_endpoint";
		folder_template = folder_list + File.separator + "template";			
		
		sId= request.getParameter("idRequest");
      	Object objRequest = request.getServletContext().getAttribute(sId);
      	
        if(! sId.isEmpty() && objRequest!=null){  
        	int idRequest = Integer.parseInt(sId);
         	Request req = (Request) objRequest;        	      	 
           	Endpoint endpoint = req.getEndpoint();
           
         	
         	String names = request.getParameter("chkValue");
         	String[] valueList = names.split(";");    	
         	List<String> list = Arrays.asList(valueList); 
         	n = list.size();
         	try{
     	    	if(n>0){     	    		
     	    		//create  folder session of this sparql endpoint if it was not created already
     	    		sEndpointForQuery = endpoint.getEndpointForQuery();    
     	    		if(sEndpointForQuery.startsWith("http://"))
     	    			sEndpointForQuery=sEndpointForQuery.substring(7);     	    		
     	    		sEndpointForQuery = sEndpointForQuery.replaceAll("/", "+"); 
     	    		sEndpointForQuery = sEndpointForQuery.replaceAll(":", "=");  
     	    		
     	    		bEndpoint = FileOperation.findFolder(folder_list, sEndpointForQuery);
     	    		if(bEndpoint == false){
     	    			file_endpoint = new File(folder_list + File.separator + sEndpointForQuery);
         	    		file_endpoint.mkdir(); 
     	    		}     	    			
     	    		
     	    		folder_id = folder_download + File.separator + sEndpointForQuery + "_" + idRequest;	    		
     				file_endpoint = new File(folder_id);
     	    		file_endpoint.mkdir();  		
     	    		
//     	    		log.info("Creating widgets from " + sEndpointForQuery);
     	    		String sErrorList="";
     	    		Date d1 = new Date();     				
     	    		
     	    		//create file of each data set (create widget)
     	    		for(i=0; i<n; i++){
     		    		j = Integer.parseInt(list.get(i));
     		    		dsName = endpoint.getDataSet(j).getUri();
     		    		dsName = Support.getName(dsName);
     		    		dsName = j + "_"+ dsName;     	    		
     		    	
     		    		
     		    		//check if this dataset was already created     		    		
     		    		bDataset = FileOperation.findFile(folder_list + File.separator + sEndpointForQuery, dsName + ".html");
     		    		
 		    			if(bEndpoint && bDataset){
 		    				FileOperation.copyFolder(folder_list + File.separator + sEndpointForQuery + File.separator + dsName+".html",
 		    						folder_id + File.separator + dsName+".html");
 		    			}else{
 		    				endpoint.getDataSet(j).queryComponent(endpoint.getEndpointForQuery(), endpoint.getHTTP(), endpoint.getUseDistict());
     		     	     	endpoint.getDataSet(j).queryValue(endpoint.getEndpointForQuery(), endpoint.getHTTP(), endpoint.getFindOther(), endpoint.getRemove());	
     		    			Widget widget = new Widget(endpoint.getDataSet(j), j, endpoint.getEndpointForWidget(), folder_id, folder_template);	
     		    			widget.createWidgetFile();
     		    			
     		    			//copy this dataset to folder of its endpoint
 	     		    		FileOperation.copyFolder(folder_id + File.separator + dsName+".html", 
 	     		    					folder_list + File.separator + sEndpointForQuery + File.separator + dsName+".html");
	     		    			
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
     	    		Date d2 = new Date();
     	    		long diff = d2.getTime() - d1.getTime();
     				long diffMin = diff /(1000);
     				log.info("Time to create widgets: " + diffMin + " seconds");
     	    		
     	    		ZipFolder.doZip(folder_id, folder_id+".zip");
     	    	 		
     	    		//store download link to array Request, then set into session
     	    		req.setDownload(folder_id + ".zip");
     	    		request.getServletContext().removeAttribute(sId);
     	    		request.getServletContext().setAttribute(sId,req);  
     	    		    		
     	    		//redirect to download page
     	    		if(!sErrorList.isEmpty())
     	    			request.setAttribute("errorList", sErrorList);     	    		
     	    		request.setAttribute("idRequest", idRequest);
     				RequestDispatcher view = request.getRequestDispatcher("/widgetgeneration/download.jsp");
     				view.forward(request, response);  	    			    		
     	    	}
         	}
         	catch(Exception e){         		 
                e.printStackTrace();
                request.setAttribute("errorPage", e.toString());
  				RequestDispatcher view = request.getRequestDispatcher("/widgetgeneration/error.jsp");
  				view.forward(request, response);  	
         	}
         }    	
    }

}
