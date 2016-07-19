/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.tuwien.ldlab.statspace.service;


import java.io.File;
import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.tuwien.ldlab.statspace.sparql.SparqlEndpoint;
import at.tuwien.ldlab.statspace.util.SpecialEndpointList;


public class ReceiveSparqlQuery extends HttpServlet {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(ReceiveSparqlQuery.class);

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException{
		doGet(request, response);
	}
	
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
		
		SpecialEndpointList specialList = new SpecialEndpointList( getServletContext().getRealPath("/")  + File.separator + "template" + File.separator + "list.xml"); 
	   	String sEndpoint = request.getParameter("endpoint");     
    	String sQuery    = request.getParameter("query"); 
    	log.info("Call ReceiveSparqlQuery for endpoint: " + sQuery + "; query: " + sEndpoint);
    	
    	if(sEndpoint!=null && sQuery!=null){   
    		boolean bHTTP, bError;
        	int k;
        	String sResult;
        	
        	//use Jena or HTTP Request
        	k=specialList.getEndpointIndex(sEndpoint);
        	if(k!=-1)       		
        		bHTTP = specialList.getHTTPRequest(k);				    		
        	else       		
        		bHTTP   = false;       		
        	
        	SparqlEndpoint q = new SparqlEndpoint(sEndpoint, sQuery, bHTTP);
        	sResult = q.query();
        	bError = q.getErrorStatus();
        	
        	if(bError==false && sResult !="")        	
	        	response.setStatus(HttpServletResponse.SC_OK);
	        else
        		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
           response.addHeader("Access-Control-Allow-Origin", "*");
           response.setContentType("application/json");
           response.getWriter().println(sResult);        	        	
    	} 
    	else{
    		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
        	response.addHeader("Access-Control-Allow-Origin", "*");
        	response.getWriter().println("");		
    	} 	    		
    }   
    
    
}
