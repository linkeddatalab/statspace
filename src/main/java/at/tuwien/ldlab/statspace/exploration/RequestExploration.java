/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.tuwien.ldlab.statspace.exploration;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.*;
import javax.servlet.http.*;
import at.tuwien.ldlab.statspace.metadata.MetaData;
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
    	String sKeyword = request.getParameter("keyword");
    	if(sKeyword==null) 
    		sKeyword = "";
    	else {
    		sKeyword = sKeyword.trim().replace("'", "");
    		sKeyword = sKeyword.trim().replace("\"", "");
    	}
    	
    	MetaData md = new MetaData();
    	md.setKeyword(sKeyword);
    	ArrayList<MetaData> arrMetaData =  new ArrayList<MetaData>();	
    	arrMetaData = md.queryMetaDataByKeyword();    		
		
		//add new request			
		request.setAttribute("result", arrMetaData);
		request.setAttribute("keyword", sKeyword);
		RequestDispatcher view = request.getRequestDispatcher("/exploration/index.jsp");		
		view.forward(request, response);		
				
	}  	    	
}
