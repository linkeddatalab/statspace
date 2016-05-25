/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tuwien.ldlab.statspace.controller.widgetgeneration;
import tuwien.ldlab.statspace.model.util.SpecialEndpointList;
import tuwien.ldlab.statspace.model.widgetgeneration.*;

import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.*;
import java.util.Random;


public class AnalyzeEndpoint extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(AnalyzeEndpoint.class);

	public AnalyzeEndpoint() {	
	}
	
	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
		log.info("------------------------------");
//		log.info("Calling AnalyzeEndpoint class");
//		log.info(getServletContext().getRealPath("/"));
		SpecialEndpointList specialList = new SpecialEndpointList(getServletContext().getRealPath("/")
											+"download_widgets"+File.separator+"list_endpoint"+File.separator+"template"+File.separator+"list.xml"); 
	
		//get Parameter from request
		HttpSession session = request.getSession( );		
		session.setMaxInactiveInterval(-1);	
    	String sEndpoint = request.getParameter("endpoint");   	
    	
    	if(!sEndpoint.isEmpty()){
    		//check special endpoint list
        	int k;
        	boolean bHTTP, bRemove, bFindOther;
        	String sUseDistinct;
        	String sEndpointForWidget;
        	sEndpoint=sEndpoint.trim();
        	if(!sEndpoint.toLowerCase().startsWith("http"))
        		sEndpoint="http://"+sEndpoint;
        	
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
				int requestId = random.nextInt();	
				
				//add new request		
				Request newRequest = new Request();
				newRequest.setEndpoint(endpoint);
				newRequest.setId(requestId);	
				request.setAttribute("idRequest", requestId);			
				request.getServletContext().setAttribute(Integer.toString(requestId), newRequest);
				
				RequestDispatcher view = request.getRequestDispatcher("/generation/dataset.jsp");
				view.forward(request, response);		
			}
		}
    }

}
