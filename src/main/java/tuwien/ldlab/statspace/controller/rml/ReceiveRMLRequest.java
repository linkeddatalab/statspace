package tuwien.ldlab.statspace.controller.rml;


import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ReceiveRMLRequest extends HttpServlet {
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
    	response.sendRedirect("localhost:8088/rml?" + request.getQueryString());  	
    }
}
