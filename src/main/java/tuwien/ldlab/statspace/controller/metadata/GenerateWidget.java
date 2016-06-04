/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tuwien.ldlab.statspace.controller.metadata;
import tuwien.ldlab.statspace.model.mediator.MetaData;
import tuwien.ldlab.statspace.model.mediator.StringTriple;
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
    	String metadata = request.getParameter("metadata");    
    	String location = request.getParameter("location");
  
    	if(metadata!=null){
    		//check special endpoint list
        	int i, j, k;        	   
        	String dataset, endpoint, feature;
        	
        	//query information of this metadata from repository
        	StringTriple st = queryMetaDataInfor(metadata);
        	dataset 	= st.getFirstString();
        	endpoint 	= st.getSecondString();
        	feature		= st.getThirdString();         	
        	String file_name="";
        	
        	//init variable for storage
        	String folder;    		 
    		Random random = new Random();
			int requestId = random.nextInt();
			folder 					= getServletContext().getRealPath("/");    		
    		String folder_download 	= folder + "download";    	
    		String folder_target 	= folder_download + File.separator + "temp_" + requestId;
    		File f_target 			= new File(folder_target); f_target.mkdir();
    		String folder_template 	= folder_download + File.separator +"list_endpoint" + File.separator + "template";	
        	
       		//case: use SPARQL method
        	if(feature.toLowerCase().contains("sparql")){         		     		
	        	String sEndpointForWidget="";
	        	SpecialEndpointList specialList = new SpecialEndpointList( getServletContext().getRealPath("/")
						+"download"+File.separator+"list_endpoint"+File.separator+"template"+File.separator+"list.xml"); 
	        	k=specialList.getEndpointIndex(endpoint);
	        	if(k!=-1){        		
	        		if(!specialList.getEndpointForWidget(k).equals(""))
	        			sEndpointForWidget = specialList.getEndpointForWidget(k);
	        		else
	        			sEndpointForWidget = endpoint;        		
	        	}else{
	        		sEndpointForWidget = endpoint;        		
	        	}  
	        	
	        	//init dataset
            	DataSet ds = new DataSet(dataset, "");
            	ds.queryComponentFromMetaData(metadata);        	
           		ds.queryValueFromMetaData();	
           		ds.getMeasure().setBMultipleMeasure(true);
           		
           		//define URI of location
            	ArrayList<String> arrLocation = new ArrayList<String>();
            	String sFilter1="", sFilter2="";
            	String[] parameters = location.split(";");
            	for(k=0; k<parameters.length; k++)
            		if(k==0){
            			sFilter1 = "?l= <" + parameters[0] +"> ";
            			sFilter2 = "?v= <" + parameters[0] +"> ";
            		}else{
            			sFilter1 = sFilter1 + " || " + "?l= <" + parameters[k] +"> ";
            			sFilter2 = sFilter2 + " || " + "?v= <" + parameters[k] +"> ";
            		}            	
            	//detect local URIs of locations used in this data set 
            	arrLocation = queryLocation(metadata, dataset, sFilter1, sFilter2);             	
	        	Widget widget = new Widget(ds, 1, sEndpointForWidget, folder_target, folder_template);
	        	widget.createWidgetUseSPARQLMethod(arrLocation);	        	
        	}else{
        		//case: use RML or API methods            
           		MetaData md = new MetaData();      
            	md.setUri(metadata);
            	md.queryMetaDataInfor();            	
               	               	
               	//Step 1. Set variables           	
            	md.setVariable();               	
               	
               	//Reorder components to unit, obsValue, refArea, refPeriod,...               	
               	md.reorderComponents();        
               	
               	//Step 2. Query         	
            	//Step 2.1. Set filter
            	md.getComponent(2).setFilterValue(location);
            	            	
            	//Step 2.2. Query data set
               	String sVarObs = "?o";
            	String sWebApp =  getServletContext().getRealPath("/");		
    			String sSeparator = File.separator;		       	          	
    			md.rewriteQuery(sVarObs, sWebApp, sSeparator, false);
    			
                //Step 3. Rewrite results
    			for(i=0; i<md.getNumberofComponent(); i++)
    				for(j=0; j<md.getComponent(i).getValueSize(); j++)
    					md.getComponent(i).setValueRefence(j, md.getComponent(i).getValue(j));
    			
    			//Step 4. Filter observation with selected locations (recheck step 2.1)
    			ArrayList<String> arrLocation = new ArrayList<String>();            
            	String[] parameters = location.split(";");
            	for(k=0; k<parameters.length; k++)
            		arrLocation.add(parameters[k]);
            	
            	ArrayList<String> arrValue0 = md.getDistinctRefValue(2);				
				for(i=0; i<arrValue0.size(); i++){
					if(arrLocation.indexOf(arrValue0.get(i))==-1){
						arrValue0.remove(i);
						i--;					
					}
				}
				md.filterValue(2, arrValue0);
				
				//Step 5. Write file
				Widget widget = new Widget(folder_target, folder_template);
	        	widget.createWidgetUseRMLMethod(md, dataset);				
        	}
        	
        	dataset = Support.getName(dataset);	
        	dataset = dataset.replace("%2F", "_");
        	file_name = "http://linkedwidgets.org/statspace/download/temp_" +requestId + "/"+ dataset + ".html";    
//        	file_name = "http://localhost:8080/statspace/download/temp_" +requestId + "/"+ dataset + ".html";
        	
        	//return to user
        	response.setStatus(HttpServletResponse.SC_ACCEPTED);
        	response.addHeader("Access-Control-Allow-Origin", "*");
        	response.getWriter().println(file_name);	
    	}
    }   	
    	    		
     
    
    public StringTriple queryMetaDataInfor(String metadata){
		String queryString;
		
		queryString =	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+						
						"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
						"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
						"PREFIX dcat:  <http://www.w3.org/ns/dcat#> \n"+
						"PREFIX void:  <http://rdfs.org/ns/void#> \n"+
						"SELECT DISTINCT ?ds ?url ?feature \n"+ 
						"WHERE{ \n"+ 
					  	" graph <http://statspace.linkedwidgets.org> { \n" +							
							"<" + metadata + "> qb:dataSet ?ds. \n"+
							"?ds dcat:accessURL ?url. \n"+
							"?ds void:feature ?feature. \n"+							
						"  }\n"+
						"}";
		return getMetaDataInfor(queryString);		
	}	
	
	public StringTriple getMetaDataInfor(String queryString) {
		QueryExecution queryExecution=null;
		String dsURI, accessURL, feature;	
		try{
			Query query = QueryFactory.create(queryString);		
			queryExecution = QueryExecutionFactory.sparqlService("http://ogd.ifs.tuwien.ac.at/sparql", query);
//			queryExecution = QueryExecutionFactory.sparqlService("http://localhost:8890/sparql", query);
			
			// execute query
			ResultSet rs = queryExecution.execSelect();			
			while (rs!=null && rs.hasNext()) {		
				QuerySolution sol = rs.nextSolution();				
				dsURI 		= sol.get("ds").toString();		
				accessURL 	= sol.get("url").toString();	
				feature 	= sol.get("feature").toString();
				return new StringTriple(dsURI, accessURL, feature);
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
		return new StringTriple();
	}   
     
    public ArrayList<String> queryLocation(String metadata, String dataset, String sFilter1, String sFilter2){
		String queryString;
		
		queryString =	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+					
						"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
						"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
						"PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n"+					
						"SELECT DISTINCT ?d ?v \n"+ 
						"WHERE{ \n"+ 
					  	" graph <http://statspace.linkedwidgets.org> { \n" +							
						"	<" + metadata + "> qb:component ?d. \n"+
						"	<" + dataset + "> rdf:value ?v. \n"+
						"	{ ?d rdf:value ?v. \n"+
						"	  ?l owl:sameAs ?v. \n"+							
						"	  FILTER ("+ sFilter1 + ") \n "+
						"	}UNION{\n"+
						"	  ?d rdf:value ?v. \n"+												
						"	  FILTER ("+ sFilter2 + ") \n "+
						"	}\n"+
						"  }\n"+
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
//			queryExecution = QueryExecutionFactory.sparqlService("http://localhost:8890/sparql", query);
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
