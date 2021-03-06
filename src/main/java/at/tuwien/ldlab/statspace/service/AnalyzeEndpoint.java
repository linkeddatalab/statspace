/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.tuwien.ldlab.statspace.service;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import at.tuwien.ldlab.statspace.util.SpecialEndpointList;
import at.tuwien.ldlab.statspace.widgetgeneration.Endpoint;
import at.tuwien.ldlab.statspace.widgetgeneration.Request;

import java.io.*;
import java.util.Random;


public class AnalyzeEndpoint extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(AnalyzeEndpoint.class);

	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
		
		//get Parameter from request
		String sCache    = request.getParameter("cache");
		String sEndpoint = request.getParameter("endpoint");  
		if(sCache!=null && sCache.toLowerCase().equals("no"))
			sCache = "no";
		else
			sCache = "yes";
		
		HttpSession session = request.getSession();		
		session.setMaxInactiveInterval(60*60);   	
    	if(!sEndpoint.isEmpty()){
    		log.info("Analyze endpoint + " + sEndpoint);		
    		//check special endpoint list
        	int k;
        	boolean bHTTP, bRemove, bFindOther;
        	String sUseDistinct;
        	String sEndpointForWidget;
        	sEndpoint=sEndpoint.trim();
        	if(!sEndpoint.toLowerCase().startsWith("http"))
        		sEndpoint="http://"+sEndpoint;
        	
        	SpecialEndpointList specialList = new SpecialEndpointList(getServletContext().getRealPath("/")  + File.separator + "template" + File.separator + "list.xml"); 	
        	k=specialList.getEndpointIndex(sEndpoint);
        	if(k!=-1){
        		if(!specialList.getEndpointForQuery(k).equals(""))
        			sEndpoint = specialList.getEndpointForQuery(k);
        		if(!specialList.getEndpointForWidget(k).equals(""))
        			sEndpointForWidget = specialList.getEndpointForWidget(k);
        		else
        			sEndpointForWidget = sEndpoint;
        		
        		bHTTP = specialList.getHTTPRequest(k);
				bRemove = specialList.getRemoveDuplicate(k);
				sUseDistinct = specialList.getUseDistinct(k);
				bFindOther = specialList.getFindOtherValue(k);        		
        	}else{
        		sEndpointForWidget = sEndpoint;
        		bHTTP   = false;
        		bRemove = false;
        		sUseDistinct = "";
        		bFindOther = true;
        	}        		
        	
	    	//query sparql endpoint
        	Endpoint endpoint = new Endpoint(sEndpoint, sEndpointForWidget, bHTTP, bRemove, sUseDistinct, bFindOther);
	        endpoint.queryDataSet();
	    	
			int errorCode = endpoint.getErrorCode();
			if(endpoint.getDataSet().size()!=0) 
				errorCode =-1;			
			
			if(errorCode!= -1){
				request.setAttribute("error", errorCode);			
				RequestDispatcher view = request.getRequestDispatcher("/generation/index.jsp");
				view.forward(request, response);
			}else{		
				
				//set Id for this request
		        Random random = new Random();
				int idRequest = random.nextInt();	
				
				//add new request		
				Request req = new Request();
				req.setEndpoint(endpoint);			
				request.setAttribute("idRequest", idRequest);
				request.setAttribute("cache", sCache);
				request.getServletContext().setAttribute(Integer.toString(idRequest), req);				
				RequestDispatcher view = request.getRequestDispatcher("/generation/dataset.jsp");
				view.forward(request, response);		
			}
		}
    }

}
