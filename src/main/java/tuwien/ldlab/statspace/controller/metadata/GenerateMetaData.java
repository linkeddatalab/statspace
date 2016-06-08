package tuwien.ldlab.statspace.controller.metadata;

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
import be.ugent.mmlab.rml.core.RMLEngine;
import be.ugent.mmlab.rml.model.Parameters;
import tuwien.ldlab.statspace.model.metadata.MetaDataForRML;
import tuwien.ldlab.statspace.model.util.SpecialEndpointList;
import tuwien.ldlab.statspace.model.widgetgeneration.Endpoint;
import tuwien.ldlab.statspace.model.widgetgeneration.Request;

public class GenerateMetaData extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String sRMLSource=null, sEndpoint=null;   			
    	Parameters parameters = new Parameters();
    	
    	//read parameters
		Enumeration<String> arrNames = request.getParameterNames();
		while(arrNames.hasMoreElements()){
			String sName = arrNames.nextElement();
			if(sName.equalsIgnoreCase("sparql")){
				sEndpoint  = request.getParameter("sparql").trim();				
			}else{
				if(sName.equalsIgnoreCase("rmlsource")){
					sRMLSource    = request.getParameter("rmlsource").trim();				
				}else{
					String sValue = request.getParameter(sName);
					parameters.addParameterValue(sName, sValue);
				}
			}
		}	
		
		//case 1: create metadata for an endpoint
		if(sEndpoint!=null && !sEndpoint.isEmpty()){
			SpecialEndpointList specialList = new SpecialEndpointList(getServletContext().getRealPath("/") + File.separator + "template" + File.separator + "list.xml"); 
			HttpSession session = request.getSession( );		
			session.setMaxInactiveInterval(60*60*3);	
			
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
				int requestId = random.nextInt();	
				
				//add new request		
				Request newRequest = new Request();
				newRequest.setMetaDataPurpose(true);
				newRequest.setEndpoint(endpoint);
				newRequest.setId(requestId);	
				request.setAttribute("idRequest", requestId);			
				request.getServletContext().setAttribute(Integer.toString(requestId), newRequest);				
				RequestDispatcher view = request.getRequestDispatcher("/generation/dataset.jsp");
				view.forward(request, response);		
			}		
		}
		//case 2: create metadata for a non-RDF dataset
		else if(sRMLSource!=null && !sRMLSource.isEmpty()){
			String sPath =  getServletContext().getRealPath("/");
			String sSeparator = File.separator;		
			if(RMLEngine.isLocalFile(sRMLSource)){
				 sRMLSource = sPath +  sRMLSource;
			}
			MetaDataForRML md = new MetaDataForRML();
			md.runRMLProcessor(sRMLSource, sPath, sSeparator, parameters);
			boolean bStatus = md.getStatus();
			if(!bStatus){
				response.addHeader("Access-Control-Allow-Origin", "*");
	        	response.getWriter().println("There was an error with your mapping");
			}else{
				File file = new File(md.getOutputFile());	           	
	    		response.setStatus(HttpServletResponse.SC_OK);
	    		response.setContentType("application/octet-stream");
	    		String s = file.getName();
	    		s = s.substring(s.indexOf("_")+1);
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
