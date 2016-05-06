package tuwien.ldlab.statspace.controller.exploration;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import tuwien.ldlab.statspace.model.mediator.MetaData;


public class ExploreDataSet  extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException{
		doGet(request, response);
	}
	
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { 
    	String sMDUri  = request.getParameter("id1"); 
    	String sIdRequest   = request.getParameter("idRequest");
    	
        if(sMDUri!=null && !sMDUri.isEmpty() && sIdRequest!=null && !sIdRequest.isEmpty()){
        	MetaData md = new MetaData();  
        	md.setUri(sMDUri);
        	md.queryMetaDataInfor();
           	ArrayList<MetaData> results = new ArrayList<MetaData>();
           	results = md.searchComparableDataSet();
           	//swap inputMetaData to the first position of results
           	int i;
           	for(i=0; i<results.size(); i++){
           		if(results.get(i).getUri().equals(sMDUri)){
           			MetaData tmp = results.get(0);
           			results.set(0, results.get(i));
           			results.set(i, tmp);
           			break;
           		}
           	}
           	
           	//return to users
           	request.setAttribute("idRequest", Integer.parseInt(sIdRequest));			
           	request.getServletContext().removeAttribute(sIdRequest);
    		request.getServletContext().setAttribute(sIdRequest, results);    		
    		RequestDispatcher view = request.getRequestDispatcher("/exploration/dataset.jsp");
    		view.forward(request, response);	
        }    	
    }
}