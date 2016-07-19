package at.tuwien.ldlab.statspace.service;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Random;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.tuwien.ldlab.statspace.metadata.MetaDataForRML;
import at.tuwien.ldlab.statspace.util.SpecialEndpointList;
import at.tuwien.ldlab.statspace.widgetgeneration.Endpoint;
import at.tuwien.ldlab.statspace.widgetgeneration.Request;
import be.ugent.mmlab.rml.model.Parameters;

public class GenerateMetaData extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(GenerateMetaData.class);
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String sRMLSource=null, sEndpoint=null;   			
    	Parameters parameters = new Parameters();    	
    	String sCache="";  
    	String sQuery = request.getQueryString();
    	
    	//read parameters
		Enumeration<String> arrNames = request.getParameterNames();
		while(arrNames.hasMoreElements()){
			String sName = arrNames.nextElement();
			if(sName.equalsIgnoreCase("sparql")){
				sEndpoint  = request.getParameter("sparql").trim();				
			}else if(sName.equalsIgnoreCase("cache"))
				sCache    = request.getParameter("cache").trim();	
			else if(sName.equalsIgnoreCase("rmlsource")){
				sRMLSource    = request.getParameter("rmlsource").trim();				
			}else{
				String sValue = request.getParameter(sName);
				parameters.addParameterValue(sName, sValue);
			}
		}	
		boolean bUseCache = true;    	
    	if(sCache!=null && sCache.toLowerCase().equals("no"))
			bUseCache = false;
		else
			sCache = "yes";
		
		//case 1: create metadata for an endpoint
		if(sEndpoint!=null && !sEndpoint.isEmpty()){
			log.info("Create metadata for " + sEndpoint);
			SpecialEndpointList specialList = new SpecialEndpointList(getServletContext().getRealPath("/") + File.separator + "template" + File.separator + "list.xml"); 
			HttpSession session = request.getSession( );		
			session.setMaxInactiveInterval(60*60);	
			
			//check special endpoint list
			int k;
			boolean bHTTP, bRemove, bFindOther;
			String sUseDistinct;		
			sEndpoint=sEndpoint.trim();
			if(!sEndpoint.toLowerCase().startsWith("http"))
				sEndpoint="http://"+sEndpoint;
			
			k=specialList.getEndpointIndex(sEndpoint);
			if(k!=-1){
				if(!specialList.getEndpointForQuery(k).equals(""))
					sEndpoint = specialList.getEndpointForQuery(k);					
				bHTTP = specialList.getHTTPRequest(k);
				bRemove = specialList.getRemoveDuplicate(k);
				sUseDistinct = specialList.getUseDistinct(k);
				bFindOther = specialList.getFindOtherValue(k);        		
			}else{
				bHTTP   = false;
				bRemove = false;
				sUseDistinct = "";
				bFindOther = true;
			}        		
			
			//query sparql endpoint
			Endpoint endpoint = new Endpoint(sEndpoint, "", bHTTP, bRemove, sUseDistinct, bFindOther);
			endpoint.queryDataSet();
			
			int errorCode = endpoint.getErrorCode();
			if(endpoint.getDataSet().size()!=0) 
				errorCode =-1;			
			
			if(errorCode!= -1){
				response.addHeader("Access-Control-Allow-Origin", "*");
	        	response.getWriter().println("Sorry. No dataset is detected");
			}else{			
				//set Id for this request
				Random random = new Random();
				int idRequest = random.nextInt();	
				
				//add new request		
				Request req = new Request();
				req.setMetaDataPurpose(true);
				req.setEndpoint(endpoint);
				request.setAttribute("idRequest", idRequest);	
				request.setAttribute("cache", sCache);
				request.getServletContext().setAttribute(Integer.toString(idRequest), req);				
				RequestDispatcher view = request.getRequestDispatcher("/generation/dataset.jsp");
				view.forward(request, response);		
			}		
		}
		//case 2: create metadata for a non-RDF dataset
		else if(sRMLSource!=null && !sRMLSource.isEmpty()){
			log.info("Create metadata for " + sRMLSource);
			String folderWebApp =  getServletContext().getRealPath("/");			
			MetaDataForRML md = new MetaDataForRML(sRMLSource, folderWebApp, sQuery);
			md.createMetaData(parameters, bUseCache);
			boolean bStatus = md.getStatus();
			if(!bStatus){
				response.addHeader("Access-Control-Allow-Origin", "*");
	        	response.getWriter().println("There was an error with your mapping");
			}else{
				File file = new File(md.getOutputMetaDataFile());	           	
	    		response.setStatus(HttpServletResponse.SC_OK);
	    		response.setContentType("application/octet-stream");
	    		String s = file.getName();	    
	    		s = s.replace(".ttl.", ".");
	    		response.setHeader("Content-Disposition", "attachment; filename=" + s);	    		
			    int length=0;	
				ServletOutputStream outStream = response.getOutputStream();
				byte[] byteBuffer = new byte[4096];
			    DataInputStream in = new DataInputStream(new FileInputStream(file));	     
		        while ((in != null) && ((length = in.read(byteBuffer)) != -1))
		        {
		            outStream.write(byteBuffer,0,length);
		        }    	        
		        in.close();
		        outStream.close();
			}
		}else{
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
        	response.addHeader("Access-Control-Allow-Origin", "*");
        	response.getWriter().println("Sorry, we cannot recognize parameters in your request");
		}
	}
}
