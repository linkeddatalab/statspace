/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tuwien.ldlab.statspace.controller.metadata;
import tuwien.ldlab.statspace.model.util.SpecialEndpointList;
import tuwien.ldlab.statspace.model.util.Support;
import tuwien.ldlab.statspace.model.widgetgeneration.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class GenerateWidget extends HttpServlet {
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
		
		SpecialEndpointList specialList = new SpecialEndpointList( getServletContext().getRealPath("/")
											+"download_widgets"+File.separator+"list_endpoint"+File.separator+"template"+File.separator+"list.xml"); 
		
//		System.out.println(getServletContext().getRealPath("/"));
    	String endpoint = request.getParameter("endpoint");     
    	String dataset = request.getParameter("dataset");
    	String location = request.getParameter("location");
    	
    	if(endpoint!=null){
    		//check special endpoint list
        	int k;        	   
        	String sEndpointForWidget;
        	
        	k=specialList.getEndpointIndex(endpoint);
        	if(k!=-1){        		
        		if(!specialList.getEndpointForWidget(k).equals(""))
        			sEndpointForWidget = specialList.getEndpointForWidget(k);
        		else
        			sEndpointForWidget = endpoint;        		
        	}else{
        		sEndpointForWidget = endpoint;        		
        	}         	    	
        	
	      	DataSet ds = new DataSet(dataset, "");
        	ds.queryComponentFromMetaData();        	
       		ds.queryValueFromMetaData();	
       		ds.getMeasure().setBMultipleMeasure(true);
        	
        	//Define URI of location
        	ArrayList<String> arrLocation = new ArrayList<String>();
        	String sFilter="";
        	String[] parameters = location.split(";");
        	for(k=0; k<parameters.length; k++)
        		if(k==0)
        			sFilter = "?l= <" + parameters[0] +"> ";
        		else
        			sFilter = sFilter + " || " + "?l= <" + parameters[k] +"> ";
        
        	arrLocation = queryLocation(dataset, sFilter);   
    		//Create widget        	
        	String folder;    		 
    		Random random = new Random();
			int requestId = random.nextInt();	
				
    		folder =  getServletContext().getRealPath("/");    		
    		String folder_download = folder + "download_widgets";    	
    		String folder_endpoint = folder_download + File.separator + "temp_" + requestId;
    		File f_endpoint = new File(folder_endpoint); f_endpoint.mkdir();
    		String folder_template = folder_download + File.separator +"list_endpoint" + File.separator + "template";		

        	Widget widget = new Widget(ds, 1, sEndpointForWidget, folder_endpoint, folder_template);
        	widget.createWidgetFile2(arrLocation);
        	String file_name = "http://linkedwidgets.org/statspace/download_widgets/temp_" +requestId + "/"+ Support.getName(dataset) + ".html";    
//        	String file_name = "http://localhost/statisticaldata/download_widgets/temp_" +requestId + "/"+ Support.getName(dataset) + ".html";

        	response.setStatus(HttpServletResponse.SC_ACCEPTED);
        	response.addHeader("Access-Control-Allow-Origin", "*");
        	response.getWriter().println(file_name);		
    	}       	
    	    		
    } 
    
    public ArrayList<String> queryLocation(String dataset, String sFilter){
		String queryString;
		
		queryString =	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+
						"PREFIX map:  <http://linkedwidgets.org/statisticalwidgets/mapping/> \n"+	
						"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
						"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
						"SELECT DISTINCT ?d ?v \n"+ 
						"WHERE{ \n"+    
							"?md map:dataset <"+dataset+">. \n" +
							"?md map:component ?d. \n"+
							"?d map:hasValue ?v. \n"+
							"<"+dataset+"> map:describes ?v. \n"+
							"?l map:reference ?v. \n"+							
							"FILTER ("+ sFilter + ") \n "+									
						"}";
		
	
		return getLocation(queryString);		
	}	
	
	public ArrayList<String> getLocation(String queryString) {
		QueryExecution queryExecution=null;
		String sDimension, sValue;
		ArrayList<String> arrLocation = new ArrayList<String>();
		try{
			Query query = QueryFactory.create(queryString);		
			queryExecution = QueryExecutionFactory.sparqlService("http://ogd.ifs.tuwien.ac.at/sparql", query);
			// execute query
			ResultSet rs = queryExecution.execSelect();			
			while (rs!=null && rs.hasNext()) {		
				QuerySolution sol = rs.nextSolution();				
				sDimension = sol.get("d").toString();		
				sValue = sol.get("v").toString();	
				if(arrLocation.size()==0){
					arrLocation.add(sDimension);
					arrLocation.add(sValue);
				}else
					arrLocation.add(sValue);					
			}			
		}catch (QueryExceptionHTTP e){	
		}		
		catch(QueryException e){
		}
		catch(Exception e){		
		}		
		finally {
			if(queryExecution!=null)
				queryExecution.close() ;
		}
		return arrLocation;
	}   
    
}
