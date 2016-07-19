/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.tuwien.ldlab.statspace.service;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;
public class ShowOntology extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException{
		doGet(request, response);
	}
	
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	String sURL = request.getRequestURL().toString();
//    	sURL=sURL.replace("http://localhost:8080/statspace", "http://statspace.linkedwidgets.org/statspace");
    	if(sURL.startsWith("http://statspace.linkedwidgets.org/statspace/codelist/")
    			||sURL.startsWith("http://statspace.linkedwidgets.org/statspace/metadata/")
    			||sURL.startsWith("http://statspace.linkedwidgets.org/statspace/dataset/")
    			||sURL.startsWith("http://statspace.linkedwidgets.org/statspace/terms/")
    			||sURL.startsWith("http://statspace.linkedwidgets.org/statspace/dimension/")){
    		sURL = "http://linkedwidgets.org/resource/page/" + sURL.replace("http://statspace.linkedwidgets.org/statspace/","");
    		response.sendRedirect(sURL);    		
    	}else{
    		response.addHeader("Access-Control-Allow-Origin", "*");
        	response.getWriter().println("Sorry, we dont have information about your URI: " + sURL);
    	}
	}  	    	
}
