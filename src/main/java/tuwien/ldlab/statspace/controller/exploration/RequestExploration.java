/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tuwien.ldlab.statspace.controller.exploration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.servlet.*;
import javax.servlet.http.*;
import tuwien.ldlab.statspace.model.mediator.MetaData;
public class RequestExploration extends HttpServlet {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException{
		doGet(request, response);
	}
	
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { 
    	
    	String sKeyword    = request.getParameter("keyword");
    	if(sKeyword==null) sKeyword="";    	
    	MetaData md = new MetaData();
    	md.setKeyword(sKeyword);
    	ArrayList<MetaData> arrMetaData =  new ArrayList<MetaData>();	
    	arrMetaData = md.queryMetaDataByKeyword();    		
		Random random = new Random();
		int idRequest = random.nextInt();	
			
		//add new request		
		request.setAttribute("idRequest", idRequest);			
		request.getServletContext().setAttribute(Integer.toString(idRequest), arrMetaData);
		
		RequestDispatcher view = request.getRequestDispatcher("/exploration/index.jsp");
		view.forward(request, response);		
				
	}  	    	
}
